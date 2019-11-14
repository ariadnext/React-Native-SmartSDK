
#import "Smartsdk.h"
#import <SmartsdkKit/AXTCaptureInterface.h>
#import <SmartsdkKit/AXTDocumentType.h>
#import <SmartsdkKit/AXTSdkInit.h>
#import <SmartsdkKit/AXTSdkParams.h>
#import <SmartsdkKit/AXTSdkResult.h>
#import <SmartsdkKit/AXTDocument.h>
#import <SmartsdkKit/AXTDataExtractionRequirement.h>
#import <SmartsdkKit/AXTDocumentValidityResult.h>
#import <SmartsdkKit/AXTDocumentIdentity.h>
#import <SmartsdkKit/AXTDocumentCreditCard.h>
#import <SmartsdkKit/AXTDocumentRegistrationVehicle.h>

#import <React/RCTLog.h>
#import "SmartsdkKeys.h"

@implementation Smartsdk

RCTPromiseResolveBlock captureResolver;
RCTPromiseRejectBlock captureRejecter;

- (dispatch_queue_t)methodQueue
{
    return dispatch_get_main_queue();
}
RCT_EXPORT_MODULE(SmartsdkModule);

RCT_REMAP_METHOD(initSmartSdk,
                 initSmartSdkWithResolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject)
{
    RCTLogInfo(@"Initializing SmartSDK...");
    if ([[AXTCaptureInterface captureInterfaceInstance] sdkIsActivated]) {
        RCTLogInfo(@"SmartSDK already initialized !");
        resolve(@"INIT_SUCCESS");
    } else {
        AXTSdkInit* sdkInit = [AXTSdkInit new];
        [sdkInit setLicenseFilename:@"licence"];
        sdkInit.timeoutActivation = 20;
        
        NSMutableDictionary* extra = [NSMutableDictionary new];
        NSUserDefaults *preferences = [NSUserDefaults standardUserDefaults];
        
        NSString *currentLevelKey = @"apiKey";
        
        if ([preferences objectForKey:currentLevelKey] != nil)
        {
            [extra setObject:[preferences objectForKey:currentLevelKey]  forKey:@"override.apikey"];
        }
        [sdkInit setExtraInformations:extra];
        
        [[AXTCaptureInterface captureInterfaceInstance] initCaptureSdk:sdkInit withCompletion:^(NSArray *result, NSException *error) {
            if (error == nil) {
                RCTLogInfo(@"Initilization successed!");
                resolve(@"INIT_SUCCESS");
            } else {
                RCTLogError(@"Error on initialization : %@", error);
                NSError *err = [NSError errorWithDomain:@"SmartSDK" code:0 userInfo:error.userInfo];
                reject(@"0",[NSString stringWithFormat:@"Error on initialization : %@", error], err);
            }
        }];
    }
}

RCT_EXPORT_METHOD(capture:(NSDictionary*)params
                 resolver:(RCTPromiseResolveBlock)resolve
                 rejecter:(RCTPromiseRejectBlock)reject) {    
    captureResolver = resolve;
    captureRejecter = reject;
    
    AXTSdkParams* sdkParams = [[AXTSdkParams alloc]init];
    [sdkParams setDoctype:[Smartsdk docTypeFromString:(NSString*)params[@"DOCUMENT_TYPE"]]];
    [sdkParams setExtractData:(BOOL)params[AXTSdkParameters(EXTRACT_DATA)]];
    [sdkParams setScanBothSide:(BOOL)params[@"SCAN_RECTO_VERSO"]];
    [sdkParams setDisplayResult:(BOOL)params[@"DISPLAY_CATPURE"]];
    [sdkParams setUseHD:(BOOL)params[AXTSdkParameters(USE_HD)]];
    [sdkParams setDataExtractionRequirement:(BOOL)params[AXTSdkParameters(DATA_EXTRACTION_REQUIREMENT)]];
    [sdkParams setUseFrontCamera:(BOOL)params[AXTSdkParameters(USE_FRONT_CAMERA)]];
    
    NSMutableDictionary* extraParams = [[NSMutableDictionary alloc] init];
    
    if ([params objectForKey:@"EXTRA_PARAMETERS"] != nil) {
        NSDictionary *jsExtraParameters = [params objectForKey:@"EXTRA_PARAMETERS"];
        for (NSString* keyExtra in [jsExtraParameters allKeys]) {
            if ([[jsExtraParameters objectForKey:keyExtra] isKindOfClass:[NSNumber class]]) {
                NSString* value = [jsExtraParameters objectForKey:keyExtra];
                [extraParams setObject:@([value intValue]) forKey:keyExtra];
            } else if ([[jsExtraParameters objectForKey:keyExtra] isKindOfClass:[NSString class]]) {
                NSString* value = [jsExtraParameters objectForKey:keyExtra];
                if ([[jsExtraParameters objectForKey:keyExtra] isEqualToString:@"false"] ) {
                    [extraParams setObject:@(NO) forKey:keyExtra];
                } else if ([[jsExtraParameters objectForKey:keyExtra] isEqualToString:@"true"]) {
                    [extraParams setObject:@(YES) forKey:keyExtra];
                } else {
                    [extraParams setObject:value forKey:keyExtra];
                }
            }
        }
    }
    [sdkParams setExtraParameters:extraParams];
    
    UIViewController* controller = [[AXTCaptureInterface captureInterfaceInstance] getViewControllerCaptureSdk:sdkParams];
    if (controller != nil) {
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(getResultFromSmartcrop:) name:SMARTSDK_RESULT object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(smartCropCancelled) name:SMARTSDK_CANCELLED object:nil];
        [[NSNotificationCenter defaultCenter] addObserver:self selector:@selector(smartCropCrash:) name:SMARTSDK_CRASH object:nil];
        
        UIViewController *presentingViewController = [[UIApplication sharedApplication] keyWindow].rootViewController;
        [presentingViewController presentViewController:controller animated:NO completion:nil];
    } else {
        NSLog(@"Wait the initialization end, please...");
    }
}


