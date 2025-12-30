package com.sangyoon.parkingpass.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.domain.model.Gate
import com.sangyoon.parkingpass.domain.usecase.GetCurrentSessionByPlateUseCase
import com.sangyoon.parkingpass.domain.usecase.GetGatesUseCase
import com.sangyoon.parkingpass.domain.usecase.GetVehicleByPlateUseCase
import com.sangyoon.parkingpass.domain.usecase.PlateDetectedUseCase
import com.sangyoon.parkingpass.presentation.state.PlateDetectionUiState
import com.sangyoon.parkingpass.presentation.state.VehicleInfo
import com.sangyoon.parkingpass.recognition.PlateNumberExtractor
import com.sangyoon.parkingpass.recognition.createTextRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class PlateDetectionViewModel(
    private val getGatesUseCase: GetGatesUseCase,
    private val plateDetectedUseCase: PlateDetectedUseCase,
    private val getVehicleByPlateUseCase: GetVehicleByPlateUseCase,
    private val getCurrentSessionByPlateUseCase: GetCurrentSessionByPlateUseCase
): ViewModel() {

    private val _selectedParkingLotId = MutableStateFlow<Long?>(null)
    val selectedParkingLotId: kotlinx.coroutines.flow.StateFlow<Long?> = _selectedParkingLotId.asStateFlow()

    private val _uiState = MutableStateFlow(PlateDetectionUiState())
    val uiState = _uiState.asStateFlow()

    fun setSelectedParkingLotId(parkingLotId: Long) {
        _selectedParkingLotId.value = parkingLotId
        loadGates(parkingLotId)
    }

    fun loadGates(parkingLotId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            getGatesUseCase(parkingLotId).fold(
                onSuccess = { gates ->
                    _uiState.update { it.copy(gates = gates, isLoading = false) }
                },
                onFailure = { e ->
                    _uiState.update { it.copy(isLoading = false, error = e.message ?: "게이트 목록 조회 실패")}
                }
            )
        }
    }

    fun selectGate(gate: Gate) {
        _uiState.update { it.copy(selectedGate = gate) }
    }

    fun updatePlateNumber(plateNumber: String) {
        _uiState.update { it.copy(plateNumber = plateNumber) }
    }

    fun detectPlate(onSuccess: () -> Unit) {
        val selectedGate = _uiState.value.selectedGate
        val plateNumber = _uiState.value.plateNumber

        if (selectedGate == null) {
            _uiState.update { it.copy(error = "게이트를 선택해주세요") }
            return
        }

        if (plateNumber.isBlank()) {
            _uiState.update { it.copy(error = "번호판 번호를 입력해주세요") }
            return
        }

        if (_uiState.value.isDetecting) {
            return  // 이미 감지 중이면 중복 요청 방지
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isDetecting = true, error = null, result = null) }
            plateDetectedUseCase(selectedGate.deviceKey, plateNumber.trim()).fold(
                onSuccess = { response ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            result = response,
                            plateNumber = ""  // 성공 후 번호판 초기화
                        )
                    }
                    onSuccess()
                },
                onFailure = { e ->
                    _uiState.update {
                        it.copy(
                            isDetecting = false,
                            error = e.message ?: "입출차 체크 실패"
                        )
                    }
                }
            )
        }
    }

    fun clearResult() {
        _uiState.update { it.copy(result = null) }
    }

    /**
     * 카메라로 캡처한 이미지에서 번호판을 인식합니다.
     * 번호판이 성공적으로 인식되면 자동으로 입력 필드에 설정합니다.
     * 
     * @return 번호판이 인식되었으면 true, 아니면 false
     */
    suspend fun recognizePlateFromImage(image: CameraImage): Boolean {
        return try {
            // 텍스트 인식 (플랫폼별 구현)
            val textRecognizer = createTextRecognizer()
            val recognitionResult = textRecognizer.recognize(image.bytes)
            
            // 디버깅: 인식된 텍스트 로그
            if (recognitionResult.text.isNotBlank()) {
                println("🔍 [PlateDetection] 인식된 텍스트: ${recognitionResult.text}")
            }
            
            // 인식된 텍스트가 비어있으면 바로 반환
            if (recognitionResult.text.isBlank()) {
                return false
            }
            
            // 오인식 보정
            val correctedText = PlateNumberExtractor.correctCommonMistakes(recognitionResult.text)
            
            // 번호판 추출
            val plateNumber = PlateNumberExtractor.extractPlateNumber(correctedText)
            
            if (plateNumber != null && PlateNumberExtractor.isValidPlateNumber(plateNumber)) {
                println("✅ [PlateDetection] 번호판 인식 성공: $plateNumber")
                // 유효한 번호판이면 입력 필드에 설정
                _uiState.update { 
                    it.copy(
                        plateNumber = plateNumber,
                        isLoading = false,
                        error = null
                    )
                }
                true // 번호판 인식 성공
            } else {
                if (plateNumber != null) {
                    println("⚠️ [PlateDetection] 유효하지 않은 번호판: $plateNumber")
                }
                false // 번호판 인식 실패 (에러 메시지 표시하지 않음 - 연속 분석 중이므로)
            }
        } catch (e: Exception) {
            // 에러 발생 시 로그 출력
            println("❌ [PlateDetection] 번호판 인식 에러: ${e.message}")
            e.printStackTrace()
            false
        }
    }
    
    /**
     * 번호판이 인식되었을 때 자동으로 입출차 처리를 시도합니다.
     * 게이트가 선택되어 있고 번호판이 있으면 자동으로 detectPlate를 호출합니다.
     * 
     * @return 입출차 처리가 시작되었으면 true, 그렇지 않으면 false
     */
    fun tryAutoDetectPlate(onSuccess: () -> Unit = {}): Boolean {
        val currentState = _uiState.value
        return if (currentState.selectedGate != null && 
            currentState.plateNumber.isNotBlank() && 
            !currentState.isDetecting) {
            detectPlate(onSuccess)
            true
        } else {
            false
        }
    }

    /**
     * 실시간 프레임 분석
     * 각 프레임에서 번호판을 인식합니다.
     */
    suspend fun analyzeFrame(imageBytes: ByteArray) {
        // 인식 중이 아니거나 바텀시트가 열려있으면 분석하지 않음
        if (!_uiState.value.isRecognizing || _uiState.value.showVehicleSheet) {
            return
        }

        return try {
            // 텍스트 인식
            val textRecognizer = createTextRecognizer()
            val recognitionResult = textRecognizer.recognize(imageBytes)
            
            println("🔍 [PlateDetection] OCR 결과: ${recognitionResult.text.take(50)}")

            // 인식된 텍스트가 비어있으면 바로 반환
            if (recognitionResult.text.isBlank()) {
                return
            }

            // 오인식 보정
            val correctedText = PlateNumberExtractor.correctCommonMistakes(recognitionResult.text)

            // 번호판 추출
            val plateNumber = PlateNumberExtractor.extractPlateNumber(correctedText)
            
            println("🔍 [PlateDetection] 추출된 번호: $plateNumber")

            if (plateNumber != null && PlateNumberExtractor.isValidPlateNumber(plateNumber)) {
                println("✅ [PlateDetection] 번호판 인식 성공: $plateNumber")
                
                // 인식 성공 시 인식 중지 및 차량 정보 조회
                stopRecognition()
                _uiState.update { it.copy(recognizedPlate = plateNumber) }
                loadVehicleInfo(plateNumber)
            } else {

            }
        } catch (e: Exception) {
            println("❌ [PlateDetection] 프레임 분석 에러: ${e.message}")
            e.printStackTrace()
            // 에러는 조용히 처리 (연속 분석 중이므로)
        }
    }

    /**
     * 차량 정보 및 현재 세션 조회
     */
    private suspend fun loadVehicleInfo(plateNumber: String) {
        val parkingLotId = _selectedParkingLotId.value ?: return

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }

            println("🔍 [PlateDetection] 차량 정보 조회 시작 - parkingLotId: $parkingLotId, plateNumber: $plateNumber")

            val vehicleResult = getVehicleByPlateUseCase(parkingLotId, plateNumber)
            val sessionResult = getCurrentSessionByPlateUseCase(parkingLotId, plateNumber)

            // 에러 로그 출력
            vehicleResult.onFailure { error ->
                println("❌ [PlateDetection] 차량 조회 실패: ${error.message}")
                error.printStackTrace()
            }
            sessionResult.onFailure { error ->
                println("❌ [PlateDetection] 세션 조회 실패: ${error.message}")
                error.printStackTrace()
            }

            val vehicle = vehicleResult.getOrNull()
            val session = sessionResult.getOrNull()

            println("🔍 [PlateDetection] 조회 결과 - vehicle: ${vehicle != null}, session: ${session != null}")
            if (vehicle != null) {
                println("🔍 [PlateDetection] 차량 정보: id=${vehicle.id}, plateNumber=${vehicle.plateNumber}, label=${vehicle.label}")
            }

            val vehicleInfo = VehicleInfo(
                plateNumber = plateNumber,
                vehicle = vehicle,
                currentSession = session
            )

            _uiState.update {
                it.copy(
                    vehicleInfo = vehicleInfo,
                    showVehicleSheet = true,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 번호판 인식 중지
     */
    fun stopRecognition() {
        _uiState.update { it.copy(isRecognizing = false) }
    }

    /**
     * 번호판 인식 재개 (바텀시트 닫은 후)
     */
    fun resumeRecognition() {
        println("📷 [PlateDetection] 인식 재개")
        _uiState.update {
            it.copy(
                recognizedPlate = null,
                vehicleInfo = null,
                showVehicleSheet = false,
                isRecognizing = true
            )
        }
    }

    /**
     * 바텀시트 닫기
     */
    fun dismissVehicleSheet() {
        resumeRecognition()
    }
}