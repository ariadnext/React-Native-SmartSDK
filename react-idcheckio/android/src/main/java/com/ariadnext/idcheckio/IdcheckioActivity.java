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
    private HashMap<String, Object> params;
    private HashMap<String, Object> cisContext;
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
        this.cisContext = (HashMap<String, Object>) intent.getSerializableExtra("CIS");
        this.action = intent.getStringExtra("ACTION");
        this.doCreate();
    }

    private void doCreate() {
        IdcheckioView.Builder idcheckioView = new IdcheckioView.Builder()
                .listener(this);
        ParameterUtils.parseParameters(idcheckioView, this.params);
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
                        (cisContext.get("cisType") != null) ? CISType.valueOf(cisContext.get("cisType").toString()) : null));
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
