package com.example.cheaptrip.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.views.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity  extends Activity {
    SharedPreferences mSharedPreferences;
    private BottomNavigationView bottomNavigation;

    private EditText mEditTankerKey;
    private EditText mEditORSKey;

    public static final String CONFIG_KEY = "CheapTripConf";
    public static final String TANKER_CONFIG_KEY = "TANKER_KEY";
    private static final String ORS_CONFIG_KEY = "ORS_KEY";

    /**
     * This function will be called on Activity creation
     * It takes care of initializing the views attached to the layout
     * and starts calculating routes with a gas station as intermediate stop
     *
     * @param savedInstanceState    Bundle, containing the state of the activity
     */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        mEditTankerKey = findViewById(R.id.edit_tanker_key);
        mEditORSKey = findViewById(R.id.edit_ors_key);
        bottomNavigation = findViewById(R.id.bottomNavigationView);
        Navigation.setBottomNavigation(this,bottomNavigation);
        bottomNavigation.getMenu().findItem(R.id.bottom_nav_settings).setChecked(true);

        mSharedPreferences = getApplicationContext().getSharedPreferences(CONFIG_KEY, 0);
    }

    /**
     * Called on Destruction of the Activity
     * The Activity gets removed from the stack -> registers removal to the app
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }
    /**
     * Called on Resume of the Activity
     * The Activity will be added on top of the stack (-> registration) to the app
     */
    public void onResume(){
        super.onResume();

        bottomNavigation.getMenu().findItem(R.id.bottom_nav_settings).setChecked(true);
        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        cheapTripApp .setCurrentActivity( this ) ;
    }

    /**
     * Called on Pause of the Activity
     * The Activity will be removed from top of the stack (-> registration to the app)
     */
    public void onPause(){
        overridePendingTransition(0, 0);

        super.onPause();

        CheapTripApp cheapTripApp = (CheapTripApp) getApplication();
        Activity currActivity = cheapTripApp.getCurrentActivity() ;

        if ( this .equals(currActivity))
            cheapTripApp.setCurrentActivity( null ) ;
    }

    public void onApiKeyConfirmed(View view){
        if(mSharedPreferences == null){
            Log.e("CHEAPTRIP","SharedPreferences is null");
            return;

        }

        SharedPreferences.Editor editor;
        switch (view.getId()){
            /*==================================================
             * Apply TankerKoenig Key
             *==================================================*/
            case R.id.btn_tanker_key_confirm:
                String API_KEY = "ad5b5cac-db85-4516-832d-1bc90df23946";

                mEditTankerKey.setText(API_KEY);
                editor = mSharedPreferences.edit();
                String tankerKey = mEditORSKey.getText().toString();

                if(!tankerKey.isEmpty())
                    editor.putString(TANKER_CONFIG_KEY, tankerKey);
                editor.apply();

                Toast.makeText(this, "Api Key Applied", Toast.LENGTH_SHORT).show();
                break;
            /*==================================================
             * Apply OpenRoutService Key
             *==================================================*/
            case R.id.btn_ors_key_confirm:
                editor = mSharedPreferences.edit();
                String orsKey = mEditORSKey.getText().toString();

                if(!orsKey.isEmpty())
                    editor.putString(ORS_CONFIG_KEY, orsKey);

                Toast.makeText(this, "Api Key Applied", Toast.LENGTH_SHORT).show();
                editor.apply();
                editor.commit();
        }
    }
}
