import Foundation
import KakaoSDKCommon
import KakaoSDKAuth
import KakaoSDKUser

@objc(KakaoLoginBridge)
public class KakaoLoginBridge: NSObject {

    @objc(loginWithCompletion:)
    public static func loginWithCompletion(_ completion: @escaping (NSString?, Bool, NSString?) -> Void) {
        let handler: (OAuthToken?, Error?) -> Void = { token, error in
            if let token = token {
                completion(token.accessToken as NSString?, false, nil)
                return
            }

            if let sdkError = error as? SdkError,
               case .ClientFailed(reason: .Cancelled, _) = sdkError {
                completion(nil, true, nil)
                return
            }

            completion(nil, false, error?.localizedDescription as NSString?)
        }

        if UserApi.isKakaoTalkLoginAvailable() {
            UserApi.shared.loginWithKakaoTalk(completion: handler)
        } else {
            UserApi.shared.loginWithKakaoAccount(completion: handler)
        }
    }

    @objc public static func isKakaoTalkLoginAvailable() -> Bool {
        return UserApi.isKakaoTalkLoginAvailable()
    }
}
