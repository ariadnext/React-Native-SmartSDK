package com.ariadnext.idcheckio;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;

import androidx.fragment.app.FragmentActivity;

import com.ariadnext.idcheckio.sdk.bean.CISContext;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteraction;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteractionInterface;
import com.ariadnext.idcheckio.sdk.interfaces.cis.CISType;
import com.ariadnext.idcheckio.sdk.interfaces.result.IdcheckioResult;
import com.ariadnext.idcheckio.sdk.utils.ExtensionUtilsKt;

import java.util.HashMap;

public class IdcheckioActivity extends FragmentActivity implements IdcheckioInteractionInterface {
    private final static int CONTAINER_ID = 123481562;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setId(CONTAINER_ID);
        rootLayout.setBackground(new ColorDrawable(Color.BLACK));
        setContentView(rootLayout);

        Intent intent = getIntent();
        HashMap<String, Object> params = (HashMap<String, Object>) intent.getSerializableExtra("PARAMS");
        HashMap<String, Object> cisContext = (HashMap<String, Object>) intent.getSerializableExtra("CIS");
        String action = intent.getStringExtra("ACTION");
        doCreate(params, cisContext, action);
    }

    private void doCreate(HashMap<String, Object> params, HashMap<String, Object> cisContext, String action) {
        IdcheckioView.Builder idcheckioView = new IdcheckioView.Builder()
                .listener(this);
        ParameterUtils.parseParameters(idcheckioView, params);
        IdcheckioView idcheckio = idcheckioView.build();
        getSupportFragmentManager().beginTransaction().replace(CONTAINER_ID, idcheckio).commit();
        switch (action) {
            case "start":
                idcheckio.start();
                break;
            case "startOnline":
                idcheckio.startOnline(new CISContext((cisContext.get("folderUid") != null) ? cisContext.get("folderUid").toString() : "",
                        (cisContext.get("referenceTaskUid") != null) ? cisContext.get("referenceTaskUid").toString() : "",
                        (cisContext.get("referenceDocUid") != null) ? cisContext.get("referenceDocUid").toString() : "",
                        (cisContext.get("cisType") != null) ? CISType.valueOf(cisContext.get("cisType").toString()) : null,
                        (cisContext.get("biometricConsent") != null) ? Boolean.parseBoolean(cisContext.get("biometricConsent").toString()) : null));
                break;
            default:
                break;
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
                Intent errorIntent = new Intent();
                if (data != null) {
                    errorIntent.putExtra("ERROR_MSG", ExtensionUtilsKt.toJson((ErrorMsg) data));
                }
                this.setResult(RESULT_CANCELED, errorIntent);
                this.finish();
                break;
            default:
                break;
        }
    }
}
