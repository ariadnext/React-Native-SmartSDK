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

RCT_EXPORT_METHOD(activate:(NSString*)licenceFileName
                  extractData:(BOOL)extractData
                  disableAudioForLiveness:(BOOL)disableAudioForLiveness
                  sdkEnvironment:(NSString*)sdkEnvironment
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    [Idcheckio.shared activateWithLicenseFilename:licenceFileName extractData:extractData
                          disableAudioForLiveness:disableAudioForLiveness
                                   sdkEnvironment:[sdkEnvironment lowercaseString]
                                       onComplete:^(NSException* error){
        if(error == nil){
            resolve(@"");
        } else {
            reject(@"0", error.reason, [NSError errorWithDomain:@"IdcheckioSdk" code:0 userInfo:error.userInfo]);
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
        reject(@"", error.localizedDescription, error);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        IdcheckioViewController *idcheckioViewController = [[IdcheckioViewController alloc] init];
        idcheckioViewController.modalPresentationStyle = UIModalPresentationFullScreen;
        idcheckioViewController.isOnlineSession = false;
        idcheckioViewController.startCompletion = ^(NSError *error) {
            if(error != nil){
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
                });
                reject(@"0", error.localizedDescription, nil);
            }
        };
        [idcheckioViewController setResultCompletion:^(IdcheckioResult *result, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
            });
            if(result != nil){
                captureResolver([IdcheckioObjcUtil resultToJSON:result]);
            } else if(error != nil){
                captureRejecter(@"0", error.localizedDescription, error);
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
        reject(@"", error.localizedDescription, nil);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        IdcheckioViewController *idcheckioViewController = [[IdcheckioViewController alloc] init];
        idcheckioViewController.modalPresentationStyle = UIModalPresentationFullScreen;
        idcheckioViewController.isOnlineSession = true;
        idcheckioViewController.onlineContext = onlineContext;
        idcheckioViewController.startCompletion = ^(NSError *error) {
            if(error != nil){
                reject(@"0", error.localizedDescription, nil);
                dispatch_async(dispatch_get_main_queue(), ^{
                    [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
                });
            }
        };
        [idcheckioViewController setResultCompletion:^(IdcheckioResult *result, NSError *error) {
            dispatch_async(dispatch_get_main_queue(), ^{
                [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
            });
            if(result != nil){
                captureResolver([IdcheckioObjcUtil resultToJSON:result]);
            } else if(error != nil){
                captureRejecter(@"0", error.localizedDescription, error);
            }
        }];
        [[[UIApplication sharedApplication] keyWindow].rootViewController presentViewController:idcheckioViewController animated:true completion:nil];
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
        reject(@"", error.localizedDescription, nil);
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
            [sdkParams setIntegrityCheck: integrity];
        } else if ([key isEqualToString:onlineConfigKey]){
            id onlineConfigParams = [params objectForKey:key];
            OnlineConfig *onlineConfig = [sdkParams onlineConfig];
            onlineConfig.isReferenceDocument = [[onlineConfigParams objectForKey:isReferenceDocumentKey] boolValue];
            [onlineConfig setCheckType:[onlineConfigParams objectForKey:checkTypeKey]];
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
            [Idcheckio.shared.extraParameters setLanguage:[params objectForKey:key]];
        } else if([key isEqualToString:manualButtonTimerKey]){
            Idcheckio.shared.extraParameters.manualButtonTimer = [[params objectForKey:key] doubleValue];
        } else if([key isEqualToString:maxPictureFilesizeKey]){
            [Idcheckio.shared.extraParameters setMaxPictureFilesize:[params objectForKey:key]];
        } else if([key isEqualToString:feedbackLevelKey]){
            [Idcheckio.shared.extraParameters setFeedbackLevel:[params objectForKey:key]];
        } else if([key isEqualToString:confirmAbortKey]){
            Idcheckio.shared.extraParameters.confirmAbort = [[params objectForKey:key] boolValue];
        } else if([key isEqualToString:adjustCropKey]){
            Idcheckio.shared.extraParameters.adjustCrop = [[params objectForKey:key] boolValue];
        } else if([key isEqualToString:sdkEnvironmentKey]){
            Idcheckio.shared.extraParameters.sdkEnvironment = [params objectForKey:key];
        }
    }
    return sdkParams;
}

- (BOOL) getBooleanFromString:(NSString*)string {
    if ([string isEqualToString:@"false"] || [string isEqualToString:@"NO"]){
        return NO;
    }
    return YES;
}

- (void)idcheckioDidSendEventWithInteraction:(enum IdcheckioInteraction)interaction msg:(IdcheckioMsg * _Nullable)msg {}

- (void)idcheckioFinishedWithResult:(IdcheckioResult * _Nullable)result error:(NSError * _Nullable)error {
    dispatch_async(dispatch_get_main_queue(), ^{
        if(result != nil){
            captureResolver([IdcheckioObjcUtil resultToJSON:result]);
        } else if(error != nil){
            captureRejecter(@"0", error.localizedDescription, error);
        }
    });
}

@end
