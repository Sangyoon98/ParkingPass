package com.sangyoon.parkingpass.recognition

/**
 * 번호판 번호 추출 및 검증 유틸리티
 */
object PlateNumberExtractor {
    /**
     * 한국 번호판 패턴
     */
    private val platePatterns = listOf(
        // 구형: 12가3456, 01가1234
        Regex("\\d{2}[가-힣]\\d{4}"),
        // 신형: 123가4567, 001가1234
        Regex("\\d{3}[가-힣]\\d{4}"),
        // 구형 (공백 포함): 12 가 3456
        Regex("\\d{2}\\s*[가-힣]\\s*\\d{4}"),
        // 신형 (공백 포함): 123 가 4567
        Regex("\\d{3}\\s*[가-힣]\\s*\\d{4}")
    )

    /**
     * 인식된 텍스트에서 번호판 번호를 추출합니다.
     * 
     * @param recognizedText OCR로 인식된 텍스트
     * @return 추출된 번호판 번호 (없으면 null)
     */
    fun extractPlateNumber(recognizedText: String): String? {
        // 공백 제거
        val cleanedText = recognizedText.replace(Regex("\\s+"), "")
        
        // 패턴 매칭
        for (pattern in platePatterns) {
            val match = pattern.find(cleanedText)
            if (match != null) {
                val plateNumber = match.value
                // 공백 제거된 번호판 반환
                return plateNumber
            }
        }
        
        // 패턴 매칭 실패 시 원본 텍스트에서 공백만 제거하여 반환 (후처리 가능)
        return if (cleanedText.length in 6..8) cleanedText else null
    }

    /**
     * OCR 오인식을 보정합니다.
     * 
     * @param text 원본 텍스트
     * @return 보정된 텍스트
     */
    fun correctCommonMistakes(text: String): String {
        return text
            .replace('O', '0')  // O → 0
            .replace('o', '0')  // o → 0
            .replace('I', '1')  // I → 1
            .replace('l', '1')  // l → 1
            .replace('B', '8')  // B → 8
            .replace('D', '0')  // D → 0
            .replace('S', '5')  // S → 5
            .replace('Z', '2')  // Z → 2
            .replace('G', '6')  // G → 6
    }

    /**
     * 번호판 형식이 유효한지 검증합니다.
     * 
     * @param plateNumber 번호판 번호
     * @return 유효하면 true
     */
    fun isValidPlateNumber(plateNumber: String): Boolean {
        val cleaned = plateNumber.replace(Regex("\\s+"), "")
        return platePatterns.any { it.matches(cleaned) }
    }
}

