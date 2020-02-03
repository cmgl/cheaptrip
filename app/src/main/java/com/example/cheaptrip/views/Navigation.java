package com.example.cheaptrip.views;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;
import com.example.cheaptrip.activities.CalculationActivity;
import com.example.cheaptrip.activities.GasStationActivity;
import com.example.cheaptrip.activities.InfoActivity;
import com.example.cheaptrip.activities.MainActivity;
import com.example.cheaptrip.activities.MapActivity;

import com.example.cheaptrip.activities.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * This classes purpose is to provide bottom navigation bar functionality
 * to it's caller.
 * Due to not being able to use Fragments (Problems with the MapView in specific MapViews),
 * this is a Solution to have a bottom navigation with activities.
 *
 * This class takes care of starting other activities based on the callers class.
 *
 * Its function is comparable with a state machine
 *
 */
public class Navigation {
    /**
     * This function takes care of handling bottom bar click events.
     * This is done by setting the onNavigationItemSelectedListener on the bottombar provided by argument.
     *
     * Based on the caller it will
     *  1)  either start another activity
     *      (if the caller's class is not the the Activity to be started)
     *
     *  2) or do nothing when the requested activity (identified by button press) is the same)
     *
     * @param context           Context of the Activity in which the bottom navigation button was pressed
     * @param bottomNavigation  an instance of the bottom bar in the Activity identified by context.
     */
    public static void setBottomNavigation(final Context context, final BottomNavigationView bottomNavigation){
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Intent intent;
                Bundle optionsBundle = null;

                switch (item.getItemId()) {
                    /*==========================================================================
                     * Navigation Route clicked
                     *==========================================================================*/
                    case R.id.bottom_nav_route:
                        if(context instanceof MainActivity || context instanceof MapActivity || context instanceof CalculationActivity){
                            return false;
                        }
                        intent = new Intent( context.getApplicationContext(), MainActivity.class);
                        break;
                    /*===========================================================================
                     * Gas-Stations nearby clicked (starting GasStationActivity
                     *============================================================================*/
                    case R.id.bottom_nav_stations:
                        if(context instanceof GasStationActivity){
                            return false;
                        }
                        intent = new Intent( context.getApplicationContext(), GasStationActivity.class);
                        break;
                    /*===========================================================================
                     * Open Settings
                     *===========================================================================*/
                    case R.id.bottom_nav_settings:
                        if(context instanceof SettingsActivity){
                            return false;
                        }
                        intent = new Intent(context.getApplicationContext(), SettingsActivity.class);
                        break;


                    case R.id.bottom_nav_info:
                        if(context instanceof InfoActivity){
                            return false;
                        }
                        intent = new Intent(context.getApplicationContext(), InfoActivity.class);
                        break;

                    default: return false;
                }

                intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                context.startActivity(intent,optionsBundle);
                return true;
            }
        });
    }
}
