package com.ariadnext.idcheckio;

import android.app.Activity;
import android.content.Intent;

import com.ariadnext.idcheckio.sdk.component.Idcheckio;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioCallback;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

public class IdcheckioModule extends ReactContextBaseJavaModule implements IdcheckioCallback {
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
                        String resultType = "";
                        String resultErrorCode = "";
                        String resultMessage = "";
                        if(intent != null) {
                            if (intent.hasExtra("IDCHECKIO_ERROR_TYPE") && intent.getExtras().get("IDCHECKIO_ERROR_TYPE") != null)
                                resultType = intent.getExtras().get("IDCHECKIO_ERROR_TYPE").toString();
                            if (intent.hasExtra("IDCHECKIO_ERROR_CODE") && intent.getExtras().get("IDCHECKIO_ERROR_CODE") != null)
                                resultErrorCode = intent.getExtras().get("IDCHECKIO_ERROR_CODE").toString();
                            if (intent.hasExtra("IDCHECKIO_ERROR_MESSAGE") && intent.getExtras().get("IDCHECKIO_ERROR_MESSAGE") != null)
                                resultMessage = intent.getExtras().getString("IDCHECKIO_ERROR_MESSAGE", "");
                        }
                        mPromise.reject(TAG, "{\"type\":\"" + resultType + "\"," +
                                "\"code\":\"" + resultErrorCode + "\"," +
                                "\"message\":\"" + resultMessage + "\"}");
                    }
                }
            }
        };
        this.reactContext.addActivityEventListener(mActivityEventListener);
    }

    @Override
    public String getName() {
        return "IdcheckioModule";
    }

    @ReactMethod
    public void preload(Boolean extractData){
        if(this.getCurrentActivity() != null){
            Idcheckio.INSTANCE.preload(this.getCurrentActivity(), extractData);
        }
    }

    @ReactMethod
    public void activate(String licenceFileName, Boolean extractData, Boolean disableImei, Promise promise){
        this.mPromise = promise;
        if(this.getCurrentActivity() != null){
            Idcheckio.INSTANCE.activate(licenceFileName, this, this.getCurrentActivity(), extractData, disableImei);
        } else {
            this.mPromise.reject(TAG, "ACTIVITY_DOES_NOT_EXIST");
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
            this.mPromise.reject(TAG, "ACTIVITY_DOES_NOT_EXIST");
        }
    }

    @ReactMethod
    public void startOnline(ReadableMap params, String licenceFileName, ReadableMap cis, Boolean disableImei, Promise promise){
        this.mPromise = promise;
        if(this.getCurrentActivity() != null) {
            Intent intent = new Intent(this.getCurrentActivity(), IdcheckioActivity.class);
            intent.putExtra("PARAMS", params.toHashMap());
            intent.putExtra("LICENCE", licenceFileName);
            intent.putExtra("CIS", cis.toHashMap());
            intent.putExtra("IMEI", disableImei);
            intent.putExtra("ACTION", START_ONLINE);
            this.getCurrentActivity().startActivityForResult(intent, START_REQUEST);
        } else {
            this.mPromise.reject(TAG, "ACTIVITY_DOES_NOT_EXIST");
        }
    }

    @Override
    public void onInitEnd(boolean success, ErrorMsg errorMsg) {
        if(success){
            this.mPromise.resolve(null);
        } else {
            this.mPromise.reject(TAG, (errorMsg.getMessage() != null)? errorMsg.getMessage() : "");
        }
    }
}
