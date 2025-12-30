package com.sangyoon.parkingpass.presentation.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.sangyoon.parkingpass.camera.CameraController
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.camera.createCameraController
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
actual fun CameraScreen(
    viewModel: PlateDetectionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit,
    onImageCaptured: (CameraImage) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var cameraController: CameraController? by remember { mutableStateOf(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var hasCameraHardware by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    // 카메라 권한 요청
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasPermission = granted
        if (granted) {
            cameraController = createCameraController(context)
        }
    }

    LaunchedEffect(Unit) {
        cameraController = createCameraController(context)
        val androidController = cameraController
        hasCameraHardware = androidController?.hasCameraHardware() ?: false
        hasPermission = cameraController?.hasPermission() ?: false

        if (!hasCameraHardware) {
            coroutineScope.launch {
                snackbarHostState.showSnackbar("이 기기에는 카메라가 없습니다")
            }
        } else if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(cameraController) {
        onDispose {
            cameraController?.stopCamera()
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text("번호판 촬영") },
                navigationIcon = {
                    Button(onClick = onBack) {
                        Text("<")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (hasCameraHardware && hasPermission && cameraController != null) {
                // 카메라 프리뷰
                var previewView: PreviewView? by remember { mutableStateOf(null) }

                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { pv ->
                            previewView = pv
                            // Android 전용 startCamera 메서드 직접 호출
                            (cameraController as? com.sangyoon.parkingpass.camera.CameraController)?.let { androidController ->
                                val errorCallback: (String) -> Unit = { errorMessage ->
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar(errorMessage)
                                    }
                                }

                                try {
                                    androidController.startCamera(pv, lifecycleOwner, errorCallback)
                                } catch (e: Exception) {
                                    // 에러 발생 시 사용자에게 알림
                                    coroutineScope.launch {
                                        snackbarHostState.showSnackbar("카메라 시작 실패: ${e.message ?: "알 수 없는 오류"}")
                                    }
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 실시간 프레임 분석 시작
                val currentController = cameraController
                LaunchedEffect(currentController, hasPermission, hasCameraHardware, previewView) {
                    if (currentController != null && hasPermission && hasCameraHardware && previewView != null) {
                        delay(2000) // 카메라 초기화 대기
                        
                        println("📷 [CameraScreen] 프레임 분석 시작 준비")

                        // 인식 시작
                        viewModel.resumeRecognition()
                        println("📷 [CameraScreen] 인식 상태 재개")

                        // 프레임 분석 시작
                        try {
                            currentController.startImageAnalysis { imageBytes ->
                                println("📸 [CameraScreen] 프레임 수신: ${imageBytes.size} bytes")
                                viewModel.analyzeFrame(imageBytes)
                            }
                            println("📷 [CameraScreen] 프레임 분석 시작 완료")
                        } catch (e: Exception) {
                            println("💥 [CameraScreen] 프레임 분석 시작 실패: ${e.message}")
                            e.printStackTrace()
                        }
                    }
                }

                // 인식된 번호 오버레이
                val uiState by viewModel.uiState.collectAsState()
                if (uiState.recognizedPlate != null) {
                    Card(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.primaryContainer
                        )
                    ) {
                        Text(
                            text = uiState.recognizedPlate ?: "",
                            modifier = Modifier.padding(16.dp),
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        when {
                            !hasCameraHardware -> "이 기기에는 카메라가 없습니다"
                            !hasPermission -> "카메라 권한이 필요합니다"
                            else -> "카메라를 사용할 수 없습니다"
                        },
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }

        // 차량 정보 바텀시트 (Box 밖에서 uiState 사용)
        val uiState by viewModel.uiState.collectAsState()
        val vehicleInfoForSheet = uiState.vehicleInfo
        if (vehicleInfoForSheet != null && uiState.showVehicleSheet) {
            VehicleInfoBottomSheet(
                vehicleInfo = vehicleInfoForSheet,
                onEnter = {
                    val gate = uiState.selectedGate
                    val plate = vehicleInfoForSheet.plateNumber
                    if (gate != null && plate.isNotBlank()) {
                        viewModel.updatePlateNumber(plate)
                        viewModel.detectPlate {
                            viewModel.dismissVehicleSheet()
                            onBack()
                        }
                    }
                },
                onExit = {
                    val gate = uiState.selectedGate
                    val plate = vehicleInfoForSheet.plateNumber
                    if (gate != null && plate.isNotBlank()) {
                        viewModel.updatePlateNumber(plate)
                        viewModel.detectPlate {
                            viewModel.dismissVehicleSheet()
                            onBack()
                        }
                    }
                },
                onRegister = {
                    // TODO: 차량 등록 화면으로 이동
                    viewModel.dismissVehicleSheet()
                },
                onDismiss = {
                    viewModel.dismissVehicleSheet()
                }
            )
        }
    }
}


