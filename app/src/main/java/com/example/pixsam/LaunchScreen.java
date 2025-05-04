package com.example.pixsam;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import androidx.appcompat.app.AppCompatActivity;

@SuppressLint("CustomSplashScreen")
public class LaunchScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.launch_screen);
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }
        final Intent i = new Intent(LaunchScreen.this, SavedDrawings.class);
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            startActivity(i);
            finish();
        }, 1500);
    }
}