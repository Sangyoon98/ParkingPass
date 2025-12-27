package com.sangyoon.parkingpass.camera

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat as AndroidImageFormat
import android.util.Log
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

    companion object {
        private const val TAG = "CameraController"
    }

    /**
     * 기기에 카메라 하드웨어가 있는지 확인합니다
     */
    fun hasCameraHardware(): Boolean {
        return androidContext.packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY)
    }

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
     * @param previewView 카메라 프리뷰를 표시할 PreviewView
     * @param lifecycleOwner LifecycleOwner
     * @param onError 카메라 초기화 실패 시 호출될 콜백 (선택사항)
     */
    fun startCamera(
        previewView: PreviewView, 
        lifecycleOwner: LifecycleOwner,
        onError: ((String) -> Unit)? = null
    ) {
        // 카메라 하드웨어 가용성 확인
        if (!hasCameraHardware()) {
            val errorMessage = "이 기기에는 카메라가 없습니다"
            Log.e(TAG, errorMessage)
            onError?.invoke(errorMessage)
            return
        }

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
                Log.d(TAG, "카메라가 성공적으로 시작되었습니다")
            } catch (e: Exception) {
                val errorMessage = "카메라 초기화 실패: ${e.message}"
                Log.e(TAG, errorMessage, e)
                onError?.invoke(errorMessage)
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
                                    
                                    val yPlane = imageProxy.planes[0]
                                    val uPlane = imageProxy.planes[1]
                                    val vPlane = imageProxy.planes[2]
                                    
                                    val width = imageProxy.width
                                    val height = imageProxy.height
                                    
                                    val yBuffer = yPlane.buffer
                                    val uBuffer = uPlane.buffer
                                    val vBuffer = vPlane.buffer
                                    
                                    val yRowStride = yPlane.rowStride
                                    val yPixelStride = yPlane.pixelStride
                                    val uRowStride = uPlane.rowStride
                                    val uPixelStride = uPlane.pixelStride
                                    val vRowStride = vPlane.rowStride
                                    val vPixelStride = vPlane.pixelStride
                                    
                                    // NV21: Y 플레인 (width * height) + 인터리브된 VU 플레인 (width * height / 2)
                                    val nv21Size = width * height * 3 / 2
                                    val nv21 = ByteArray(nv21Size)
                                    
                                    // Y 플레인 복사 - 정확히 width * height만큼만 복사
                                    val yBytes = ByteArray(yBuffer.remaining())
                                    yBuffer.get(yBytes)
                                    
                                    if (yRowStride == width && yPixelStride == 1) {
                                        // 정렬된 Y 플레인 - 직접 복사
                                        System.arraycopy(yBytes, 0, nv21, 0, width * height)
                                    } else {
                                        // 패딩이 있는 경우 - rowStride를 고려하여 복사
                                        var srcPos = 0
                                        var dstPos = 0
                                        for (row in 0 until height) {
                                            System.arraycopy(yBytes, srcPos, nv21, dstPos, width)
                                            srcPos += yRowStride
                                            dstPos += width
                                        }
                                    }
                                    
                                    // VU 플레인을 NV21 형식으로 인터리브 (NV21은 VU 순서)
                                    val uvOffset = width * height
                                    val uvWidth = width / 2
                                    val uvHeight = height / 2
                                    
                                    // U와 V 플레인 데이터 읽기
                                    val uBytes = ByteArray(uBuffer.remaining())
                                    val vBytes = ByteArray(vBuffer.remaining())
                                    uBuffer.get(uBytes)
                                    vBuffer.get(vBytes)
                                    
                                    // U와 V를 VU 순서로 인터리브
                                    var uSrcPos = 0
                                    var vSrcPos = 0
                                    var dstPos = uvOffset
                                    
                                    for (row in 0 until uvHeight) {
                                        for (col in 0 until uvWidth) {
                                            // NV21은 VU 순서 (V 먼저, U 나중)
                                            if (vSrcPos < vBytes.size) {
                                                nv21[dstPos++] = vBytes[vSrcPos]
                                            }
                                            vSrcPos += vPixelStride
                                            
                                            if (uSrcPos < uBytes.size) {
                                                nv21[dstPos++] = uBytes[uSrcPos]
                                            }
                                            uSrcPos += uPixelStride
                                        }
                                        // rowStride 보정 - 다음 행으로 이동
                                        uSrcPos += (uRowStride - uvWidth * uPixelStride)
                                        vSrcPos += (vRowStride - uvWidth * vPixelStride)
                                    }
                                    
                                    // YUV_420_888을 JPEG로 변환
                                    val yuvImage = android.graphics.YuvImage(
                                        nv21,
                                        AndroidImageFormat.NV21,
                                        width,
                                        height,
                                        null
                                    )
                                    
                                    val jpegOutputStream = ByteArrayOutputStream()
                                    yuvImage.compressToJpeg(
                                        android.graphics.Rect(0, 0, width, height),
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

