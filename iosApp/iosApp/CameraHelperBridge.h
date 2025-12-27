#import <Foundation/Foundation.h>
#import <AVFoundation/AVFoundation.h>
#import <UIKit/UIKit.h>

NS_ASSUME_NONNULL_BEGIN

@class PreviewView;

@interface PreviewView : UIView
@property (nonatomic, readonly, nullable) AVCaptureVideoPreviewLayer *previewLayer;
@end

@interface CameraHelper : NSObject

+ (BOOL)hasPermission;
+ (void)requestPermissionWithCompletion:(void (^)(BOOL granted))completion;

+ (instancetype)shared;

- (instancetype)init;
- (void)setupCameraWithCompletion:(void (^)(PreviewView * _Nullable view))completion;
- (void)stopCamera;
- (void)capturePhotoWithCompletion:(void (^)(NSData * _Nullable data, NSError * _Nullable error))completion;
+ (void)recognizeTextWithImageData:(NSData *)imageData completion:(void (^)(NSString * _Nullable text, float confidence, NSError * _Nullable error))completion;

@end

NS_ASSUME_NONNULL_END

