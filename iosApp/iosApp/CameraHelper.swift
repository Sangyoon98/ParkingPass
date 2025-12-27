import Foundation
import AVFoundation
import Vision
import UIKit

/// 카메라 컨트롤러 및 텍스트 인식
@objc(CameraHelper)
public class CameraHelper: NSObject {
    
    // MARK: - Singleton
    @objc public static let shared = CameraHelper()
    
    private var captureSession: AVCaptureSession?
    private var photoOutput: AVCapturePhotoOutput?
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private let sessionQueue = DispatchQueue(label: "com.parkingpass.camera.session.queue")
    // PhotoCaptureDelegate를 강하게 유지하기 위한 임시 저장소
    private var currentPhotoCaptureDelegate: PhotoCaptureDelegate?
    
    // MARK: - Permission
    
    /// 카메라 권한 상태 확인
    @objc public static func hasPermission() -> Bool {
        return AVCaptureDevice.authorizationStatus(for: .video) == .authorized
    }
    
    /// 카메라 권한 요청
    @objc(requestPermissionWithCompletion:) public static func requestPermission(completion: @escaping (Bool) -> Void) {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            completion(true)
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { granted in
                DispatchQueue.main.async {
                    completion(granted)
                }
            }
        default:
            completion(false)
        }
    }
    
    // MARK: - Camera Setup
    
    /// 카메라 세션 설정 및 PreviewView 반환
    @objc(setupCameraWithCompletion:) public func setupCamera(completion: @escaping (PreviewView?) -> Void) {
        NSLog("setupCamera called")
        // 세션 큐에서 모든 작업 수행
        sessionQueue.async { [weak self] in
            guard let self = self else {
                DispatchQueue.main.async {
                    completion(nil)
                }
                return
            }
            
            // 기존 세션이 실행 중이면 먼저 중지
            if let existingSession = self.captureSession, existingSession.isRunning {
                NSLog("Stopping existing session before setup")
                existingSession.stopRunning()
            }
            
            // 기존 세션 정리 (sessionQueue에서 안전하게)
            NSLog("Cleaning up existing session - photoOutput before: \(self.photoOutput != nil), session before: \(self.captureSession != nil)")
            self.captureSession = nil
            self.photoOutput = nil
            NSLog("After cleanup - photoOutput: \(self.photoOutput != nil), session: \(self.captureSession != nil)")
            DispatchQueue.main.async {
                self.previewLayer = nil
            }
            
            let session = AVCaptureSession()
            session.sessionPreset = .photo
            NSLog("Created new AVCaptureSession")
            
            guard let videoDevice = AVCaptureDevice.default(.builtInWideAngleCamera, for: .video, position: .back) else {
                DispatchQueue.main.async {
                    completion(nil)
                }
                return
            }
            
            do {
                let videoInput = try AVCaptureDeviceInput(device: videoDevice)
                
                // 세션 설정 시작
                session.beginConfiguration()
                
                if session.canAddInput(videoInput) {
                    session.addInput(videoInput)
                } else {
                    session.commitConfiguration()
                    DispatchQueue.main.async {
                        completion(nil)
                    }
                    return
                }
                
                let output = AVCapturePhotoOutput()
                if session.canAddOutput(output) {
                    session.addOutput(output)
                    // photoOutput을 세션 큐에서 설정 (스레드 안전)
                    self.photoOutput = output
                    NSLog("photoOutput set: \(self.photoOutput != nil)")
                } else {
                    NSLog("ERROR: cannot add photoOutput to session")
                    // output 추가 실패 시 에러 처리
                    session.commitConfiguration()
                    DispatchQueue.main.async {
                        completion(nil)
                    }
                    return
                }
                
                // 세션 설정 완료
                session.commitConfiguration()
                
                // 세션 저장 및 photoOutput 저장 (중요: 같은 sessionQueue에서)
                self.captureSession = session
                // photoOutput은 이미 위에서 설정됨 (line 96)
                
                // photoOutput이 nil이 아니라는 것을 확인 (sessionQueue에서 확인)
                NSLog("Before guard - photoOutput: \(self.photoOutput != nil)")
                guard let photoOutput = self.photoOutput else {
                    NSLog("ERROR: photoOutput is nil after setting up session!")
                    DispatchQueue.main.async {
                        completion(nil)
                    }
                    return
                }
                NSLog("After guard - photoOutput exists: true")
                NSLog("Before startRunning - photoOutput exists: \(photoOutput != nil), captureSession exists: \(self.captureSession != nil)")
                
                // 세션 시작 (sessionQueue에서 - 블로킹 호출이므로 완료될 때까지 대기)
                session.startRunning()
                
                NSLog("After startRunning - photoOutput exists: \(self.photoOutput != nil), captureSession exists: \(self.captureSession != nil), isRunning: \(session.isRunning)")
                
                // PreviewView 생성 및 세션 설정 (메인 스레드에서 - Apple 권장사항)
                DispatchQueue.main.async { [weak self] in
                    guard let self = self else {
                        completion(nil)
                        return
                    }
                    let previewView = PreviewView()
                    previewView.backgroundColor = .black
                    if let previewLayer = previewView.previewLayer {
                        previewLayer.session = session
                        previewLayer.videoGravity = .resizeAspectFill
                        self.previewLayer = previewLayer
                    }
                    NSLog("Camera setup completed - photoOutput: \(self.photoOutput != nil), captureSession: \(self.captureSession != nil)")
                    completion(previewView)
                }
            } catch {
                DispatchQueue.main.async {
                    completion(nil)
                }
            }
        }
    }
    
    /// 카메라 세션 중지
    @objc public func stopCamera() {
        NSLog("stopCamera called")
        sessionQueue.async { [weak self] in
            guard let self = self else { return }
            NSLog("stopCamera executing - photoOutput before: \(self.photoOutput != nil), session before: \(self.captureSession != nil)")
            self.captureSession?.stopRunning()
            self.captureSession = nil
            self.photoOutput = nil
            NSLog("stopCamera executed - photoOutput after: \(self.photoOutput != nil)")
            DispatchQueue.main.async {
                self.previewLayer = nil
            }
        }
    }
    
    // MARK: - Capture Image
    
    /// 사진 촬영
    @objc(capturePhotoWithCompletion:) public func capturePhoto(completion: @escaping (Data?, Error?) -> Void) {
        sessionQueue.async { [weak self] in
            guard let self = self else {
                NSLog("ERROR: CameraHelper is nil in capturePhoto")
                DispatchQueue.main.async {
                    completion(nil, NSError(domain: "CameraHelper", code: -1, userInfo: [NSLocalizedDescriptionKey: "CameraHelper is nil"]))
                }
                return
            }
            
            NSLog("capturePhoto called - photoOutput: \(self.photoOutput != nil), session: \(self.captureSession != nil), isRunning: \(self.captureSession?.isRunning ?? false)")
            
            // photoOutput을 먼저 확인 (sessionQueue에서 안전하게 접근)
            let photoOutput = self.photoOutput
            guard let output = photoOutput else {
                NSLog("ERROR: photoOutput is nil in capturePhoto")
                DispatchQueue.main.async {
                    completion(nil, NSError(domain: "CameraHelper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Photo output not available"]))
                }
                return
            }
            
            guard let captureSession = self.captureSession, captureSession.isRunning else {
                NSLog("ERROR: Capture session is not running in capturePhoto")
                DispatchQueue.main.async {
                    completion(nil, NSError(domain: "CameraHelper", code: -2, userInfo: [NSLocalizedDescriptionKey: "Capture session is not running"]))
                }
                return
            }
            
            // PhotoCaptureDelegate를 strong reference로 유지하기 위해 인스턴스 변수로 저장
            // (delegate가 완료되기 전에 해제되지 않도록)
            let delegate = PhotoCaptureDelegate { [weak self] imageData, error in
                // completion 호출 후 delegate 해제
                completion(imageData, error)
                DispatchQueue.main.async {
                    self?.currentPhotoCaptureDelegate = nil
                }
            }
            // 강한 참조 유지를 위해 임시로 저장 (completion이 호출될 때까지 유지됨)
            self.currentPhotoCaptureDelegate = delegate
            
            let settings = AVCapturePhotoSettings()
            NSLog("📸 [CameraHelper] capturePhoto 호출 - settings 생성 완료")
            output.capturePhoto(with: settings, delegate: delegate)
            NSLog("📸 [CameraHelper] capturePhoto 호출 완료 - delegate 설정됨")
        }
    }
    
    // MARK: - Text Recognition
    
    /// 이미지에서 텍스트 인식
    @objc(recognizeTextWithImageData:completion:) public static func recognizeText(_ imageData: Data, completion: @escaping (String?, Float, Error?) -> Void) {
        guard let image = UIImage(data: imageData) else {
            completion(nil, 0, NSError(domain: "CameraHelper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to create image"]))
            return
        }
        
        guard let cgImage = image.cgImage else {
            completion(nil, 0, NSError(domain: "CameraHelper", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to get CGImage"]))
            return
        }
        
        let request = VNRecognizeTextRequest { request, error in
            if let error = error {
                completion(nil, 0, error)
                return
            }
            
            guard let observations = request.results as? [VNRecognizedTextObservation] else {
                completion(nil, 0, NSError(domain: "CameraHelper", code: -1, userInfo: [NSLocalizedDescriptionKey: "No text found"]))
                return
            }
            
            var recognizedStrings: [String] = []
            var maxConfidence: Float = 0
            
            for observation in observations {
                guard let topCandidate = observation.topCandidates(1).first else { continue }
                recognizedStrings.append(topCandidate.string)
                maxConfidence = max(maxConfidence, topCandidate.confidence)
            }
            
            let fullText = recognizedStrings.joined(separator: " ")
            completion(fullText, maxConfidence, nil)
        }
        
        request.recognitionLanguages = ["ko-KR", "en-US"]
        request.recognitionLevel = .accurate
        
        let handler = VNImageRequestHandler(cgImage: cgImage, options: [:])
        do {
            try handler.perform([request])
        } catch {
            completion(nil, 0, error)
        }
    }
}

