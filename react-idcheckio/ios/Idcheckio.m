#import "Idcheckio.h"
#import "IdcheckioKeys.h"
#import <ReactIdcheckio/ReactIdcheckio-Swift.h>

@implementation IdcheckioModule

RCTPromiseResolveBlock captureResolver;
RCTPromiseRejectBlock captureRejecter;

RCT_EXPORT_MODULE(IdcheckioModule);

RCT_EXPORT_METHOD(preload:(BOOL)extractData){
    [Idcheckio.shared preloadWithExtractData:extractData];
}

RCT_EXPORT_METHOD(activate:(NSString*)idToken
                  extractData:(BOOL)extractData
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    [Idcheckio.shared activateWithToken:idToken extractData:extractData onComplete:^(NSError* error){
        if(error == nil){
            resolve(@"");
        } else {
            reject(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
        }
    }];
}

RCT_EXPORT_METHOD(start:(NSDictionary*)params
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    captureResolver = resolve;
    captureRejecter = reject;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];

    NSError* error;
    [Idcheckio.shared setParams:sdkParams error:&error];
    if(error != nil){
        reject(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        IdcheckioViewController *idcheckioViewController = [[IdcheckioViewController alloc] init];
        idcheckioViewController.modalPresentationStyle = UIModalPresentationFullScreen;
        idcheckioViewController.isOnlineSession = false;
        [idcheckioViewController setResultCompletion:^(IdcheckioResult *result, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
            });
            if(result != nil){
                captureResolver([IdcheckioObjcUtil resultToJSON:result]);
            } else if(error != nil){
                captureRejecter(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
            }
        }];
        [[[UIApplication sharedApplication] keyWindow].rootViewController presentViewController:idcheckioViewController animated:true completion:nil];
    });
}

RCT_EXPORT_METHOD(startOnline:(NSDictionary*)params
                  cis:(NSDictionary*)cis
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    captureResolver = resolve;
    captureRejecter = reject;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];
    OnlineContext* onlineContext = [self getOnlineContextFromJson:cis];
    NSError* error;
    [Idcheckio.shared setParams:sdkParams error:&error];
    if(error != nil){
        reject(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        IdcheckioViewController *idcheckioViewController = [[IdcheckioViewController alloc] init];
        idcheckioViewController.modalPresentationStyle = UIModalPresentationFullScreen;
        idcheckioViewController.isOnlineSession = true;
        idcheckioViewController.onlineContext = onlineContext;
        [idcheckioViewController setResultCompletion:^(IdcheckioResult *result, NSError *error) {
           dispatch_async(dispatch_get_main_queue(), ^{
               [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{
                   dispatch_async(dispatch_get_main_queue(), ^{
                       if(result != nil){
                           captureResolver([IdcheckioObjcUtil resultToJSON:result]);
                       } else if(error != nil){
                           captureRejecter(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
                       }
                   });
               }];
           });
        }];
        [[[UIApplication sharedApplication] keyWindow].rootViewController presentViewController:idcheckioViewController animated:true completion:nil];
    });
}

