package com.ariadnext.idcheckio;

import android.util.Log;

import com.ariadnext.idcheckio.sdk.bean.CheckType;
import com.ariadnext.idcheckio.sdk.bean.ConfirmationType;
import com.ariadnext.idcheckio.sdk.bean.DataRequirement;
import com.ariadnext.idcheckio.sdk.bean.DocumentType;
import com.ariadnext.idcheckio.sdk.bean.Extraction;
import com.ariadnext.idcheckio.sdk.bean.FaceDetection;
import com.ariadnext.idcheckio.sdk.bean.FeedbackLevel;
import com.ariadnext.idcheckio.sdk.bean.FileSize;
import com.ariadnext.idcheckio.sdk.bean.Forceable;
import com.ariadnext.idcheckio.sdk.bean.IntegrityCheck;
import com.ariadnext.idcheckio.sdk.bean.Language;
import com.ariadnext.idcheckio.sdk.bean.OnlineConfig;
import com.ariadnext.idcheckio.sdk.bean.Orientation;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;
import com.ariadnext.idcheckio.sdk.interfaces.cis.CISType;

import java.util.HashMap;
import java.util.Map;

public class ParameterUtils {

    public static void parseParameters(IdcheckioView.Builder idcheckioView, HashMap<String, Object> params){
        try {
            for (Map.Entry<String, Object> entries : params.entrySet()) {
                String key = entries.getKey();
                Object value = entries.getValue();
                switch (key) {
                    case "docType":
                        idcheckioView.docType(DocumentType.valueOf(value.toString()));
                        break;
                    case "orientation":
                        idcheckioView.orientation(Orientation.valueOf(value.toString()));
                        break;
                    case "confirmationType":
                        idcheckioView.confirmType(ConfirmationType.valueOf(value.toString()));
                        break;
                    case "useHd":
                        idcheckioView.useHd(Boolean.parseBoolean(value.toString()));
                        break;
                    case "integrityCheck":
                        HashMap<String, Object> integrityCheck = (HashMap<String, Object>) value;
                        boolean readEmrtd = Boolean.parseBoolean(integrityCheck.get("readEmrtd").toString());
                        idcheckioView.integrityCheck(new IntegrityCheck(readEmrtd));
                        break;
                    case "scanBothSides":
                        idcheckioView.scanBothSides(Forceable.valueOf(value.toString()));
                        break;
                    case "sideOneExtraction":
                        HashMap<String, String> extraction1 = (HashMap<String, String>) value;
                        idcheckioView.sideOneExtraction(new Extraction(DataRequirement.valueOf(extraction1.get("codeline")),
                                FaceDetection.valueOf(extraction1.get("faceDetection"))));
                        break;
                    case "sideTwoExtraction":
                        HashMap<String, String> extraction2 = (HashMap<String, String>) value;
                        idcheckioView.sideTwoExtraction(new Extraction(DataRequirement.valueOf(extraction2.get("codeline")),
                                FaceDetection.valueOf(extraction2.get("faceDetection"))));
                        break;
                    case "language":
                        idcheckioView.language(Language.valueOf(value.toString()));
                        break;
                    case "manualButtonTimer":
                        double doubleValue = Double.parseDouble(value.toString());
                        idcheckioView.manualButtonTimer((int) doubleValue);
                        break;
                    case "feedbackLevel":
                        idcheckioView.feedbackLevel(FeedbackLevel.valueOf(value.toString()));
                        break;
                    case "adjustCrop":
                        idcheckioView.adjustCrop(Boolean.parseBoolean(value.toString()));
                        break;
                    case "maxPictureFilesize":
                        idcheckioView.maxPictureFilesize(FileSize.valueOf(value.toString()));
                        break;
                    case "token":
                        idcheckioView.token(value.toString());
                        break;
                    case "confirmAbort":
                        idcheckioView.confirmAbort(Boolean.parseBoolean(value.toString()));
                        break;
                    case "onlineConfig":
                        HashMap<String, Object> onlineConfig = (HashMap<String, Object>) value;
                        boolean isReferenceDocument = Boolean.parseBoolean(onlineConfig.get("isReferenceDocument").toString());
                        CheckType checkType = CheckType.valueOf(onlineConfig.get("checkType").toString());
                        CISType cisType;
                        if(onlineConfig.get("cisType") != null){
                            cisType = CISType.valueOf(onlineConfig.get("cisType").toString());
                        } else {
                            cisType = null;
                        }
                        String folderUid;
                        if(onlineConfig.get("folderUid") != null){
                            folderUid = onlineConfig.get("folderUid").toString();
                        } else {
                            folderUid = null;
                        }
                        Boolean biometricConsent;
                        if(onlineConfig.get("biometricConsent") != null){
                            biometricConsent = Boolean.parseBoolean(onlineConfig.get("biometricConsent").toString());
                        } else {
                            biometricConsent = null;
                        }
                        boolean enableManualAnalysis = Boolean.parseBoolean(onlineConfig.get("enableManualAnalysis").toString());
                        idcheckioView.onlineConfig(new OnlineConfig(isReferenceDocument, checkType, cisType, folderUid, biometricConsent, enableManualAnalysis));
                    default:
                        break;
                }
            }
        } catch (IllegalArgumentException ex) {
            Log.e("IdcheckioActivity", "Failed to parse parameters", ex);
        }
    }
}
