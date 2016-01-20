package com.example.blockcanary;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SplashActivity.this.setTheme(R.style.ModuThreeHandsomeTheme_WhiteActivity);
        super.onCreate(savedInstanceState);
        startActivity(new Intent(this, DemoActivity.class));
        SplashActivity.this.finish();
        overridePendingTransition(0, 0);
    }
}