RCT_EXPORT_METHOD(startIps:(NSString*)folderUid
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    if([folderUid length] == 0){
        reject(@"0", [IdcheckioObjcUtil missingFolderUid], nil);
        return;
    }
    IpsTheme *ipsTheme = [[IpsTheme alloc] init];
    [ipsTheme setOrientation:@"automatic"];
    dispatch_async(dispatch_get_main_queue(), ^{
        [Idcheckio startIpsWith:folderUid from:[[UIApplication sharedApplication] keyWindow].rootViewController ipsTheme:ipsTheme token:nil completion:^(NSError* error){
            if(error != nil) {
                reject(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
            } else {
                resolve(@"{}");
            }
        }];
    });
}

RCT_EXPORT_METHOD(analyze:(NSDictionary*)params
                  side1Image:(NSString*)side1Image
                  side2Image:(NSString*)side2Image
                  online:(BOOL)online
                  context:(NSDictionary*)context
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    captureResolver = resolve;
    captureRejecter = reject;
    NSError* error;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];
    OnlineContext* onlineContext = [self getOnlineContextFromJson:context];
    [Idcheckio.shared setParams:sdkParams error:&error];
    if(error != nil){
        reject(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
    }
    Idcheckio.shared.delegate = self;
    NSURL *url1 = [NSURL URLWithString:[side1Image stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
    NSURL *url2 = [NSURL URLWithString:[side2Image stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
    UIImage *side1 = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:url1]];
    UIImage *side2 = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:url2]];
    [Idcheckio.shared analyzeWithSide1Image:side1 side2Image:side2 online:online onlineContext:onlineContext];
}

- (OnlineContext*) getOnlineContextFromJson:(NSDictionary*)cis{
    if (cis == nil) {
        return nil;
    }
    NSError * err;
    NSData * jsonData = [NSJSONSerialization  dataWithJSONObject:cis options:0 error:&err];
    NSString * myString = [[NSString alloc] initWithData:jsonData   encoding:NSUTF8StringEncoding];
    if (err == nil) {
        return [OnlineContext fromJson:myString];
    }
    return nil;
}

- (SDKParams*) getParamsFromDictionnary:(NSDictionary*)params {
    SDKParams* sdkParams = [[SDKParams alloc] init];
    SDKExtraParams* extraParams = [[SDKExtraParams alloc] init];
    Theme* theme = [[Theme alloc] init];
    for(NSString* key in [params allKeys]){
        if([key isEqualToString:docTypeKey]){
            [sdkParams setDocumentType:[params objectForKey:key]];
        } else if ([key isEqualToString:confirmationTypeKey]) {
            [sdkParams setConfirmType:[params objectForKey:key]];
        } else if ([key isEqualToString:sideOneExtractionKey]) {
            id paramsSide1 = [params objectForKey:key];
            Extraction* extractSide1 = [[Extraction alloc] init];
            [extractSide1 setFace:[paramsSide1 objectForKey:faceDetectionKey]];
            [extractSide1 setCodeline:[paramsSide1 objectForKey:codelineKey]];
            [sdkParams setSide1Extraction:extractSide1];
        } else if ([key isEqualToString:sideTwoExtractionKey]) {
            id paramsSide2 = [params objectForKey:key];
            Extraction* extractSide2 = [[Extraction alloc] init];
            [extractSide2 setFace:[paramsSide2 objectForKey:faceDetectionKey]];
            [extractSide2 setCodeline:[paramsSide2 objectForKey:codelineKey]];
            [sdkParams setSide2Extraction:extractSide2];
        } else if ([key isEqualToString:integrityCheckKey]) {
            id paramIntegrityCheck = [params objectForKey:key];
            IntegrityCheck *integrity = [[IntegrityCheck alloc] init];
            integrity.readEmrtd = [paramIntegrityCheck objectForKey:readEmrtdKey];
            integrity.docLiveness = [paramIntegrityCheck objectForKey:docLivenessKey];
            [sdkParams setIntegrityCheck: integrity];
        } else if ([key isEqualToString:onlineConfigKey]){
            id onlineConfigParams = [params objectForKey:key];
            OnlineConfig *onlineConfig = [sdkParams onlineConfig];
            onlineConfig.isReferenceDocument = [[onlineConfigParams objectForKey:isReferenceDocumentKey] boolValue];
            if ([onlineConfigParams objectForKey:cisTypeKey] != [NSNull null]) {
                [onlineConfig setCisType:[onlineConfigParams objectForKey:cisTypeKey]];
            }
            if ([onlineConfigParams objectForKey:folderUidKey] != [NSNull null]) {
                onlineConfig.folderUid = [onlineConfigParams objectForKey:folderUidKey];
            }
            if ([onlineConfigParams objectForKey:biometricConsentKey] != [NSNull null]) {
                [onlineConfig setBiometricConsentWithBiometricConsent:[[onlineConfigParams objectForKey:biometricConsentKey] boolValue]];
            }
            onlineConfig.enableManualAnalysis = [[onlineConfigParams objectForKey:enableManualAnalysisKey] boolValue];
        } else if ([key isEqualToString:scanBothSidesKey]) {
            [sdkParams setScanBothSides:[params objectForKey:key]];
        } else if ([key isEqualToString:useHdKey]) {
            [sdkParams setUseHD: [[params objectForKey:key] boolValue]];
        } else if([key isEqualToString:languageKey]){
            [extraParams setLanguage:[params objectForKey:key]];
        } else if([key isEqualToString:manualButtonTimerKey]){
            [extraParams setManualButtonTimer:[[params objectForKey:key] doubleValue]];
        } else if([key isEqualToString:maxPictureFilesizeKey]){
            [extraParams setMaxPictureFilesize:[params objectForKey:key]];
        } else if([key isEqualToString:feedbackLevelKey]){
            [extraParams setFeedbackLevel:[params objectForKey:key]];
        } else if([key isEqualToString:confirmAbortKey]){
            [extraParams setConfirmAbort:[[params objectForKey:key] boolValue]];
        } else if([key isEqualToString:adjustCropKey]){
            [extraParams setAdjustCrop:[[params objectForKey:key] boolValue]];
        } else if([key isEqualToString:captureModeKey]){
            [extraParams setCaptureMode:[params objectForKey:key]];
        } else if([key isEqualToString:themeKey]){
            id themeParams = [params objectForKey:key];
            if ([themeParams objectForKey:primaryColorKey] != [NSNull null]) {
                [theme setPrimaryColor:[self colorWithHexString:[themeParams objectForKey:primaryColorKey]]];
            }
            if ([themeParams objectForKey:foregroundColorKey] != [NSNull null]) {
                [theme setForegroundColor:[self colorWithHexString:[themeParams objectForKey:foregroundColorKey]]];
            }
            if ([themeParams objectForKey:backgroundColorKey] != [NSNull null]) {
                [theme setBackgroundColor:[self colorWithHexString:[themeParams objectForKey:backgroundColorKey]]];
            }
            if ([themeParams objectForKey:borderColorKey] != [NSNull null]) {
                [theme setBorderColor:[self colorWithHexString:[themeParams objectForKey:borderColorKey]]];
            }
            if ([themeParams objectForKey:textColorKey] != [NSNull null]) {
                [theme setTextColor:[self colorWithHexString:[themeParams objectForKey:textColorKey]]];
            }
            if ([themeParams objectForKey:titleColorKey] != [NSNull null]) {
                [theme setTitleColor:[self colorWithHexString:[themeParams objectForKey:titleColorKey]]];
            }
        }
        [Idcheckio.shared setExtraParams:extraParams error:nil];
        [Idcheckio.shared setTheme:theme];
    }
    return sdkParams;
}

- (BOOL) getBooleanFromString:(NSString*)string {
    if ([string isEqualToString:@"false"] || [string isEqualToString:@"NO"]){
        return NO;
    }
    return YES;
}

- (void)idcheckioFinishedWithResult:(IdcheckioResult * _Nullable)result error:(NSError * _Nullable)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(result != nil){
            captureResolver([IdcheckioObjcUtil resultToJSON:result]);
        } else if(error != nil){
            captureRejecter(@"0", [IdcheckioObjcUtil getErrorJson:error], error);
        }
    });
}

