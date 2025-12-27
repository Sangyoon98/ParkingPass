package com.sangyoon.parkingpass.recognition

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.Text
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

/**
 * Android ML Kit 기반 텍스트 인식기 구현
 */
actual class TextRecognizer {
    private val recognizer = TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())

    actual suspend fun recognize(imageBytes: ByteArray): TextRecognitionResult {
        // 바이트 배열을 Bitmap으로 변환
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: throw IllegalArgumentException("Failed to decode image bytes")

        // InputImage 생성
        val image = InputImage.fromBitmap(bitmap, 0)

        // 텍스트 인식 실행 (suspendCancellableCoroutine 사용)
        return suspendCancellableCoroutine { continuation ->
            recognizer.process(image)
                .addOnSuccessListener { result ->
                    // 결과 변환
                    val blocks = result.textBlocks.map { block: Text.TextBlock ->
                        TextBlock(
                            text = block.text,
                            confidence = 0.8f // ML Kit v2는 confidence를 직접 제공하지 않으므로 기본값 사용
                        )
                    }

                    continuation.resume(
                        TextRecognitionResult(
                            text = result.text,
                            confidence = if (blocks.isNotEmpty()) 0.8f else 0f,
                            blocks = blocks
                        )
                    )
                }
                .addOnFailureListener { exception ->
                    continuation.resumeWithException(exception)
                }
        }
    }
}

/**
 * Android TextRecognizer 생성
 */
actual fun createTextRecognizer(): TextRecognizer = TextRecognizer()

