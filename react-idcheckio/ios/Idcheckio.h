#if __has_include("RCTBridgeModule.h")
#import "RCTBridgeModule.h"
#else
#import <React/RCTBridgeModule.h>
#endif

#import <IDCheckIOSDK/IDCheckIOSDK-Swift.h>

@interface IdcheckioModule : NSObject <RCTBridgeModule, IdcheckioDelegate>

@end