RCT_EXPORT_SYNCHRONOUS_TYPED_METHOD(BOOL, isActivated) {
    return [[AXTCaptureInterface captureInterfaceInstance] sdkIsActivated];
}

- (void)dealloc
{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_RESULT object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CANCELLED object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CRASH object:nil];
}

- (void) getResultFromSmartcrop:(NSNotification *)paramNotification{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_RESULT object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CANCELLED object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CRASH object:nil];
    
    [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:TRUE completion:nil];
    
    AXTSdkResult* result = [[paramNotification userInfo] valueForKey:SMARTSDK_RESULT_PARAM];
    NSString* jsonString = [self getJSONFromResult:result];
    
    captureResolver(@{@"axtSdkResult": jsonString != nil ? jsonString : @""});
}



-(void) smartCropCancelled{
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_RESULT object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CANCELLED object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CRASH object:nil];
    
    [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:TRUE completion:nil];
    captureRejecter(@"0", @"SmartSDK cancelled", nil);
}

-(void) smartCropCrash:(NSNotification *)paramNotification{
    NSException* exception = [[paramNotification userInfo] valueForKey:SMARTSDK_EXCEPTION];
    NSError *err = [NSError errorWithDomain:@"SmartSDK" code:0 userInfo:exception.userInfo];
    
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_RESULT object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CANCELLED object:nil];
    [[NSNotificationCenter defaultCenter] removeObserver:self name:SMARTSDK_CRASH object:nil];
    
    [[[UIApplication sharedApplication] keyWindow].rootViewController dismissViewControllerAnimated:TRUE completion:nil];
    captureRejecter(@"0", @"SmartSDK stopped with error", err);
}

