package com.sangyoon.parkingpass.recognition

/**
 * 번호판 번호 추출 및 검증 유틸리티
 */
object PlateNumberExtractor {
    /**
     * 한국 번호판에서 허용되는 한글 문자
     * - 일반 차량: 가, 나, 다, 라, 마, 거, 너, 더, 러, 머, 버, 서, 어, 저, 고, 노, 도, 로, 모, 보, 소, 오, 조, 구, 누, 두, 루, 무, 부, 수, 우, 주 (32개)
     * - 특수 용도: 허, 하, 호 (렌터카), 아, 바, 사, 자 (택시), 배 (배달)
     */
    private val allowedKoreanChars = setOf(
        // 일반 차량 (32개)
        "가", "나", "다", "라", "마",
        "거", "너", "더", "러", "머", "버", "서", "어", "저",
        "고", "노", "도", "로", "모", "보", "소", "오", "조",
        "구", "누", "두", "루", "무", "부", "수", "우", "주",
        // 특수 용도
        "허", "하", "호",  // 렌터카
        "아", "바", "사", "자",  // 택시
        "배"  // 배달
    )
    
    /**
     * 허용된 한글 문자들을 정규식 문자 클래스로 변환
     */
    private val allowedKoreanCharsPattern = allowedKoreanChars.joinToString("")
    
    private const val separatorPattern = "[-\\s]*"
    private val whitespaceOrHyphen = Regex("[-\\s]+")

    private val standardPlatePattern =
        Regex("\\d{2,3}$separatorPattern[$allowedKoreanCharsPattern]$separatorPattern\\d{4}")
    private val temporaryPlatePattern = Regex("임${separatorPattern}\\d{4}")

    /**
     * 인식된 텍스트에서 번호판 번호를 추출합니다.
     * 
     * @param recognizedText OCR로 인식된 텍스트
     * @return 추출된 번호판 번호 (없으면 null)
     */
    fun extractPlateNumber(recognizedText: String): String? {
        val normalizedText = recognizedText.replace("\n", " ")

        standardPlatePattern.find(normalizedText)?.let { match ->
            val candidate = match.value.replace(whitespaceOrHyphen, "")
            if (isValidPlateNumber(candidate)) {
                return candidate
            }
        }

        temporaryPlatePattern.find(normalizedText)?.let { match ->
            val candidate = match.value.replace(whitespaceOrHyphen, "")
            if (isValidPlateNumber(candidate)) {
                return candidate
            }
        }

        // 패턴 매칭 실패 시 기본 형식 검증을 수행하여 반환
        // 최소한의 검증: 길이, 숫자와 허용된 한글 문자만 포함, 형식(숫자+한글+숫자) 또는 임시 번호판
        val cleanedText = normalizedText.replace(whitespaceOrHyphen, "")
        if (cleanedText.length !in 5..8) {
            return null
        }

        return if (isValidPlateNumber(cleanedText)) cleanedText else null
    }

    /**
     * OCR 오인식을 보정합니다.
     * 한글 문자는 그대로 유지하고, 숫자로 오인식될 수 있는 영문자만 치환합니다.
     * 
     * @param text 원본 텍스트
     * @return 보정된 텍스트
     */
    fun correctCommonMistakes(text: String): String {
        return text.map { char ->
            // 한글 문자는 그대로 유지 (U+AC00 ~ U+D7A3)
            if (char.code in 0xAC00..0xD7A3) {
                char
            } else {
                // 한글이 아닌 경우에만 OCR 오인식 보정 적용
                when (char) {
                    'O', 'o' -> '0'  // O → 0
                    'I', 'l' -> '1'  // I → 1
                    'B' -> '8'       // B → 8
                    'D' -> '0'       // D → 0
                    'S' -> '5'       // S → 5
                    'Z' -> '2'       // Z → 2
                    'G' -> '6'       // G → 6
                    else -> char     // 기타 문자는 그대로 유지
                }
            }
        }.joinToString("")
    }

    /**
     * 번호판 형식이 유효한지 검증합니다.
     * 패턴 검증과 함께 한글 문자가 허용된 문자인지 확인합니다.
     * 
     * @param plateNumber 번호판 번호
     * @return 유효하면 true
     */
    fun isValidPlateNumber(plateNumber: String): Boolean {
        val cleaned = plateNumber.replace(whitespaceOrHyphen, "")

        if (temporaryPlatePattern.matches(cleaned)) {
            return cleaned.length == 5 &&
                cleaned.firstOrNull() == '임' &&
                cleaned.drop(1).all { it.isDigit() }
        }

        if (!standardPlatePattern.matches(cleaned)) {
            return false
        }

        val koreanChars = cleaned.filter { it.code in 0xAC00..0xD7A3 }
        if (koreanChars.isEmpty()) {
            return false
        }

        return koreanChars.all { it.toString() in allowedKoreanChars }
    }
}
