package com.sangyoon.parkingpass.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat as AndroidImageFormat
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import com.google.common.util.concurrent.ListenableFuture
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor

/**
 * Android CameraX 기반 카메라 컨트롤러 구현
 */
actual class CameraController(private val androidContext: Context) {
    private var imageCapture: ImageCapture? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null

    actual suspend fun requestPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            if (hasPermission()) {
                continuation.resume(true)
                return@suspendCancellableCoroutine
            }

            // 권한 요청은 Activity에서 해야 하므로, 여기서는 권한 확인만 수행
            // 실제 권한 요청은 Activity에서 처리해야 합니다
            continuation.resume(false)
        }
    }

    actual fun hasPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            androidContext,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    actual fun startCamera() {
        // CameraX는 lifecycle과 연결되어야 하므로
        // 실제 시작은 PreviewView를 받아서 처리해야 합니다
        // 이 메서드는 더미로 두고, 실제 시작은 startCamera(previewView, lifecycleOwner)를 사용합니다
    }

    /**
     * PreviewView와 LifecycleOwner를 받아 카메라를 시작합니다
     */
    fun startCamera(previewView: PreviewView, lifecycleOwner: LifecycleOwner) {
        val cameraProviderFuture: ListenableFuture<ProcessCameraProvider> = 
            ProcessCameraProvider.getInstance(androidContext)
        
        cameraProviderFuture.addListener({
            try {
                val provider = cameraProviderFuture.get()
                cameraProvider = provider

                // Preview 설정
                preview = Preview.Builder()
                    .build()
                    .also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }

                // ImageCapture 설정
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Camera 선택 (후면 카메라)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // 기존 바인딩 해제 후 재바인딩
                provider.unbindAll()
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(androidContext))
    }

    actual fun stopCamera() {
        cameraProvider?.unbindAll()
        cameraProvider = null
        preview = null
        imageCapture = null
    }

    actual suspend fun captureImage(): CameraImage? {
        val capture = imageCapture ?: return null

        return suspendCancellableCoroutine { continuation ->
            val executor: Executor = ContextCompat.getMainExecutor(androidContext)
            
            capture.takePicture(
                executor,
                object : ImageCapture.OnImageCapturedCallback() {
                    override fun onCaptureSuccess(imageProxy: ImageProxy) {
                        try {
                            val format = imageProxy.format

                            // ImageProxy를 JPEG 바이트 배열로 변환
                            when (format) {
                                AndroidImageFormat.JPEG -> {
                                    // 이미 JPEG 형식인 경우 직접 사용
                                    val buffer = imageProxy.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    
                                    continuation.resume(
                                        CameraImage(
                                            bytes = bytes,
                                            format = ImageFormat.JPEG
                                        )
                                    )
                                }
                                AndroidImageFormat.YUV_420_888 -> {
                                    // YUV_420_888 형식인 경우 JPEG로 변환
                                    // planes가 3개 미만인 경우 첫 번째 plane만 사용 (JPEG로 가정)
                                    if (imageProxy.planes.size < 3) {
                                        val buffer = imageProxy.planes[0].buffer
                                        val bytes = ByteArray(buffer.remaining())
                                        buffer.get(bytes)
                                        
                                        continuation.resume(
                                            CameraImage(
                                                bytes = bytes,
                                                format = ImageFormat.JPEG
                                            )
                                        )
                                        return
                                    }
                                    
                                    val yBuffer = imageProxy.planes[0].buffer
                                    val uBuffer = imageProxy.planes[1].buffer
                                    val vBuffer = imageProxy.planes[2].buffer
                                    
                                    val ySize = yBuffer.remaining()
                                    val uSize = uBuffer.remaining()
                                    val vSize = vBuffer.remaining()
                                    
                                    val nv21 = ByteArray(ySize + uSize + vSize)
                                    yBuffer.get(nv21, 0, ySize)
                                    vBuffer.get(nv21, ySize, vSize)
                                    uBuffer.get(nv21, ySize + vSize, uSize)
                                    
                                    // YUV_420_888을 JPEG로 변환
                                    val yuvImage = android.graphics.YuvImage(
                                        nv21,
                                        AndroidImageFormat.NV21,
                                        imageProxy.width,
                                        imageProxy.height,
                                        null
                                    )
                                    
                                    val jpegOutputStream = ByteArrayOutputStream()
                                    yuvImage.compressToJpeg(
                                        android.graphics.Rect(0, 0, imageProxy.width, imageProxy.height),
                                        90,
                                        jpegOutputStream
                                    )
                                    
                                    continuation.resume(
                                        CameraImage(
                                            bytes = jpegOutputStream.toByteArray(),
                                            format = ImageFormat.JPEG
                                        )
                                    )
                                }
                                else -> {
                                    // 다른 형식인 경우 첫 번째 plane만 사용하여 JPEG로 인코딩 시도
                                    val buffer = imageProxy.planes[0].buffer
                                    val bytes = ByteArray(buffer.remaining())
                                    buffer.get(bytes)
                                    
                                    continuation.resume(
                                        CameraImage(
                                            bytes = bytes,
                                            format = ImageFormat.JPEG
                                        )
                                    )
                                }
                            }
                        } catch (e: Exception) {
                            continuation.resumeWithException(e)
                        } finally {
                            imageProxy.close()
                        }
                    }

                    override fun onError(exception: ImageCaptureException) {
                        continuation.resumeWithException(exception)
                    }
                }
            )
        }
    }
}

/**
 * Android CameraController 생성
 */
actual fun createCameraController(context: Any?): CameraController {
    val androidContext = context as? android.content.Context
        ?: throw IllegalArgumentException("Context required for Android CameraController")
    return CameraController(androidContext)
}

