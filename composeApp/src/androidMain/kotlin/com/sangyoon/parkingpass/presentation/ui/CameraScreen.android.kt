package com.sangyoon.parkingpass.presentation.ui

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.view.PreviewView
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.LifecycleOwner
import com.sangyoon.parkingpass.camera.CameraController
import com.sangyoon.parkingpass.camera.CameraImage
import com.sangyoon.parkingpass.camera.createCameraController
import com.sangyoon.parkingpass.presentation.viewmodel.PlateDetectionViewModel
import kotlinx.coroutines.delay

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
    var isAnalyzing by remember { mutableStateOf(false) }

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
        hasPermission = cameraController?.hasPermission() ?: false
        if (!hasPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    DisposableEffect(cameraController) {
        onDispose {
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
                // 카메라 프리뷰
                var previewView: PreviewView? by remember { mutableStateOf(null) }
                
                AndroidView(
                    factory = { ctx ->
                        PreviewView(ctx).also { pv ->
                            previewView = pv
                            val androidController = cameraController as? com.sangyoon.parkingpass.camera.CameraController
                            androidController?.let {
                                // startCamera 메서드를 리플렉션으로 호출
                                try {
                                    val method = it.javaClass.getDeclaredMethod(
                                        "startCamera",
                                        PreviewView::class.java,
                                        LifecycleOwner::class.java
                                    )
                                    method.isAccessible = true
                                    method.invoke(it, pv, lifecycleOwner)
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxSize()
                )

                // 실시간 번호판 인식 (2초마다 프레임 분석)
                val currentController = cameraController
                LaunchedEffect(currentController, hasPermission) {
                    if (currentController != null && hasPermission) {
                        delay(3000) // 카메라 초기화 대기
                        while (true) {
                            if (!isAnalyzing) {
                                isAnalyzing = true
                                try {
                                    val image = currentController.captureImage()
                                    if (image != null) {
                                        // 이미지 분석 - 번호판이 인식되면 true 반환
                                        val recognized = viewModel.recognizePlateFromImage(image)
                                        if (recognized) {
                                            // 번호판 인식 성공 - 카메라 화면 종료
                                            onImageCaptured(image)
                                            return@LaunchedEffect
                                        }
                                    }
                                } catch (e: Exception) {
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


