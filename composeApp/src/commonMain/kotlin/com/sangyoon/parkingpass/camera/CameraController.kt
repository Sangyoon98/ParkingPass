package com.sangyoon.parkingpass.camera

/**
 * 플랫폼별 카메라 컨트롤러 인터페이스
 */
expect class CameraController {
    /**
     * 카메라 권한 요청
     * @return 권한이 허용되면 true
     */
    suspend fun requestPermission(): Boolean

    /**
     * 카메라 권한 확인
     * @return 권한이 허용되어 있으면 true
     */
    fun hasPermission(): Boolean

    /**
     * 카메라 시작
     */
    fun startCamera()

    /**
     * 카메라 중지
     */
    fun stopCamera()

    /**
     * 사진 촬영
     * @return 캡처된 이미지 (실패 시 null)
     */
    suspend fun captureImage(): CameraImage?
}

/**
 * 플랫폼별 CameraController 인스턴스 생성
 */
expect fun createCameraController(context: Any?): CameraController

