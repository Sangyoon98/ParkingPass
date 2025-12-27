package com.sangyoon.parkingpass.recognition

import platform.Foundation.*
import com.sangyoon.parkingpass.camera.ios.*
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlinx.cinterop.*
import kotlin.coroutines.resumeWithException

/**
 * iOS Swift CameraHelper를 사용한 텍스트 인식기 구현
 */
actual class TextRecognizer {
    
    @OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
    actual suspend fun recognize(imageBytes: ByteArray): TextRecognitionResult {
        return suspendCancellableCoroutine { continuation ->
            // NSData.create는 바이트를 복사하므로 pinned scope 외부에서 안전하게 사용 가능
            val imageData = imageBytes.usePinned { pinned ->
                NSData.create(
                    bytes = pinned.addressOf(0),
                    length = imageBytes.size.toULong()
                )
            }
            
            CameraHelper.recognizeTextWithImageData(imageData) { text: String?, confidence: Float, error: NSError? ->
                if (error != null) {
                    continuation.resumeWithException(
                        Exception(error.localizedDescription ?: "Text recognition failed")
                    )
                    return@recognizeTextWithImageData
                }
                
                val recognizedText = text ?: ""
                val finalConfidence = confidence
                
                // 텍스트를 블록 단위로 분리 (간단히 줄바꿈 기준)
                val blocks = recognizedText.split("\n").filter { it.isNotBlank() }.map { blockText ->
                    TextBlock(
                        text = blockText.trim(),
                        confidence = finalConfidence
                    )
                }
                
                continuation.resume(
                    TextRecognitionResult(
                        text = recognizedText,
                        confidence = finalConfidence,
                        blocks = blocks
                    )
                )
            }
        }
    }
}

/**
 * iOS TextRecognizer 생성
 */
actual fun createTextRecognizer(): TextRecognizer = TextRecognizer()

