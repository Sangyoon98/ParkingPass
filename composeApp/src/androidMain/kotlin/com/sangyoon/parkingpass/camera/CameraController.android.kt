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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.ByteArrayOutputStream
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit

/**
 * Android CameraX 기반 카메라 컨트롤러 구현
 */
actual class CameraController(private val androidContext: Context) {
    private var imageCapture: ImageCapture? = null
    private var imageAnalysis: ImageAnalysis? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var preview: Preview? = null
    private var lifecycleOwner: LifecycleOwner? = null
    private var cameraExecutor: ExecutorService = Executors.newSingleThreadExecutor()
    private var analysisCallback: (suspend (ByteArray) -> Unit)? = null
    private var analysisScope: CoroutineScope? = null

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
        // LifecycleOwner 저장 (startImageAnalysis에서 사용)
        this.lifecycleOwner = lifecycleOwner
        
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

                // ImageAnalysis 설정 (실시간 프레임 분석용)
                imageAnalysis = ImageAnalysis.Builder()
                    .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                    .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                    .build()

                // ImageCapture 설정 (필요 시 사용)
                imageCapture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()

                // Camera 선택 (후면 카메라)
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                // 기존 바인딩 해제 후 재바인딩
                provider.unbindAll()
                
                // Preview가 없으면 에러
                val previewUseCase = preview
                if (previewUseCase == null) {
                    Log.e(TAG, "Preview가 설정되지 않았습니다")
                    onError?.invoke("Preview가 설정되지 않았습니다")
                    return@addListener
                }
                
                // Preview만 먼저 바인딩 (ImageAnalysis는 startImageAnalysis에서 추가)
                provider.bindToLifecycle(
                    lifecycleOwner,
                    cameraSelector,
                    previewUseCase
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
        stopImageAnalysis()
        cameraProvider?.unbindAll()
        cameraProvider = null
        preview = null
        imageCapture = null
        imageAnalysis = null
        lifecycleOwner = null
        
        // ExecutorService 종료 (스레드 누수 방지)
        try {
            cameraExecutor.shutdown()
            if (!cameraExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                Log.w(TAG, "cameraExecutor가 2초 내에 종료되지 않아 강제 종료합니다")
                cameraExecutor.shutdownNow()
            }
        } catch (e: InterruptedException) {
            Log.w(TAG, "cameraExecutor 종료 중 인터럽트 발생", e)
            Thread.currentThread().interrupt()
            cameraExecutor.shutdownNow()
        }
        
        // 종료된 ExecutorService는 재사용할 수 없으므로 새로운 인스턴스 생성
        cameraExecutor = Executors.newSingleThreadExecutor()
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

    /**
     * ImageProxy를 JPEG ByteArray로 변환
     */
    private fun convertImageProxyToJpeg(imageProxy: ImageProxy): ByteArray {
        val format = imageProxy.format

        return when (format) {
            AndroidImageFormat.JPEG -> {
                // 이미 JPEG 형식인 경우 직접 사용
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                bytes
            }
            AndroidImageFormat.YUV_420_888 -> {
                // YUV_420_888 형식인 경우 JPEG로 변환
                if (imageProxy.planes.size < 3) {
                    val buffer = imageProxy.planes[0].buffer
                    val bytes = ByteArray(buffer.remaining())
                    buffer.get(bytes)
                    return bytes
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

                // Y 플레인 복사
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
                jpegOutputStream.toByteArray()
            }
            else -> {
                // 다른 형식인 경우 첫 번째 plane만 사용
                val buffer = imageProxy.planes[0].buffer
                val bytes = ByteArray(buffer.remaining())
                buffer.get(bytes)
                bytes
            }
        }
    }

    actual fun startImageAnalysis(onFrame: suspend (ByteArray) -> Unit) {
        analysisCallback = onFrame
        // 분석용 코루틴 스코프 생성 (프레임마다 새로운 스코프를 생성하지 않도록)
        analysisScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

        // ExecutorService가 종료되었으면 재생성 (방어적 프로그래밍)
        if (cameraExecutor.isShutdown || cameraExecutor.isTerminated) {
            Log.w(TAG, "cameraExecutor가 종료되어 재생성합니다")
            cameraExecutor = Executors.newSingleThreadExecutor()
        }

        val analysis = imageAnalysis
        val provider = cameraProvider
        val owner = lifecycleOwner
        
        if (analysis != null && provider != null && owner != null) {
            // Analyzer 설정
            analysis.setAnalyzer(cameraExecutor) { imageProxy ->
                try {
                    val jpegBytes = convertImageProxyToJpeg(imageProxy)
                    // 클래스 레벨 스코프에서 콜백 실행
                    analysisScope?.launch {
                        analysisCallback?.invoke(jpegBytes)
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "프레임 분석 중 오류 발생", e)
                } finally {
                    imageProxy.close()
                }
            }
            
            // ImageAnalysis를 기존 바인딩에 추가
            val previewUseCase = preview
            if (previewUseCase != null) {
                // 기존 바인딩 해제 후 Preview와 ImageAnalysis 함께 바인딩
                provider.unbindAll()
                provider.bindToLifecycle(
                    owner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    previewUseCase,
                    analysis
                )
                Log.d(TAG, "ImageAnalysis가 바인딩되었습니다")
            } else {
                Log.e(TAG, "Preview가 없어 ImageAnalysis를 바인딩할 수 없습니다")
            }
        } else {
            Log.w(TAG, "카메라가 시작되지 않았거나 ImageAnalysis/LifecycleOwner가 설정되지 않았습니다. analysis: ${analysis != null}, provider: ${provider != null}, owner: ${owner != null}")
        }
    }

    actual fun stopImageAnalysis() {
        imageAnalysis?.clearAnalyzer()
        analysisCallback = null
        // 분석용 코루틴 스코프 취소 및 정리
        analysisScope?.cancel()
        analysisScope = null
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