- (NSString*)getJSONFromResult:(AXTSdkResult*)result {
    NSMutableDictionary *dict = [NSMutableDictionary dictionary];
    
    NSMutableDictionary *crop = [NSMutableDictionary dictionary];
    for (NSString* key in [result mapImageCropped]) {
        NSMutableDictionary *fieldCropped = [NSMutableDictionary dictionary];
        AXTImageResult *image = [[result mapImageCropped] objectForKey:key];
        [fieldCropped setObject:[image imagePath] forKey:IMAGE_URI];
        [crop setObject:fieldCropped forKey:key];
    }
    [dict setObject:crop forKey:MAP_IMAGE_CROPPED];
    
    NSMutableDictionary *source = [NSMutableDictionary dictionary];
    for (NSString* key in [result mapImageSource]) {
        NSMutableDictionary *fieldSource = [NSMutableDictionary dictionary];
        AXTImageResult *image = [[result mapImageSource] objectForKey:key];
        [fieldSource setObject:[image imagePath] forKey:IMAGE_URI];
        [source setObject:fieldSource forKey:key];
    }
    [dict setObject:source forKey:MAP_IMAGE_SOURCE];
    
    NSMutableDictionary *faces = [NSMutableDictionary dictionary];
    for (NSString* key in [result mapImageFace]) {
        NSMutableDictionary *fieldFace = [NSMutableDictionary dictionary];
        AXTImageResult *image = [[result mapImageFace] objectForKey:key];
        [fieldFace setObject:[image imagePath] forKey:IMAGE_URI];
        [faces setObject:fieldFace forKey:key];
    }
    [dict setObject:faces forKey:MAP_IMAGE_FACE];
    
    NSMutableDictionary *documents = [NSMutableDictionary dictionary];
    for(NSString* key in [result mapDocument]){
        NSMutableDictionary *docfields = [NSMutableDictionary dictionary];
        NSMutableDictionary *fields = [NSMutableDictionary dictionary];
        if ([key isEqualToString:IDENTITY_DOCUMENT]){
            AXTDocumentIdentity* doc = [[result mapDocument] objectForKey:key];
            if ([[doc fields] valueForKey:CODELINE] != nil) {
                [fields setObject:[[doc fields] valueForKey:CODELINE] forKey:CODELINE];
            }
            if ([[doc fields] valueForKey:EMIT_DATE] != nil) {
                [fields setObject:[[doc fields] valueForKey:EMIT_DATE] forKey:EMIT_DATE];
            }
            if ([[doc fields] valueForKey:EMIT_COUNTRY] != nil) {
                [fields setObject:[[doc fields] valueForKey:EMIT_COUNTRY] forKey:EMIT_COUNTRY];
            }
            if ([[doc fields] valueForKey:DOCUMENT_NUMBER] != nil) {
                [fields setObject:[[doc fields] valueForKey:DOCUMENT_NUMBER] forKey:DOCUMENT_NUMBER];
            }
            if ([[doc fields] valueForKey:LAST_NAMES] != nil) {
                [fields setObject:[[doc fields] valueForKey:LAST_NAMES] forKey:LAST_NAMES];
            }
            if ([[doc fields] valueForKey:FIRST_NAMES] != nil) {
                [fields setObject:[[doc fields] valueForKey:FIRST_NAMES] forKey:FIRST_NAMES];
            }
            if ([[doc fields] valueForKey:GENDER] != nil) {
                [fields setObject:[[doc fields] valueForKey:GENDER] forKey:GENDER];
            }
            if ([[doc fields] valueForKey:BIRTH_DATE] != nil) {
                [fields setObject:[[doc fields] valueForKey:BIRTH_DATE] forKey:BIRTH_DATE];
            }
            if ([[doc fields] valueForKey:NATIONALITY] != nil) {
                [fields setObject:[[doc fields] valueForKey:NATIONALITY] forKey:NATIONALITY];
            }
            if ([[doc fields] valueForKey:PERSONAL_NUMBER] != nil) {
                [fields setObject:[[doc fields] valueForKey:PERSONAL_NUMBER] forKey:PERSONAL_NUMBER];
            }
            
            AXTDocumentValidityResult validity = [doc documentValidity];
            if (validity == VALID) {
                [docfields setObject:VALIDITY_VALID forKey:DOCUMENT_VALIDITY];
            } else if (validity == INVALID) {
                [docfields setObject:VALIDITY_INVALID forKey:DOCUMENT_VALIDITY];
            } else {
                [docfields setObject:VALIDITY_CONTROL_NOT_AVAILABLE forKey:DOCUMENT_VALIDITY];
            }
            
            if ([doc documentType] != nil) {
                [docfields setObject:[doc documentType] forKey:DOCUMENT_TYPE];
            }
        } else if ([key isEqualToString:CREDIT_CARD_DOCUMENT]){
            AXTDocumentCreditCard* doc = [[result mapDocument] objectForKey:key];
            if ([[doc fields] valueForKey:CODELINE] != nil) {
                [fields setObject:[[doc fields] valueForKey:CODELINE] forKey:CODELINE];
            }
            if ([[doc fields] valueForKey:EXPIRATION_MONTH] != nil) {
                [fields setObject:[[doc fields] valueForKey:EXPIRATION_MONTH] forKey:EXPIRATION_MONTH];
            }
            if ([[doc fields] valueForKey:EXPIRATION_YEAR] != nil) {
                [fields setObject:[[doc fields] valueForKey:EXPIRATION_YEAR] forKey:EXPIRATION_YEAR];
            }
            if ([[doc fields] valueForKey:PAYMENT_CARD_NUMBER] != nil) {
                [fields setObject:[[doc fields] valueForKey:PAYMENT_CARD_NUMBER] forKey:PAYMENT_CARD_NUMBER];
            }
            if ([doc documentValidity] != nil) {
                AXTDocumentValidityResult validity = [doc documentValidity];
                if (validity == VALID) {
                    [docfields setObject:VALIDITY_VALID forKey:DOCUMENT_VALIDITY];
                } else if (validity == INVALID) {
                    [docfields setObject:VALIDITY_INVALID forKey:DOCUMENT_VALIDITY];
                } else {
                    [docfields setObject:VALIDITY_CONTROL_NOT_AVAILABLE forKey:DOCUMENT_VALIDITY];
                }
            }
            if ([doc documentType] != nil) {
                [docfields setObject:[doc documentType] forKey:DOCUMENT_TYPE];
            }
        } else if ([key isEqualToString:REGISTRATION_VEHICLE_DOCUMENT]){
            AXTDocumentRegistrationVehicle* doc = [[result mapDocument] objectForKey:key];
            if ([[doc fields] valueForKey:CODELINE] != nil) {
                [fields setObject:[[doc fields] valueForKey:CODELINE] forKey:CODELINE];
            }
            if ([[doc fields] valueForKey:VEHICLE_NUMBER] != nil) {
                [fields setObject:[[doc fields] valueForKey:VEHICLE_NUMBER] forKey:VEHICLE_NUMBER];
            }
            if ([[doc fields] valueForKey:FIRST_REGISTRATION_DATE] != nil) {
                [fields setObject:[[doc fields] valueForKey:FIRST_REGISTRATION_DATE] forKey:FIRST_REGISTRATION_DATE];
            }
            if ([[doc fields] valueForKey:MODEL_NAME] != nil) {
                [fields setObject:[[doc fields] valueForKey:MODEL_NAME] forKey:MODEL_NAME];
            }
            if ([[doc fields] valueForKey:REGISTRATION_NUMBER] != nil) {
                [fields setObject:[[doc fields] valueForKey:REGISTRATION_NUMBER] forKey:REGISTRATION_NUMBER];
            }
            if ([[doc fields] valueForKey:MAKE_NAME] != nil) {
                [fields setObject:[[doc fields] valueForKey:MAKE_NAME] forKey:MAKE_NAME];
            }
            if ([doc documentValidity] != nil) {
                AXTDocumentValidityResult validity = [doc documentValidity];
                if (validity == VALID) {
                    [docfields setObject:VALIDITY_VALID forKey:DOCUMENT_VALIDITY];
                } else if (validity == INVALID) {
                    [docfields setObject:VALIDITY_INVALID forKey:DOCUMENT_VALIDITY];
                } else {
                    [docfields setObject:VALIDITY_CONTROL_NOT_AVAILABLE forKey:DOCUMENT_VALIDITY];
                }
            }
            if ([doc documentType] != nil) {
                [docfields setObject:[doc documentType] forKey:DOCUMENT_TYPE];
            }
        } else {
            AXTDocumentAbstract* doc = [[result mapDocument] objectForKey:key];
            if ([doc documentValidity] != nil) {
                AXTDocumentValidityResult validity = [doc documentValidity];
                if (validity == VALID) {
                    [docfields setObject:VALIDITY_VALID forKey:DOCUMENT_VALIDITY];
                } else if (validity == INVALID) {
                    [docfields setObject:VALIDITY_INVALID forKey:DOCUMENT_VALIDITY];
                } else {
                    [docfields setObject:VALIDITY_CONTROL_NOT_AVAILABLE forKey:DOCUMENT_VALIDITY];
                }
            }
            if ([doc documentType] != nil) {
                [docfields setObject:[doc documentType] forKey:DOCUMENT_TYPE];
            }
        }
        [docfields setObject:fields forKey:FIELDS];
        [documents setObject:docfields forKey:key];
    }
    [dict setObject:documents forKey:MAP_DOCUMENT];
    
    NSData * jsonData = [NSJSONSerialization dataWithJSONObject:dict options:0 error:nil];
    NSString * myString = [[NSString alloc] initWithData:jsonData encoding:NSUTF8StringEncoding];
    
    return myString;

}

