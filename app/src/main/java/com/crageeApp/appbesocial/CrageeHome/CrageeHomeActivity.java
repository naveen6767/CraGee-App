package com.crageeApp.appbesocial.CrageeHome;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import com.crageeApp.appbesocial.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class CrageeHomeActivity extends AppCompatActivity {


    private BottomNavigationView bottomNavigation;
    private NavController navController;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cragee_home);

        navController = Navigation.findNavController(this,R.id.nav_host_fragment);
        bottomNavigation= findViewById(R.id.bottom_navigation);
        NavigationUI.setupWithNavController(bottomNavigation, navController);

    }

    @Override
    public boolean onSupportNavigateUp() {
        return navController.navigateUp();
    }
}