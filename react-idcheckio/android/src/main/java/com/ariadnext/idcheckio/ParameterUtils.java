package com.ariadnext.idcheckio;

import static com.ariadnext.idcheckio.IdcheckioConst.ADJUST_CROP;
import static com.ariadnext.idcheckio.IdcheckioConst.BIOMETRIC_CONSENT;
import static com.ariadnext.idcheckio.IdcheckioConst.CIS_TYPE;
import static com.ariadnext.idcheckio.IdcheckioConst.CONFIRM_ABORT;
import static com.ariadnext.idcheckio.IdcheckioConst.CONFIRM_TYPE;
import static com.ariadnext.idcheckio.IdcheckioConst.DATA_REQUIREMENT;
import static com.ariadnext.idcheckio.IdcheckioConst.DOCUMENT_TYPE;
import static com.ariadnext.idcheckio.IdcheckioConst.DOC_LIVENESS;
import static com.ariadnext.idcheckio.IdcheckioConst.ENABLE_MANUAL_ANALYSIS;
import static com.ariadnext.idcheckio.IdcheckioConst.FACE_DETECTION;
import static com.ariadnext.idcheckio.IdcheckioConst.FEEDBACK_LEVEL;
import static com.ariadnext.idcheckio.IdcheckioConst.FOLDER_UID;
import static com.ariadnext.idcheckio.IdcheckioConst.INTEGRITY_CHECK;
import static com.ariadnext.idcheckio.IdcheckioConst.IS_REFERENCE_DOC;
import static com.ariadnext.idcheckio.IdcheckioConst.LANGUAGE;
import static com.ariadnext.idcheckio.IdcheckioConst.MANUAL_BUTTON_TIMER;
import static com.ariadnext.idcheckio.IdcheckioConst.MAX_PICTURE_FILESIZE;
import static com.ariadnext.idcheckio.IdcheckioConst.ONLINE_CONFIG;
import static com.ariadnext.idcheckio.IdcheckioConst.ORIENTATION;
import static com.ariadnext.idcheckio.IdcheckioConst.READ_EMRTD;
import static com.ariadnext.idcheckio.IdcheckioConst.SCAN_BOTH_SIDES;
import static com.ariadnext.idcheckio.IdcheckioConst.SIDE_1_EXTRACTION;
import static com.ariadnext.idcheckio.IdcheckioConst.SIDE_2_EXTRACTION;
import static com.ariadnext.idcheckio.IdcheckioConst.USE_HD;

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

    public static IdcheckioView.Builder getIDCheckioViewFromCall(HashMap<String, Object> params) {
        IdcheckioView.Builder idcheckioView = new IdcheckioView.Builder();
        for (Map.Entry<String, Object> entries : params.entrySet()) {
            String key = entries.getKey();
            Object value = entries.getValue();
            switch (key) {
                case DOCUMENT_TYPE:
                    idcheckioView.docType(DocumentType.valueOf(value.toString()));
                    break;
                case ORIENTATION:
                    idcheckioView.orientation(Orientation.valueOf(value.toString()));
                    break;
                case CONFIRM_TYPE:
                    idcheckioView.confirmType(ConfirmationType.valueOf(value.toString()));
                    break;
                case INTEGRITY_CHECK:
                    idcheckioView.integrityCheck(getIntegrityCheck((HashMap<String, Object>) value));
                    break;
                case USE_HD:
                    idcheckioView.useHd(Boolean.parseBoolean(value.toString()));
                    break;
                case SCAN_BOTH_SIDES:
                    idcheckioView.scanBothSides(Forceable.valueOf(value.toString()));
                    break;
                case SIDE_1_EXTRACTION:
                    idcheckioView.sideOneExtraction(getExtraction((HashMap<String, String>) value));
                    break;
                case SIDE_2_EXTRACTION:
                    idcheckioView.sideTwoExtraction(getExtraction((HashMap<String, String>) value));
                    break;
                case ONLINE_CONFIG:
                    idcheckioView.onlineConfig(getOnlineConfig((HashMap<String, Object>) value));
                    break;
                case LANGUAGE:
                    idcheckioView.language(Language.valueOf(value.toString()));
                    break;
                case MANUAL_BUTTON_TIMER:
                    double doubleValue = Double.parseDouble(value.toString());
                    idcheckioView.manualButtonTimer((int) doubleValue);
                    break;
                case FEEDBACK_LEVEL:
                    idcheckioView.feedbackLevel(FeedbackLevel.valueOf(value.toString()));
                    break;
                case ADJUST_CROP:
                    idcheckioView.adjustCrop(Boolean.parseBoolean(value.toString()));
                    break;
                case MAX_PICTURE_FILESIZE:
                    idcheckioView.maxPictureFilesize(FileSize.valueOf(value.toString()));
                    break;
                case CONFIRM_ABORT:
                    idcheckioView.confirmAbort(Boolean.parseBoolean(value.toString()));
                    break;
            }
        }
        return  idcheckioView;
    }

    private static IntegrityCheck getIntegrityCheck(HashMap<String, Object> integrityMap) {
        boolean readEmrtd = false;
        boolean docLiveness = false;

        if(integrityMap.containsKey(READ_EMRTD)) {
            Object readEmrtdOpt = integrityMap.get(READ_EMRTD);
            if(readEmrtdOpt != null) {
                readEmrtd = Boolean.parseBoolean(readEmrtdOpt.toString());
            }
        }
        if(integrityMap.containsKey(DOC_LIVENESS)) {
            Object docLivenessOpt = integrityMap.get(DOC_LIVENESS);
            if(docLivenessOpt != null) {
                docLiveness = Boolean.parseBoolean(docLivenessOpt.toString());
            }
        }
        return new IntegrityCheck(readEmrtd, docLiveness);
    }

    private static Extraction getExtraction(HashMap<String, String> integrityMap) {
        DataRequirement dataRequirement = DataRequirement.DISABLED;
        FaceDetection faceDetection = FaceDetection.DISABLED;

        if(integrityMap.containsKey(DATA_REQUIREMENT)) {
            String dataOpt = integrityMap.get(DATA_REQUIREMENT);
            if(dataOpt != null) {
                dataRequirement = DataRequirement.valueOf(dataOpt);
            }
        }
        if(integrityMap.containsKey(FACE_DETECTION)) {
            String faceOpt = integrityMap.get(FACE_DETECTION);
            if(faceOpt != null) {
                faceDetection = FaceDetection.valueOf(faceOpt);
            }
        }
        return new Extraction(dataRequirement, faceDetection);
    }

    private static OnlineConfig getOnlineConfig(HashMap<String, Object> configMap) {
        OnlineConfig onlineConfig = new OnlineConfig();

        if(configMap.containsKey(BIOMETRIC_CONSENT)) {
            Object bioOpt = configMap.get(BIOMETRIC_CONSENT);
            if(bioOpt != null) {
                onlineConfig.setBiometricConsent(Boolean.parseBoolean(bioOpt.toString()));
            }
        }

        if(configMap.containsKey(IS_REFERENCE_DOC)) {
            Object refDocOpt = configMap.get(IS_REFERENCE_DOC);
            if(refDocOpt != null) {
                onlineConfig.setReferenceDocument(Boolean.parseBoolean(refDocOpt.toString()));
            }
        }

        if(configMap.containsKey(ENABLE_MANUAL_ANALYSIS)) {
            Object manualOpt = configMap.get(ENABLE_MANUAL_ANALYSIS);
            if(manualOpt != null) {
                onlineConfig.setEnableManualAnalysis(Boolean.parseBoolean(manualOpt.toString()));
            }
        }

        if(configMap.containsKey(FOLDER_UID)) {
            Object folderOpt = configMap.get(FOLDER_UID);
            if(folderOpt != null) {
                onlineConfig.setFolderUid(folderOpt.toString());
            }
        }

        if(configMap.containsKey(CIS_TYPE)) {
            Object cisOpt = configMap.get(CIS_TYPE);
            if(cisOpt != null) {
                onlineConfig.setCisType(CISType.valueOf(cisOpt.toString()));
            }
        }
        return onlineConfig;
    }
}
