package com.sangyoon.parkingpass.recognition

/**
 * 텍스트 인식 결과
 */
data class TextRecognitionResult(
    /** 인식된 전체 텍스트 */
    val text: String,
    /** 신뢰도 점수 (0.0 ~ 1.0) */
    val confidence: Float = 0f,
    /** 텍스트 블록 목록 */
    val blocks: List<TextBlock> = emptyList()
)

/**
 * 텍스트 블록 (줄 단위)
 */
data class TextBlock(
    /** 블록 내 텍스트 */
    val text: String,
    /** 신뢰도 */
    val confidence: Float = 0f
)

