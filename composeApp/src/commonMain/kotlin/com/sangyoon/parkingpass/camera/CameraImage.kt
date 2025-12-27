package com.sangyoon.parkingpass.camera

/**
 * 카메라로 캡처한 이미지 데이터
 */
data class CameraImage(
    /** 이미지 바이트 배열 */
    val bytes: ByteArray,
    /** 이미지 형식 (JPEG, PNG 등) */
    val format: ImageFormat = ImageFormat.JPEG
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CameraImage

        if (!bytes.contentEquals(other.bytes)) return false
        if (format != other.format) return false

        return true
    }

    override fun hashCode(): Int {
        var result = bytes.contentHashCode()
        result = 31 * result + format.hashCode()
        return result
    }
}

/**
 * 이미지 형식
 */
enum class ImageFormat {
    JPEG,
    PNG
}

