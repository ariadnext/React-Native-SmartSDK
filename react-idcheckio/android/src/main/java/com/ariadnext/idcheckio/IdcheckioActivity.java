package com.ariadnext.idcheckio;

import static com.ariadnext.idcheckio.IdcheckioConst.*;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.Window;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import com.ariadnext.idcheckio.sdk.bean.DayNightTheme;
import com.ariadnext.idcheckio.sdk.bean.DisplayMode;
import com.ariadnext.idcheckio.sdk.bean.IpsCustomization;
import com.ariadnext.idcheckio.sdk.bean.OnlineContext;
import com.ariadnext.idcheckio.sdk.bean.Orientation;
import com.ariadnext.idcheckio.sdk.component.Idcheckio;
import com.ariadnext.idcheckio.sdk.component.IdcheckioView;
import com.ariadnext.idcheckio.sdk.interfaces.ErrorMsg;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioError;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioErrorKt;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteraction;
import com.ariadnext.idcheckio.sdk.interfaces.IdcheckioInteractionInterface;
import com.ariadnext.idcheckio.sdk.interfaces.result.IdcheckioResult;
import com.ariadnext.idcheckio.sdk.interfaces.result.ips.IpsResultCallback;
import com.ariadnext.idcheckio.sdk.utils.extension.JsonExtensionKt;

import org.json.JSONException;

import java.util.HashMap;


public class IdcheckioActivity extends FragmentActivity implements IdcheckioInteractionInterface, IpsResultCallback {
    private final static int CONTAINER_ID = 123481562;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        FrameLayout rootLayout = new FrameLayout(this);
        rootLayout.setId(CONTAINER_ID);
        rootLayout.setBackground(new ColorDrawable(Color.BLACK));
        setContentView(rootLayout);

        Intent intent = getIntent();
        boolean isOnline = intent.getBooleanExtra("isOnline", false);
        boolean isIps = intent.getBooleanExtra("isIps", false);
        if(isIps) {
            String folderUid = intent.getStringExtra(FOLDER_UID);
            folderUid = (folderUid != null)? folderUid : "";
            /*
             * You can update the IpsCustomization with a DayNightTheme to make update the colors of the sdk.
             * You will have more information about the DayNightTheme in the Developers Guide.
             */
            DayNightTheme dayNightTheme = new DayNightTheme(
                    R.color.idcheckio_sdk_background_color,
                    R.color.idcheckio_sdk_border_color,
                    R.color.idcheckio_sdk_secondary_background_color,
                    R.color.idcheckio_sdk_primary_color,
                    // If you let the title and text color null, it will automatically switch between white and black depending on the background it is on.
                    null,
                    null,
                    // The display mode will be removed in a future update
                    DisplayMode.DEFAULT
            );
            IpsCustomization ipsCustomization = new IpsCustomization(dayNightTheme, Orientation.AUTOMATIC);
            Idcheckio.startIps(this, folderUid, this, ipsCustomization, null);
        } else if(isOnline) {
            IdcheckioView idcheckioView = pushIdcheckioView((HashMap<String, Object>) intent.getSerializableExtra("PARAMS"));
            String online = intent.getStringExtra("ONLINE");
            OnlineContext onlineContext = null;
            if (online != null && !online.isEmpty()) {
                onlineContext = OnlineContext.createFrom(online);
            }
            idcheckioView.startOnline(onlineContext);
        } else {
            IdcheckioView idcheckioView = pushIdcheckioView((HashMap<String, Object>) intent.getSerializableExtra("PARAMS"));
            idcheckioView.start();
        }
    }

    /**
     * Retrieve the params from the json.
     * Create an [IdcheckioView], assign the parameters to the view
     * And then push the view in the fragment manager.
     * @param params a hashmap with all the sdk parameters
     * @return the created [IdcheckioView]
     */
    private IdcheckioView pushIdcheckioView(HashMap<String, Object> params){
        IdcheckioView idcheckioView = ParameterUtils.getIDCheckioViewFromCall(params)
                .listener(this)
                .build();
        getSupportFragmentManager().beginTransaction().replace(CONTAINER_ID, idcheckioView).commit();
        return idcheckioView;
    }

    @Override
    public void onBackPressed(){
        onIdcheckioInteraction(IdcheckioInteraction.ERROR, IdcheckioErrorKt.toErrorMsg(IdcheckioError.CANCELLED_BY_USER,
                "Session cancelled by user.", null));
    }

    @Override
    public void onIdcheckioInteraction(IdcheckioInteraction idcheckioInteraction, Object data) {
        switch (idcheckioInteraction) {
            case RESULT:
                Intent resultIntent = new Intent();
                resultIntent.putExtra("IDCHECKIO_RESULT", JsonExtensionKt.toJson((IdcheckioResult) data));
                setResult(RESULT_OK, resultIntent);
                finish();
                break;
            case ERROR:
                Intent errorIntent = new Intent();
                if (data != null) {
                    errorIntent.putExtra("ERROR_MSG", JsonExtensionKt.toJson((ErrorMsg) data));
                }
                setResult(RESULT_CANCELED, errorIntent);
                finish();
                break;
            default:
                break;
        }
    }

    @Override
    public void onIpsSessionFailure(@NonNull ErrorMsg errorMsg) {
        onIdcheckioInteraction(IdcheckioInteraction.ERROR, errorMsg);
    }

    @Override
    public void onIpsSessionSuccess() {
        // Empty success (the sdk give no result on an ips session success)
        onIdcheckioInteraction(IdcheckioInteraction.RESULT, new IdcheckioResult());
    }
}
