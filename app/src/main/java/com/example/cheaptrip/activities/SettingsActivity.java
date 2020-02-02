package com.example.cheaptrip.activities;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.example.cheaptrip.R;
import com.example.cheaptrip.views.Navigation;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SettingsActivity  extends Activity {
    SharedPreferences sharedpreferences;
    private BottomNavigationView bottomNavigation;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        bottomNavigation = findViewById(R.id.bottomNavigationView);
        bottomNavigation.setSelectedItemId(R.id.bottom_nav_stations);
        Navigation.setBottomNavigation(this,bottomNavigation);
    }
}
