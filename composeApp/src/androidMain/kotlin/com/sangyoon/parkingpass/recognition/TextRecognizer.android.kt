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
        // 이미지 크기 제한 (10MB)
        if (imageBytes.size > 10 * 1024 * 1024) {
            throw IllegalArgumentException("Image size exceeds maximum limit of 10MB")
        }

        // 이미지 바운드 먼저 디코딩하여 크기 확인
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        
        // 이미지 크기가 0인 경우 에러
        if (options.outWidth <= 0 || options.outHeight <= 0) {
            throw IllegalArgumentException("Invalid image dimensions")
        }
        
        // 큰 이미지는 다운샘플링하여 메모리 사용량 감소
        // 최대 2000x2000 크기를 초과하면 다운샘플링
        val maxDimension = 2000
        options.inJustDecodeBounds = false
        options.inSampleSize = calculateInSampleSize(options, maxDimension, maxDimension)
        // 메모리 효율적인 컬러 설정
        options.inPreferredConfig = android.graphics.Bitmap.Config.RGB_565
        
        // 실제 비트맵 디코딩
        val bitmap = try {
            BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
        } catch (e: OutOfMemoryError) {
            // OOM 발생 시 더 작은 샘플 사이즈로 재시도
            options.inSampleSize *= 2
            try {
                BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size, options)
            } catch (e2: OutOfMemoryError) {
                throw IllegalArgumentException("Image too large to decode", e2)
            }
        } ?: throw IllegalArgumentException("Failed to decode image bytes")

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
 * 이미지 크기에 맞는 inSampleSize 계산
 * @param options BitmapFactory.Options (inJustDecodeBounds가 true로 설정된 상태)
 * @param reqWidth 목표 너비
 * @param reqHeight 목표 높이
 * @return 적절한 inSampleSize 값
 */
private fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // 현재 샘플 사이즈로 목표 크기보다 크면 샘플 사이즈를 2배씩 증가
        while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
            inSampleSize *= 2
        }
    }

    return inSampleSize
}

/**
 * Android TextRecognizer 생성
 */
actual fun createTextRecognizer(): TextRecognizer = TextRecognizer()

