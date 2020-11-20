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
                  disableImei:(BOOL)disableImei
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
    Idcheckio.shared.delegate = self;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];

    NSError* error;
    [Idcheckio.shared setParams:sdkParams error:&error];
    if(error != nil){
        reject(@"", error.localizedDescription, error);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *sdkViewController = [[UIViewController alloc] init];
        sdkViewController.modalPresentationStyle = UIModalPresentationFullScreen;
        IdcheckioView *cameraView = [[IdcheckioView alloc] init];

        cameraView.translatesAutoresizingMaskIntoConstraints = false;
        sdkViewController.view.frame = [[UIApplication sharedApplication] keyWindow].rootViewController.view.frame;
        [sdkViewController.view addSubview:cameraView];
        sdkViewController.view.backgroundColor = UIColor.blackColor;
        [[cameraView.leadingAnchor constraintEqualToAnchor:sdkViewController.view.leadingAnchor] setActive:true];
        [[cameraView.trailingAnchor constraintEqualToAnchor:sdkViewController.view.trailingAnchor] setActive:true];
        [[cameraView.topAnchor constraintEqualToAnchor:sdkViewController.view.topAnchor] setActive:true];
        [[cameraView.bottomAnchor constraintEqualToAnchor:sdkViewController.view.bottomAnchor] setActive:true];

        [[[UIApplication sharedApplication] keyWindow].rootViewController presentViewController:sdkViewController animated:true completion:^{
            [Idcheckio.shared startWith:cameraView completion:^(NSError *error) {
                if(error != nil){
                    reject(@"", error.localizedDescription, error);
                }
            }];
        }];
    });
}

