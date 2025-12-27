package com.sangyoon.parkingpass.camera

import com.sangyoon.parkingpass.camera.ios.CameraHelper
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.addressOf
import kotlinx.cinterop.memScoped
import kotlinx.cinterop.usePinned
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
}

/**
 * iOS CameraController 생성
 */
actual fun createCameraController(context: Any?): CameraController {
    val viewController = context as? UIViewController
        ?: throw IllegalArgumentException("UIViewController required for iOS CameraController")
    return CameraController(viewController)
}

