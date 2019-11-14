package com.ariadnext.idcheckio;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.ariadnext.idcheckio.sdk.bean.CISContext;
import com.ariadnext.idcheckio.sdk.bean.ConfirmationType;
import com.ariadnext.idcheckio.sdk.bean.DataRequirement;
import com.ariadnext.idcheckio.sdk.bean.DocumentType;
import com.ariadnext.idcheckio.sdk.bean.Extraction;
import com.ariadnext.idcheckio.sdk.bean.FaceDetection;
import com.ariadnext.idcheckio.sdk.bean.FeedbackLevel;
import com.ariadnext.idcheckio.sdk.bean.FileSize;
import com.ariadnext.idcheckio.sdk.bean.Forceable;
import com.ariadnext.idcheckio.sdk.bean.Orientation;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteraction;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteractionInterface;
import com.ariadnext.idcheckio.sdk.interfaces.cis.CISType;
import com.ariadnext.idcheckio.sdk.interfaces.result.IdcheckioResult;
import com.ariadnext.idcheckio.sdk.utils.ExtensionUtilsKt;

import java.util.HashMap;
import java.util.Map;

public class IdcheckioActivity extends FragmentActivity implements IdcheckioInteractionInterface {
    private final static int CONTAINER_ID = 123481562;
    private HashMap<String, Object> params;
    private String licenceFileName;
    private HashMap<String, Object> cisContext;
    private boolean disableImei;
    private String action;
    private FrameLayout rootLayout;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        this.getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        this.rootLayout = new FrameLayout(this);
        this.rootLayout.setId(CONTAINER_ID);
        this.rootLayout.setBackground(new ColorDrawable(Color.BLACK));
        setContentView(this.rootLayout);

        Intent intent = getIntent();
        this.params = (HashMap<String, Object>) intent.getSerializableExtra("PARAMS");
        this.licenceFileName = intent.getStringExtra("LICENCE");
        this.cisContext = (HashMap<String, Object>) intent.getSerializableExtra("CIS");
        this.disableImei = intent.getBooleanExtra("IMEI", true);
        this.action = intent.getStringExtra("ACTION");
        this.doCreate();
    }

    private void doCreate() {
        IdcheckioView.Builder idcheckioView = new IdcheckioView.Builder()
                .listener(this);
        try {
            for (Map.Entry<String, Object> entries : params.entrySet()) {
                String key = entries.getKey();
                Object value = entries.getValue();
                switch (key) {
                    case "DocumentType":
                        idcheckioView.docType(DocumentType.valueOf(value.toString()));
                        break;
                    case "Orientation":
                        idcheckioView.orientation(Orientation.valueOf(value.toString()));
                        break;
                    case "ConfirmType":
                        idcheckioView.confirmType(ConfirmationType.valueOf(value.toString()));
                        break;
                    case "UseHd":
                        idcheckioView.useHd(Boolean.parseBoolean(value.toString()));
                        break;
                    case "ScanBothSides":
                        idcheckioView.scanBothSides(Forceable.valueOf(value.toString()));
                        break;
                    case "Side1Extraction":
                        idcheckioView.sideOneExtraction(new Extraction(DataRequirement.valueOf(((HashMap<String, Object>) value).get("DataRequirement").toString()),
                                FaceDetection.valueOf(((HashMap<String, Object>) value).get("FaceDetection").toString())));
                        break;
                    case "Side2Extraction":
                        idcheckioView.sideTwoExtraction(new Extraction(DataRequirement.valueOf(((HashMap<String, Object>) value).get("DataRequirement").toString()),
                                FaceDetection.valueOf(((HashMap<String, Object>) value).get("FaceDetection").toString())));
                        break;
                    case "ExtraParams":
                        HashMap<String, Object> extraParams = (HashMap<String, Object>) value;
                        for (Map.Entry<String, Object> extraEntries : extraParams.entrySet()) {
                            String extraKey = extraEntries.getKey();
                            Object extraValue = extraEntries.getValue();
                            switch (extraKey) {
                                case "Language":
                                    idcheckioView.language(extraValue.toString());
                                    break;
                                case "ManualButtonTimer":
                                    idcheckioView.manualButtonTimer(Integer.parseInt(extraValue.toString()));
                                    break;
                                case "MaxPictureFilesize":
                                    idcheckioView.maxPictureFilesize(FileSize.valueOf(extraValue.toString()));
                                    break;
                                case "FeedbackLevel":
                                    idcheckioView.feedbackLevel(FeedbackLevel.valueOf(extraValue.toString()));
                                    break;
                                case "Token":
                                    idcheckioView.token(extraValue.toString());
                                    break;
                                case "AdjustCrop":
                                    idcheckioView.adjustCrop(Boolean.parseBoolean(extraValue.toString()));
                                    break;
                                case "ConfirmAbort":
                                    idcheckioView.confirmAbort(Boolean.parseBoolean(extraValue.toString()));
                                default:
                                    break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
            IdcheckioView idcheckio = idcheckioView.build();
            getSupportFragmentManager().beginTransaction().replace(CONTAINER_ID, idcheckio).commit();
            switch (action) {
                case "start":
                    idcheckio.start();
                    break;
                case "startOnline":
                    idcheckio.startOnline(licenceFileName,
                            new CISContext((cisContext.get("folderUid") != null) ? cisContext.get("folderUid").toString() : "",
                                    (cisContext.get("referenceTaskUid") != null) ? cisContext.get("referenceTaskUid").toString() : "",
                                    (cisContext.get("referenceDocUid") != null) ? cisContext.get("referenceDocUid").toString() : "",
                                    (cisContext.get("cisType") != null) ? CISType.valueOf(cisContext.get("cisType").toString()) : null
                            ), disableImei);
                    break;
                default:
                    break;
            }
        } catch (IllegalArgumentException ex) {
            Log.e("IdcheckioActivity", "Failed to parse parameters", ex);
        }
    }

    @Override
    public void onIdcheckioInteraction(IdcheckioInteraction idcheckioInteraction, Object data) {
        switch (idcheckioInteraction) {
            case RESULT:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("IDCHECKIO_RESULT", ExtensionUtilsKt.toJson((IdcheckioResult) data));
                this.setResult(RESULT_OK, resultIntent);
                this.finish();
                break;
            case ERROR:
                ErrorMsg errorMsg = (ErrorMsg) data;
                Intent errorIntent = new Intent();
                if (errorMsg != null) {
                    errorIntent.putExtra("IDCHECKIO_ERROR_TYPE", errorMsg.getType());
                    errorIntent.putExtra("IDCHECKIO_ERROR_CODE", errorMsg.getCode());
                    errorIntent.putExtra("IDCHECKIO_ERROR_MESSAGE", errorMsg.getMessage());
                }
                this.setResult(RESULT_CANCELED, errorIntent);
                this.finish();
                break;
            default:
                break;
        }
    }
}
