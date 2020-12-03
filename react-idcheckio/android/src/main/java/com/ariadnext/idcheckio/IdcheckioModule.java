package com.ariadnext.idcheckio;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import com.ariadnext.idcheckio.sdk.bean.CISContext;
import com.ariadnext.idcheckio.sdk.bean.SdkEnvironment;
import com.ariadnext.idcheckio.sdk.component.Idcheckio;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioCallback;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteraction;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteractionInterface;
import com.ariadnext.idcheckio.sdk.interfaces.cis.CISType;
import com.ariadnext.idcheckio.sdk.interfaces.result.IdcheckioResult;
import com.ariadnext.idcheckio.sdk.utils.EnumUtils;
import com.ariadnext.idcheckio.sdk.utils.ExtensionUtilsKt;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

import java.util.HashMap;

public class IdcheckioModule extends ReactContextBaseJavaModule implements IdcheckioCallback, IdcheckioInteractionInterface {
    /**
     * React context, used to start activity
     */
    private final ReactApplicationContext reactContext;
    /**
     * Promise to send results back to js
     */
    private Promise mPromise;
    /**
     * TAG
     */
    private static final String TAG = "IdcheckioModule";
    /**
     * Error when user back
     */
    private static final String USER_ABORT = "USER_ABORT";
    /**
     * Flag to start the sdk offline mode
     */
    private final static String START = "start";
    /**
     * Flag to start the sdk online mode
     */
    private final static String START_ONLINE = "startOnline";
    /**
     * Flag to retrieve activity result
     */
    private final static int START_REQUEST = 5;
    /**
     * Const for no activity error
     */
    private final static String NO_ACTIVITY_ERROR = "ACTIVITY_DOES_NOT_EXIST";

    public IdcheckioModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        ActivityEventListener mActivityEventListener = new BaseActivityEventListener() {
            @Override
            public void onActivityResult(Activity activity, int requestCode, int resultCode, Intent intent) {
                if (requestCode == START_REQUEST) {
                    if (resultCode == Activity.RESULT_OK) {
                        String result = intent.getExtras().getString("IDCHECKIO_RESULT", "{}");
                        mPromise.resolve(result);
                    } else if(resultCode == Activity.RESULT_CANCELED){
                        if(intent != null){
                            String error = intent.getExtras().getString("ERROR_MSG", "{}");
                            mPromise.reject(TAG, error);
                        } else {
                            mPromise.reject(TAG, USER_ABORT);
                        }
                    }
                }
            }
        };
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void preload(Boolean extractData){
        if(this.getCurrentActivity() != null){
            Idcheckio.preload(this.getCurrentActivity(), extractData);
        }
    }

    @ReactMethod
    public void activate(String licenceFileName, Boolean extractData, Boolean disableImei, Boolean disableAudioForLiveness,
                 String environment, Promise promise){
        this.mPromise = promise;
        if(!EnumUtils.isValidEnum(SdkEnvironment.class, environment)){
            mPromise.reject(TAG, "Wrong SdkEnvironment value.");
        }
        if(this.getCurrentActivity() != null){
            Idcheckio.activate(licenceFileName, this, this.getCurrentActivity(), extractData, disableImei,
                    disableAudioForLiveness, SdkEnvironment.valueOf(environment));
        } else {
            mPromise.reject(TAG, NO_ACTIVITY_ERROR);
        }
    }

    @ReactMethod
    public void start(ReadableMap map, Promise promise){
        this.mPromise = promise;
        if(this.getCurrentActivity() != null){
            Intent intent = new Intent(this.getCurrentActivity(), IdcheckioActivity.class);
            intent.putExtra("PARAMS", map.toHashMap());
            intent.putExtra("ACTION", START);
            this.getCurrentActivity().startActivityForResult(intent, START_REQUEST);
        } else {
            this.mPromise.reject(TAG, NO_ACTIVITY_ERROR);
        }
    }

    @ReactMethod
    public void startOnline(ReadableMap params, ReadableMap cis, Promise promise){
        this.mPromise = promise;
        if(this.getCurrentActivity() != null) {
            Intent intent = new Intent(this.getCurrentActivity(), IdcheckioActivity.class);
            intent.putExtra("PARAMS", params.toHashMap());
            intent.putExtra("CIS", cis.toHashMap());
            intent.putExtra("ACTION", START_ONLINE);
            this.getCurrentActivity().startActivityForResult(intent, START_REQUEST);
        } else {
            this.mPromise.reject(TAG, NO_ACTIVITY_ERROR);
        }
    }

    @ReactMethod
    public void analyze(ReadableMap params, String side1ToUpload, String side2ToUpload, Boolean isOnline, ReadableMap cis, Promise promise){
        this.mPromise = promise;
        if(this.getCurrentActivity() != null) {
            Uri side1Uri, side2Uri = null;
            IdcheckioView.Builder builder = new IdcheckioView.Builder();
            ParameterUtils.parseParameters(builder, params.toHashMap());
            HashMap<String, Object> cisContext = cis.toHashMap();
            if(side1ToUpload != null){
                side1Uri = Uri.parse(side1ToUpload);
            } else {
                this.mPromise.reject(TAG, "SIDE_1_PARSING_ERROR");
                return;
            }
            if(side2ToUpload != null){
                side2Uri = Uri.parse(side2ToUpload);
            }
            //Start in a custom thread
            Idcheckio.analyze(reactContext.getApplicationContext(),
                    builder.captureParams(),
                    side1Uri,
                    side2Uri,
                    this,
                    isOnline,
                    new CISContext((cisContext.get("folderUid") != null) ? cisContext.get("folderUid").toString() : "",
                            (cisContext.get("referenceTaskUid") != null) ? cisContext.get("referenceTaskUid").toString() : "",
                            (cisContext.get("referenceDocUid") != null) ? cisContext.get("referenceDocUid").toString() : "",
                            (cisContext.get("cisType") != null) ? CISType.valueOf(cisContext.get("cisType").toString()) : null,
                            (cisContext.get("biometricConsent") != null) ? Boolean.parseBoolean(cisContext.get("biometricConsent").toString()) : null));
        } else {
            this.mPromise.reject(TAG, NO_ACTIVITY_ERROR);
        }
    }

    @Override
    public void onInitEnd(boolean success, ErrorMsg errorMsg) {
        if(success){
            this.mPromise.resolve(null);
        } else {
            this.mPromise.reject(TAG, ExtensionUtilsKt.toJson(errorMsg));
        }
    }

    @Override
    public void onIdcheckioInteraction(IdcheckioInteraction idcheckioInteraction, Object data) {
        switch (idcheckioInteraction) {
            case RESULT:
                mPromise.resolve(ExtensionUtilsKt.toJson((IdcheckioResult) data));
                break;
            case ERROR:
                mPromise.reject(TAG, ExtensionUtilsKt.toJson((ErrorMsg) data));
                break;
            default:
                break;
        }
    }
}
