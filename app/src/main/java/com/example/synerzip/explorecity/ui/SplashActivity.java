package com.example.synerzip.explorecity.ui;

import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

//import com.crashlytics.android.Crashlytics;
import com.example.synerzip.explorecity.R;

//import io.fabric.sdk.android.Fabric;

/**
 * Created by Prajakta Patil on 4/1/17.
 * Copyright © 2017 Synerzip. All rights reserved
 */
public class SplashActivity extends AppCompatActivity {
    private static int SPLASH_TIME_OUT = 2000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       // Fabric.with(this, new Crashlytics());
        setContentView(R.layout.activity_splash);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(SplashActivity.this, MapsActivity.class));
                finish();
            }
        }, SPLASH_TIME_OUT);
    }
}
