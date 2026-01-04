#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface KakaoLoginBridge : NSObject

+ (void)loginWithCompletion:(void (^)(NSString * _Nullable token, BOOL cancelled, NSString * _Nullable errorMessage))completion;
+ (BOOL)isKakaoTalkLoginAvailable;

@end

NS_ASSUME_NONNULL_END