+ (AXTDocumentType)docTypeFromString:(NSString*)type {
    AXTDocumentType result;
    if ([type isEqualToString:@"ID"]){
        result = ID;
    } else if ([type isEqualToString:@"DL_USA"]){
        result = DL_USA;
    } else if ([type isEqualToString:@"A4"]){
        result = A4;
    } else if ([type isEqualToString:@"MRZ"]){
        result = MRZ;
    } else if ([type isEqualToString:@"VEHICLE_REGISTRATION"]){
        result = VEHICLE_REGISTRATION;
    } else if ([type isEqualToString:@"SELFIE"]){
        result = SELFIE;
    } else if ([type isEqualToString:@"A4_PORTRAIT"]){
        result = A4_PORTRAIT;
    } else if ([type isEqualToString:@"A4_LANDSCAPE"]){
        result = A4_LANDSCAPE;
    } else if ([type isEqualToString:@"CHEQUE"]){
        result = CHEQUE;
    } else if ([type isEqualToString:@"DISABLED"]){
        result = DISABLED;
    } else if ([type isEqualToString:@"ANY"]){
        result = ANY;
    } else if ([type isEqualToString:@"CREDIT_CARD"]){
        result = CREDIT_CARD;
    } else if ([type isEqualToString:@"PHOTO"]){
        result = PHOTO;
    } else {
        result = OLD_DL_FR;
    }
    return result;
}

@end
  