RCT_EXPORT_METHOD(startOnline:(NSDictionary*)params
                  cis:(NSDictionary*)cis
                  resolver:(RCTPromiseResolveBlock)resolve
                  rejecter:(RCTPromiseRejectBlock)reject){
    captureResolver = resolve;
    captureRejecter = reject;
    Idcheckio.shared.delegate = self;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];
    CISContext* cisContext = [self getCisContextFromJson:cis];

    NSError* error;
    [Idcheckio.shared setParams:sdkParams error:&error];
    if(error != nil){
        reject(@"", error.localizedDescription, nil);
    }

    dispatch_async(dispatch_get_main_queue(), ^{
        UIViewController *sdkViewController = [[UIViewController alloc] init];
        IdcheckioView *cameraView = [[IdcheckioView alloc] init];

        cameraView.translatesAutoresizingMaskIntoConstraints = false;
        sdkViewController.view.frame = [[UIApplication sharedApplication] keyWindow].rootViewController.view.frame;
        [sdkViewController.view addSubview:cameraView];
        sdkViewController.view.backgroundColor = UIColor.blackColor;
        [[cameraView.leadingAnchor constraintEqualToAnchor:sdkViewController.view.leadingAnchor] setActive:true];
        [[cameraView.trailingAnchor constraintEqualToAnchor:sdkViewController.view.trailingAnchor] setActive:true];
        [[cameraView.topAnchor constraintEqualToAnchor:sdkViewController.view.topAnchor] setActive:true];
        [[cameraView.bottomAnchor constraintEqualToAnchor:sdkViewController.view.bottomAnchor] setActive:true];

        [[[UIApplication sharedApplication] keyWindow].rootViewController presentViewController:sdkViewController animated:true completion:^{
            [Idcheckio.shared startOnlineWith:cameraView cisContext:cisContext  externalAuthenticationDelegate:nil completion:^(NSError *error) {
                if(error != nil){
                    reject(@"0", error.localizedDescription, nil);
                }
            }];
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
    Idcheckio.shared.delegate = self;
    SDKParams* sdkParams = [self getParamsFromDictionnary:params];
    CISContext* cisContext = [self getCisContextFromJson:context];
    NSURL *url1 = [NSURL URLWithString:[side1Image stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
    NSURL *url2 = [NSURL URLWithString:[side2Image stringByAddingPercentEncodingWithAllowedCharacters:[NSCharacterSet URLQueryAllowedCharacterSet]]];
    UIImage *side1 = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:url1]];
    UIImage *side2 = [[UIImage alloc] initWithData:[NSData dataWithContentsOfURL:url2]];
    
    [Idcheckio.shared analyzeWithParams: sdkParams side1Image:side1 side2Image:side2 online:online cisContext:cisContext];

}

- (CISContext*) getCisContextFromJson:(NSDictionary*)cis{
    CISContext* cisContext = [[CISContext alloc] init];
    for(NSString* key in [cis allKeys]){
        if([key isEqualToString:folderUid]){
            [cisContext setFolderUid:[cis objectForKey:key]];
        } else if([key isEqualToString:referenceTaskUid]){
            [cisContext setReferenceTaskUid:[cis objectForKey:key]];
        } else if([key isEqualToString:referenceDocUid]){
            [cisContext setReferenceDocUid:[cis objectForKey:key]];
        }  else if([key isEqualToString:biometricConsent]){
            [cisContext setBiometricConsentWithBiometricConsent:[cis objectForKey:key]];
        }
    }
    return cisContext;
}

- (SDKParams*) getParamsFromDictionnary:(NSDictionary*)params {
    SDKParams* sdkParams = [[SDKParams alloc] init];
    for(NSString* key in [params allKeys]){
        if([key isEqualToString:DocumentType]){
            [sdkParams setDocumentType:[params objectForKey:key]];
        } else if ([key isEqualToString:ConfirmType]) {
            [sdkParams setConfirmType:[params objectForKey:key]];
        } else if ([key isEqualToString:Side1Extraction]) {
            id paramsSide1 = [params objectForKey:key];
            Extraction* extractSide1 = [[Extraction alloc] init];
            [extractSide1 setFace:[paramsSide1 objectForKey:FaceDetection]];
            [extractSide1 setCodeline:[paramsSide1 objectForKey:DataRequirement]];
            [sdkParams setSide1Extraction:extractSide1];
        } else if ([key isEqualToString:Side2Extraction]) {
            id paramsSide2 = [params objectForKey:key];
            Extraction* extractSide2 = [[Extraction alloc] init];
            [extractSide2 setFace:[paramsSide2 objectForKey:FaceDetection]];
            [extractSide2 setCodeline:[paramsSide2 objectForKey:DataRequirement]];
            [sdkParams setSide2Extraction:extractSide2];
        } else if ([key isEqualToString:ScanBothSides]) {
            [sdkParams setScanBothSides:[params objectForKey:key]];
        } else if ([key isEqualToString:UseHD]) {
            [sdkParams setUseHD:[self getBooleanFromString:[params objectForKey:key]]];
        } else if ([key isEqualToString:ExtraParams]) {
            id extraParams = [params objectForKey:key];
            for(NSString* extraKey in [extraParams allKeys]){
                if([extraKey isEqualToString:Language]){
                    Idcheckio.shared.extraParameters.language = [extraParams objectForKey:extraKey];
                } else if([extraKey isEqualToString:ManualButtonTimer]){
                    Idcheckio.shared.extraParameters.manualButtonTimer = [extraParams doubleForKey:extraKey];
                } else if([extraKey isEqualToString:MaxPictureFilesize]){
                    [Idcheckio.shared.extraParameters setMaxPictureFilesize:[extraParams stringForKey:extraKey]];
                } else if([extraKey isEqualToString:FeedbackLevel]){
                    [Idcheckio.shared.extraParameters setFeedbackLevel:[extraParams objectForKey:extraKey]];
                } else if([extraKey isEqualToString:Token]){
                    [Idcheckio.shared.extraParameters setToken:[extraParams objectForKey:extraKey]];
                } else if([extraKey isEqualToString:ConfirmAbort]){
                    Idcheckio.shared.extraParameters.confirmAbort = [[extraParams objectForKey:extraKey] boolValue];
                } else if([extraKey isEqualToString:AdjustCrop]){
                    Idcheckio.shared.extraParameters.adjustCrop = [[extraParams objectForKey:extraKey] boolValue];
                } else if([extraKey isEqualToString:SdkEnvironment]){
                    Idcheckio.shared.extraParameters.sdkEnvironment = [extraParams objectForKey:extraKey];
                }
            }
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
        [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:true completion:^{}];
    });
    if(result != nil){
        captureResolver([IdcheckioObjcUtil resultToJSON:result]);
    } else if(error != nil){
        captureRejecter(@"0", error.localizedDescription, error);
    }
}

@end
