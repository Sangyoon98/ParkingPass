package com.sangyoon.parkingpass.presentation.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import platform.AVFoundation.AVCaptureVideoPreviewLayer
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import platform.UIKit.UIColor
import platform.CoreGraphics.CGPoint
import com.sangyoon.parkingpass.camera.CameraController
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.camera.createCameraController
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import kotlinx.coroutines.delay

@OptIn(ExperimentalMaterial3Api::class, ExperimentalForeignApi::class)
@Composable
actual fun CameraScreen(
    viewModel: PlateDetectionViewModel,
    parkingLotId: Long,
    onBack: () -> Unit,
    onImageCaptured: (CameraImage) -> Unit
) {
    // UIViewController 생성 (CameraController에서 필요)
    val viewController = remember { UIViewController() }
    var cameraController: CameraController? by remember { mutableStateOf(null) }
    var hasPermission by remember { mutableStateOf(false) }
    var isAnalyzing by remember { mutableStateOf(false) }
    var previewView: UIView? by remember { mutableStateOf(null) }

    // 카메라 컨트롤러 초기화 및 권한 확인
    LaunchedEffect(Unit) {
        cameraController = createCameraController(viewController)
        hasPermission = cameraController?.hasPermission() ?: false
        
        // 권한이 없으면 요청
        if (!hasPermission) {
            val granted = cameraController?.requestPermission() ?: false
            hasPermission = granted
        }
        
        // 권한이 있으면 카메라 시작
        if (hasPermission && cameraController != null) {
            val iosController = cameraController as com.sangyoon.parkingpass.camera.CameraController
            iosController.setupCamera { view ->
                // 메인 스레드에서 previewView 설정
                previewView = view
            }
        }
    }

    DisposableEffect(Unit) {
        onDispose {
            // cleanup 시에만 stopCamera 호출 (cameraController가 변경될 때는 호출하지 않음)
            cameraController?.stopCamera()
        }
    }

    Scaffold(
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
            if (hasPermission && cameraController != null) {
                // iOS 카메라 프리뷰
                previewView?.let { view ->
                    UIKitView(
                        factory = { view },
                        modifier = Modifier.fillMaxSize()
                    )
                } ?: Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("카메라 프리뷰 로딩 중...")
                }

                // 실시간 번호판 인식 (2초마다 프레임 분석)
                // previewView가 설정된 후에만 시작
                val currentController = cameraController
                LaunchedEffect(currentController, hasPermission, previewView) {
                    if (currentController != null && hasPermission && previewView != null) {
                        println("📷 [CameraScreen] 번호판 인식 시작")
                        delay(3000) // 카메라 세션이 완전히 시작될 때까지 대기
                        while (true) {
                            if (!isAnalyzing) {
                                isAnalyzing = true
                                try {
                                    println("📸 [CameraScreen] 이미지 촬영 시도")
                                    val image = currentController.captureImage()
                                    if (image != null) {
                                        println("🖼️ [CameraScreen] 이미지 촬영 성공, 번호판 인식 시작")
                                        // 이미지 분석 - 번호판이 인식되면 true 반환
                                        val recognized = viewModel.recognizePlateFromImage(image)
                                        if (recognized) {
                                            println("🎉 [CameraScreen] 번호판 인식 성공, 화면 종료")
                                            // 번호판 인식 성공 - 카메라 화면 종료
                                            onImageCaptured(image)
                                            return@LaunchedEffect
                                        } else {
                                            println("❌ [CameraScreen] 번호판 인식 실패, 다음 시도 대기")
                                        }
                                    } else {
                                        println("⚠️ [CameraScreen] 이미지 촬영 실패 (null)")
                                    }
                                } catch (e: Exception) {
                                    // 에러 로그 출력
                                    println("💥 [CameraScreen] 이미지 처리 에러: ${e.message}")
                                    e.printStackTrace()
                                } finally {
                                    isAnalyzing = false
                                }
                            }
                            delay(2000) // 2초마다 다음 분석
                        }
                    }
                }

                // 분석 중 표시
                if (isAnalyzing) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(16.dp)
                    ) {
                        CircularProgressIndicator()
                    }
                }
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        "카메라 권한이 필요합니다",
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        }
    }
}
