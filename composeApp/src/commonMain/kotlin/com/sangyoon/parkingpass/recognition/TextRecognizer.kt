package com.sangyoon.parkingpass.recognition

/**
 * 플랫폼별 텍스트 인식기 인터페이스
 */
expect class TextRecognizer {
    /**
     * 이미지에서 텍스트를 인식합니다.
     * 
     * @param imageBytes 이미지 바이트 배열 (JPEG, PNG 등)
     * @return 텍스트 인식 결과
     */
    suspend fun recognize(imageBytes: ByteArray): TextRecognitionResult
}

/**
 * 플랫폼별 TextRecognizer 인스턴스 생성
 */
expect fun createTextRecognizer(): TextRecognizer

