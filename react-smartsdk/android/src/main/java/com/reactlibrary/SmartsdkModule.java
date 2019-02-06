
package com.reactlibrary;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;

import com.ariadnext.android.smartsdk.bean.enums.AXTSdkParameters;
import com.ariadnext.android.smartsdk.enums.EnumExtraParameter;
import com.ariadnext.android.smartsdk.exception.CaptureApiException;
import com.ariadnext.android.smartsdk.interfaces.AXTCaptureInterface;
import com.ariadnext.android.smartsdk.interfaces.AXTCaptureInterfaceCallback;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTDataExtractionRequirement;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTDocumentType;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkInit;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkParams;
import com.ariadnext.android.smartsdk.interfaces.bean.AXTSdkResult;
import com.ariadnext.android.smartsdk.utils.EnumUtils;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class SmartsdkModule extends ReactContextBaseJavaModule implements AXTCaptureInterfaceCallback {
    /**
     * React Context
     */
    private final ReactApplicationContext reactContext;
    /**
     * Init Callback
     */
    private Promise initCallback;
    /**
     * Capture Callback
     */
    private Promise captureCallback;
    /**
     * isActivated
     */
    private boolean isActivated = false;
    /**
     * Intent request code
     */
    private static final int SDK_REQUEST_CODE = 101;
    /**
     * TAG
     */
    private static final String TAG = "SmartsdkModule";
    
    public SmartsdkModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent data) {
                if (requestCode == SDK_REQUEST_CODE) {
                    if (resultCode == Activity.RESULT_OK) {
                        try {
                            final AXTSdkResult result = AXTCaptureInterface.INSTANCE.getResultImageFromCapture(data);
                            ObjectMapper mapper = new ObjectMapper();
                            String json_cropped = mapper.writeValueAsString(result.getMapImageCropped());
                            String json_source = mapper.writeValueAsString(result.getMapImageSource());
                            String json_face = mapper.writeValueAsString(result.getMapImageFace());
                            String json_document = mapper.writeValueAsString(result.getMapDocument());
                            String json_result = "{\"mapImageSource\":" + json_source + ",\"mapImageCropped\":" + json_cropped + ",\"mapImageFace\":" + json_face + ",\"mapDocument\":" + json_document + "}";
                            WritableMap map = Arguments.createMap();
                            map.putString("axtSdkResult", json_result);
                            captureCallback.resolve(map);
                        } catch (final CaptureApiException ex) {
                            captureCallback.reject(TAG, ex.getMessage());
                        } catch (JsonProcessingException e) {
                            captureCallback.reject(TAG, "JSON_PARSING_ERROR");
                        }
                    }
                }
            }
        };
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }
    
    @ReactMethod
    public void initSmartSdk(Promise initCallback){
        Log.d(TAG, "Starting SmartSdk init...");
        this.initCallback = initCallback;
        if(this.getCurrentActivity() != null) {
            try {
                AXTSdkInit sdkInit = new AXTSdkInit("licence");
                AXTCaptureInterface.INSTANCE.initCaptureSdk(this.getCurrentActivity(), sdkInit, this);
            } catch (CaptureApiException e) {
                Log.e(TAG, e.getMessage());
                this.initCallback.reject(TAG, e.getMessage());
            }
        } else {
            Log.e(TAG, "ACTIVITY_DOES_NOT_EXIST");
            this.initCallback.reject(TAG, "ACTIVITY_DOES_NOT_EXIST");
        }
    }
    
    @ReactMethod
    public void capture(ReadableMap map, Promise captureCallback){
        this.captureCallback = captureCallback;
        if(getCurrentActivity() != null) {
            AXTSdkParams params = new AXTSdkParams();
            ReadableMapKeySetIterator iterator = map.keySetIterator();
            while(iterator.hasNextKey()){
                String key = iterator.nextKey();
                switch(key){
                    case "EXTRACT_DATA" :
                        params.addParameters(AXTSdkParameters.EXTRACT_DATA, Boolean.parseBoolean(map.getString(key)));
                        break;
                    case "USE_HD" :
                        params.addParameters(AXTSdkParameters.USE_HD, Boolean.parseBoolean(map.getString(key)));
                        break;
                    case "DISPLAY_CAPTURE" :
                        params.addParameters(AXTSdkParameters.DISPLAY_CAPTURE, Boolean.parseBoolean(map.getString(key)));
                        break;
                    case "SCAN_RECTO_VERSO" :
                        params.addParameters(AXTSdkParameters.SCAN_RECTO_VERSO, Boolean.parseBoolean(map.getString(key)));
                        break;
                    case "USE_FRONT_CAMERA" :
                        params.addParameters(AXTSdkParameters.USE_FRONT_CAMERA, Boolean.parseBoolean(map.getString(key)));
                        break;
                    case "DOCUMENT_TYPE" :
                        params.setDocType(AXTDocumentType.valueOf(map.getString(key)));
                        break;
                    case "DATA_EXTRACTION_REQUIREMENT" :
                        params.addParameters(AXTSdkParameters.DATA_EXTRACTION_REQUIREMENT, AXTDataExtractionRequirement.valueOf(map.getString(key)));
                        break;
                    case "EXTRA_PARAMETERS" :
                        ReadableMap extra = map.getMap(key);
                        ReadableMapKeySetIterator iteratorExtra = extra.keySetIterator();
                        while(iteratorExtra.hasNextKey()) {
                            String keyExtra = iteratorExtra.nextKey();
                            if(EnumUtils.isValidEnum(EnumExtraParameter.class, keyExtra)) {
                                params.addExtraParameters(EnumExtraParameter.valueOf(keyExtra), extra.getString(keyExtra));
                            }
                        }
                        break;
                }
            }
            
            if(this.isActivated){
                try {
                    final Intent smartsdk = AXTCaptureInterface.INSTANCE.getIntentCapture(this.getCurrentActivity().getApplicationContext(), params);
                    this.getCurrentActivity().startActivityForResult(smartsdk, SDK_REQUEST_CODE, null);
                } catch (CaptureApiException ex){
                    this.captureCallback.reject(TAG, ex.getMessage());
                }
            } else {
                this.captureCallback.reject(TAG, "SDK_NOT_ACTIVATED");
            }
        } else {
            this.captureCallback.reject(TAG, "ACTIVITY_DOES_NOT_EXIST");
        }
    }
    
    @ReactMethod
    public boolean isActivated() {
        return isActivated;
    }
    
    @Override
    public String getName() {
        return "SmartsdkModule";
    }
    
    @Override
    public void onInitSuccess() {
        this.isActivated = true;
        Log.i(TAG, "INIT_SUCCESS");
        this.initCallback.resolve("INIT_SUCCESS");
    }
    
    @Override
    public void onInitError() {
        this.isActivated = false;
        Log.i(TAG, "INIT_FAILED");
        this.initCallback.reject(TAG, "INIT_FAILED");
    }
}