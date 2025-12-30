package com.sangyoon.parkingpass.camera

import com.sangyoon.parkingpass.camera.ios.CameraHelper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.Foundation.getBytes
import platform.UIKit.UIView
import platform.UIKit.UIViewController
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * iOS Swift CameraHelper를 사용한 카메라 컨트롤러 구현
 */
actual class CameraController(private val viewController: UIViewController) {
    
    @OptIn(ExperimentalForeignApi::class)
    private val cameraHelper = CameraHelper.shared()!!
    
    private var analysisScope: CoroutineScope? = null
    private var analysisChannel: Channel<ByteArray>? = null
    
    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun requestPermission(): Boolean {
        return suspendCancellableCoroutine { continuation ->
            CameraHelper.requestPermissionWithCompletion { granted ->
                continuation.resume(granted)
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun hasPermission(): Boolean {
        return CameraHelper.hasPermission()
    }

    actual fun startCamera() {
        // 실제 구현은 setupCamera(previewLayer)를 사용해야 합니다
        // 이 메서드는 더미입니다
    }
    
    /**
     * 카메라를 시작하고 PreviewView를 반환합니다
     */
    @OptIn(ExperimentalForeignApi::class)
    fun setupCamera(completion: (UIView?) -> Unit) {
        cameraHelper.setupCameraWithCompletion { previewView: platform.UIKit.UIView? ->
            completion(previewView)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopCamera() {
        cameraHelper.stopCamera()
    }

    @OptIn(ExperimentalForeignApi::class)
    actual suspend fun captureImage(): CameraImage? {
        return suspendCancellableCoroutine { continuation ->
            cameraHelper.capturePhotoWithCompletion { imageData: platform.Foundation.NSData?, error: platform.Foundation.NSError? ->
                if (error != null) {
                    val errorMsg = error.localizedDescription
                        ?: "Photo capture failed"
                    continuation.resumeWithException(Exception(errorMsg))
                    return@capturePhotoWithCompletion
                }
                
                if (imageData == null) {
                    continuation.resume(null)
                    return@capturePhotoWithCompletion
                }
                
                val length = imageData.length.toInt()
                val bytes = ByteArray(length)
                memScoped {
                    bytes.usePinned { pinned ->
                        imageData.getBytes(pinned.addressOf(0), length.toULong())
                    }
                }
                
                continuation.resume(
                    CameraImage(
                        bytes = bytes,
                        format = ImageFormat.JPEG
                    )
                )
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun startImageAnalysis(onFrame: suspend (ByteArray) -> Unit) {
        // 기존 분석이 실행 중이면 중지
        stopImageAnalysis()
        
        // iOS에서는 suspend 함수를 Objective-C 클로저로 직접 전달할 수 없으므로
        // Channel을 사용하여 프레임 데이터를 전달합니다
        val channel = Channel<ByteArray>(Channel.UNLIMITED)
        analysisChannel = channel
        
        val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        analysisScope = scope
        
        // 채널에서 데이터를 읽어서 onFrame 콜백 호출
        scope.launch {
            try {
                for (frameData in channel) {
                    onFrame(frameData)
                }
            } catch (e: Exception) {
                // 채널이 닫히거나 취소된 경우
            }
        }
        
        // Swift 콜백에서 채널로 데이터 전달
        cameraHelper.startVideoAnalysisWithCallback { imageData: platform.Foundation.NSData ->
            val length = imageData.length.toInt()
            val bytes = ByteArray(length)
            memScoped {
                bytes.usePinned { pinned ->
                    imageData.getBytes(pinned.addressOf(0), length.toULong())
                }
            }
            channel.trySend(bytes)
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    actual fun stopImageAnalysis() {
        cameraHelper.stopVideoAnalysis()
        
        // 채널과 코루틴 스코프 정리
        analysisChannel?.close()
        analysisChannel = null
        analysisScope?.cancel()
        analysisScope = null
    }
}

/**
 * iOS CameraController 생성
 */
actual fun createCameraController(context: Any?): CameraController {
    val viewController = context as? UIViewController
        ?: throw IllegalArgumentException("UIViewController required for iOS CameraController")
    return CameraController(viewController)
}

