package com.ariadnext.idcheckio;

import android.util.Log;

import com.ariadnext.idcheckio.sdk.bean.ConfirmationType;
import com.ariadnext.idcheckio.sdk.bean.DataRequirement;
import com.ariadnext.idcheckio.sdk.bean.DocumentType;
import com.ariadnext.idcheckio.sdk.bean.EnumExtraParameters;
import com.ariadnext.idcheckio.sdk.bean.Extraction;
import com.ariadnext.idcheckio.sdk.bean.FaceDetection;
import com.ariadnext.idcheckio.sdk.bean.FeedbackLevel;
import com.ariadnext.idcheckio.sdk.bean.FileSize;
import com.ariadnext.idcheckio.sdk.bean.Forceable;
import com.ariadnext.idcheckio.sdk.bean.Language;
import com.ariadnext.idcheckio.sdk.bean.Orientation;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;

import java.util.HashMap;
import java.util.Map;

public class ParameterUtils {

    public static void parseParameters(IdcheckioView.Builder idcheckioView, HashMap<String, Object> params){
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
                                    idcheckioView.language(Language.valueOf(extraValue.toString()));
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
                                    break;
                                case "SdkEnvironment":
                                    idcheckioView.extraParameter(EnumExtraParameters.SDK_ENVIRONMENT, extraValue.toString());
                                    break;
                                default:
                                    break;
                            }
                        }
                        break;
                    default:
                        break;
                }
            }
        } catch (IllegalArgumentException ex) {
            Log.e("IdcheckioActivity", "Failed to parse parameters", ex);
        }
    }

}