// MARK: - PreviewView

/// AVCaptureVideoPreviewLayer를 layer로 사용하는 UIView
@objc(PreviewView)
public class PreviewView: UIView {
    public override class var layerClass: AnyClass {
        return AVCaptureVideoPreviewLayer.self
    }
    
    public var previewLayer: AVCaptureVideoPreviewLayer? {
        return layer as? AVCaptureVideoPreviewLayer
    }
}

// MARK: - Photo Capture Delegate

private class PhotoCaptureDelegate: NSObject, AVCapturePhotoCaptureDelegate {
    private let completion: (Data?, Error?) -> Void
    
    init(completion: @escaping (Data?, Error?) -> Void) {
        self.completion = completion
        super.init()
        NSLog("📸 [PhotoCaptureDelegate] 초기화")
    }
    
    func photoOutput(_ output: AVCapturePhotoOutput, didFinishProcessingPhoto photo: AVCapturePhoto, error: Error?) {
        NSLog("📸 [PhotoCaptureDelegate] didFinishProcessingPhoto 호출됨 - error: \(error?.localizedDescription ?? "nil")")
        
        if let error = error {
            NSLog("❌ [PhotoCaptureDelegate] 에러 발생: \(error.localizedDescription)")
            DispatchQueue.main.async { [completion] in
                completion(nil, error)
            }
            return
        }
        
        guard let imageData = photo.fileDataRepresentation() else {
            NSLog("❌ [PhotoCaptureDelegate] 이미지 데이터를 가져올 수 없음")
            DispatchQueue.main.async { [completion] in
                completion(nil, NSError(domain: "PhotoCaptureDelegate", code: -1, userInfo: [NSLocalizedDescriptionKey: "Failed to get image data"]))
            }
            return
        }
        
        NSLog("✅ [PhotoCaptureDelegate] 이미지 데이터 성공 - 크기: \(imageData.count) bytes")
        DispatchQueue.main.async { [completion] in
            completion(imageData, nil)
        }
    }
}