- (UIColor *) colorWithHexString: (NSString *) hexString {
    NSString *colorString = [[hexString stringByReplacingOccurrencesOfString: @"#" withString: @""] uppercaseString];
    CGFloat alpha, red, blue, green;
    switch ([colorString length]) {
        case 3: // #RGB
            alpha = 1.0f;
            red   = [self colorComponentFrom: colorString start: 0 length: 1];
            green = [self colorComponentFrom: colorString start: 1 length: 1];
            blue  = [self colorComponentFrom: colorString start: 2 length: 1];
            break;
        case 4: // #ARGB
            alpha = [self colorComponentFrom: colorString start: 0 length: 1];
            red   = [self colorComponentFrom: colorString start: 1 length: 1];
            green = [self colorComponentFrom: colorString start: 2 length: 1];
            blue  = [self colorComponentFrom: colorString start: 3 length: 1];
            break;
        case 6: // #RRGGBB
            alpha = 1.0f;
            red   = [self colorComponentFrom: colorString start: 0 length: 2];
            green = [self colorComponentFrom: colorString start: 2 length: 2];
            blue  = [self colorComponentFrom: colorString start: 4 length: 2];
            break;
        case 8: // #AARRGGBB
            alpha = [self colorComponentFrom: colorString start: 0 length: 2];
            red   = [self colorComponentFrom: colorString start: 2 length: 2];
            green = [self colorComponentFrom: colorString start: 4 length: 2];
            blue  = [self colorComponentFrom: colorString start: 6 length: 2];
            break;
        default:
            [NSException raise:@"Invalid color value" format: @"Color value %@ is invalid.  It should be a hex value of the form #RBG, #ARGB, #RRGGBB, or #AARRGGBB", hexString];
            break;
    }
    return [UIColor colorWithRed: red green: green blue: blue alpha: alpha];
}

- (CGFloat) colorComponentFrom: (NSString *) string start: (NSUInteger) start length: (NSUInteger) length {
    NSString *substring = [string substringWithRange: NSMakeRange(start, length)];
    NSString *fullHex = length == 2 ? substring : [NSString stringWithFormat: @"%@%@", substring, substring];
    unsigned hexComponent;
    [[NSScanner scannerWithString: fullHex] scanHexInt: &hexComponent];
    return hexComponent / 255.0;
}

@end
