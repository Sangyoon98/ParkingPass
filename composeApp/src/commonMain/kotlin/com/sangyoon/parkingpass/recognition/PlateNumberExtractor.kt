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
    
    /**
     * 한국 번호판 패턴 (허용된 한글 문자만 사용)
     */
    private val platePatterns = listOf(
        // 구형: 12가3456, 01가1234
        Regex("\\d{2}[$allowedKoreanCharsPattern]\\d{4}"),
        // 신형: 123가4567, 001가1234
        Regex("\\d{3}[$allowedKoreanCharsPattern]\\d{4}"),
        // 구형 (공백 포함): 12 가 3456
        Regex("\\d{2}\\s*[$allowedKoreanCharsPattern]\\s*\\d{4}"),
        // 신형 (공백 포함): 123 가 4567
        Regex("\\d{3}\\s*[$allowedKoreanCharsPattern]\\s*\\d{4}")
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
                val plateNumber = match.value.replace(Regex("\\s+"), "") // 공백 제거
                // 허용된 한글 문자 확인
                val koreanChars = plateNumber.filter { it.code in 0xAC00..0xD7A3 }
                if (koreanChars.isNotEmpty() && koreanChars.all { it.toString() in allowedKoreanChars }) {
                    return plateNumber
                }
            }
        }
        
        // 패턴 매칭 실패 시 기본 형식 검증을 수행하여 반환
        // 최소한의 검증: 길이, 숫자와 허용된 한글 문자만 포함, 형식(숫자+한글+숫자)
        if (cleanedText.length !in 7..8) {
            return null
        }
        
        // 허용된 문자만 포함되어 있는지 확인 (숫자 + 허용된 한글 문자)
        val containsOnlyAllowedChars = cleanedText.all { char ->
            char.isDigit() || (char.code in 0xAC00..0xD7A3 && char.toString() in allowedKoreanChars)
        }
        if (!containsOnlyAllowedChars) {
            return null
        }
        
        // 한글 문자가 포함되어 있는지 확인
        val koreanChars = cleanedText.filter { it.code in 0xAC00..0xD7A3 }
        if (koreanChars.isEmpty()) {
            return null
        }
        
        // 기본 형식 검증: 숫자로 시작하고 끝나야 함
        if (!cleanedText.first().isDigit() || !cleanedText.last().isDigit()) {
            return null
        }
        
        // 한글 문자가 허용된 문자 집합에 포함되는지 확인
        if (!koreanChars.all { it.toString() in allowedKoreanChars }) {
            return null
        }
        
        return cleanedText
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
        val cleaned = plateNumber.replace(Regex("\\s+"), "")
        
        // 패턴 검증
        val matchesPattern = platePatterns.any { it.matches(cleaned) }
        if (!matchesPattern) {
            return false
        }
        
        // 한글 문자가 허용된 문자 집합에 포함되는지 추가 검증
        // 정규식으로 이미 필터링되지만 이중 검증을 위해 확인
        val koreanChars = cleaned.filter { it.code in 0xAC00..0xD7A3 } // 한글 유니코드 범위
        if (koreanChars.isEmpty()) {
            return false
        }
        
        // 각 한글 문자가 허용된 문자 집합에 포함되는지 확인
        return koreanChars.all { it.toString() in allowedKoreanChars }
    }
}

