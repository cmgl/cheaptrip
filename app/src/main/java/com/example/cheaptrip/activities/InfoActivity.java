package com.example.cheaptrip.activities;

import android.app.Activity;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.example.cheaptrip.R;
import com.example.cheaptrip.app.CheapTripApp;
import com.example.cheaptrip.views.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class InfoActivity extends Activity {

    BottomNavigationView bottomNavigation;

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
        setContentView(R.layout.activity_info);

        bottomNavigation = findViewById(R.id.bottomNavigationView);
        Navigation.setBottomNavigation(this,bottomNavigation);
        bottomNavigation.getMenu().findItem(R.id.bottom_nav_info).setChecked(true);

        TextView t2 = findViewById(R.id.tv_license);
        t2.setMovementMethod(LinkMovementMethod.getInstance());
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

        bottomNavigation.getMenu().findItem(R.id.bottom_nav_info).setChecked(true);
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
}
