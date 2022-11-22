package com.ariadnext.idcheckio;

import static com.ariadnext.idcheckio.IdcheckioConst.FOLDER_UID;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import com.ariadnext.idcheckio.sdk.bean.CaptureParams;
import com.ariadnext.idcheckio.sdk.bean.OnlineContext;
import com.ariadnext.idcheckio.sdk.component.Idcheckio;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioCallback;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioError;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioErrorCause;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioErrorKt;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteraction;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteractionInterface;
import com.ariadnext.idcheckio.sdk.interfaces.result.IdcheckioResult;
import com.ariadnext.idcheckio.sdk.utils.extension.JsonExtensionKt;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.BaseActivityEventListener;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableMap;

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
                        if(intent != null){
                            String error = intent.getExtras().getString("ERROR_MSG", "{}");
                            mPromise.reject(TAG, error);
                        }
                    }
                }
            }
        };
        reactContext.addActivityEventListener(mActivityEventListener);
    }

    @NonNull
    @Override
    public String getName() {
        return TAG;
    }

    @ReactMethod
    public void preload(Boolean extractData){
        Idcheckio.preload(reactContext, extractData);
    }

    private boolean checkContext() {
        if(getCurrentActivity() == null) {
            onIdcheckioInteraction(IdcheckioInteraction.ERROR, IdcheckioErrorKt.toErrorMsg(IdcheckioError.CONTEXT_LOST,
                    "Fail to find a usable activity to start the sdk.", null));
            return false;
        }
        return true;
    }

    @ReactMethod
    public void activate(String idToken, Boolean extractData, Promise promise){
        mPromise = promise;
        Idcheckio.activate(idToken, reactContext, this, extractData);
    }

    @ReactMethod
    public void start(ReadableMap map, Promise promise){
        mPromise = promise;
        if(!checkContext()) return;
        Intent intent = new Intent(getCurrentActivity(), IdcheckioActivity.class);
        intent.putExtra("PARAMS", map.toHashMap());
        intent.putExtra("isOnline", false);
        getCurrentActivity().startActivityForResult(intent, START_REQUEST);
    }

    @ReactMethod
    public void startOnline(ReadableMap params, ReadableMap onlineContext, Promise promise){
        mPromise = promise;
        if(!checkContext()) return;
        Intent intent = new Intent(this.getCurrentActivity(), IdcheckioActivity.class);
        intent.putExtra("PARAMS", params.toHashMap());
        intent.putExtra("isOnline", true);
        intent.putExtra("ONLINE", (onlineContext != null)? onlineContext.toHashMap().toString() : null);
        getCurrentActivity().startActivityForResult(intent, START_REQUEST);
    }

    @ReactMethod
    public void startIps(String folderUid, Promise promise){
        mPromise = promise;
        if(!checkContext()) return;
        if(folderUid == null || folderUid.isEmpty()) {
            onIdcheckioInteraction(IdcheckioInteraction.ERROR, new ErrorMsg(IdcheckioErrorCause.CUSTOMER_ERROR, "MISSING_FOLDER_UID",
                    "The ips folderUid is mandatory to start an ips session.", null));
        } else {
            Intent intent = new Intent(getCurrentActivity(), IdcheckioActivity.class);
            intent.putExtra(FOLDER_UID, folderUid);
            intent.putExtra("isIps", true);
            getCurrentActivity().startActivityForResult(intent, START_REQUEST);
        }
    }

    @ReactMethod
    public void analyze(ReadableMap params, String side1ToUpload, String side2ToUpload, Boolean isOnline, ReadableMap context, Promise promise){
        mPromise = promise;
        Uri side1Uri, side2Uri = null;
        CaptureParams captureParams = ParameterUtils.getIDCheckioViewFromCall(params.toHashMap()).captureParams();
        OnlineContext onlineContext = null;
        if(context != null) {
            onlineContext = OnlineContext.createFrom(context.toHashMap().toString());
        }
        if(side1ToUpload != null){
            if(side1ToUpload.startsWith("/data")) {
                side1Uri = Uri.parse(side1ToUpload+"file://");
            } else {
                side1Uri = Uri.parse(side1ToUpload);
            }
        } else {
            onIdcheckioInteraction(IdcheckioInteraction.ERROR, IdcheckioErrorKt.toErrorMsg(IdcheckioError.FAILED_TO_RETRIEVE_IMAGE_FROM_GALLERY, "Image path is empty.", null));
            return;
        }
        if(side2ToUpload != null){
            if(side2ToUpload.startsWith("/data")) {
                side2Uri = Uri.parse(side2ToUpload+"file://");
            } else {
                side2Uri = Uri.parse(side2ToUpload);
            }
        }
        //Start in a custom thread
        Idcheckio.analyze(reactContext,
                this,
                captureParams,
                side1Uri,
                side2Uri,
                isOnline,
                onlineContext);
    }

    @Override
    public void onInitEnd(boolean success, ErrorMsg errorMsg) {
        if(success){
            mPromise.resolve(null);
        } else {
            mPromise.reject(TAG, JsonExtensionKt.toJson(errorMsg));
        }
    }

    @Override
    public void onIdcheckioInteraction(IdcheckioInteraction idcheckioInteraction, Object data) {
        switch (idcheckioInteraction) {
            case RESULT:
                mPromise.resolve(JsonExtensionKt.toJson((IdcheckioResult) data));
                break;
            case ERROR:
                mPromise.reject(TAG, JsonExtensionKt.toJson((ErrorMsg) data));
                break;
            default:
                break;
        }
    }
}
