package com.example.cheaptrip.views;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.util.Pair;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.example.cheaptrip.R;
import com.example.cheaptrip.activities.CalculationActivity;
import com.example.cheaptrip.activities.GasStationActivity;
import com.example.cheaptrip.activities.MainActivity;
import com.example.cheaptrip.activities.MapActivity;

import com.example.cheaptrip.activities.SettingsActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.Set;

public class Navigation {
    public static void setBottomNavigation(final Context context, final BottomNavigationView bottomNavigation){
        bottomNavigation.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {

                Intent intent;
                Bundle optionsBundle = null;

                switch (item.getItemId()) {
                    case R.id.bottom_nav_route:
                        if(context instanceof MainActivity || context instanceof MapActivity || context instanceof CalculationActivity){
                            return false;
                        }
                        intent = new Intent( context.getApplicationContext(), MainActivity.class);
                        break;

                    case R.id.bottom_nav_stations:
                        if(context instanceof GasStationActivity){
                            return false;
                        }
                        intent = new Intent( context.getApplicationContext(), GasStationActivity.class);
                        break;
                    case R.id.bottom_nav_settings:
                        if(context instanceof SettingsActivity){
                            return false;
                        }
                        intent = new Intent(context.getApplicationContext(), SettingsActivity.class);
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
