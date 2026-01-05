#import <Foundation/Foundation.h>

NS_ASSUME_NONNULL_BEGIN

@interface SecureStorageBridge : NSObject

+ (BOOL)saveToken:(NSString *)token;
+ (NSString * _Nullable)loadToken;
+ (void)clearToken;

@end

NS_ASSUME_NONNULL_END
