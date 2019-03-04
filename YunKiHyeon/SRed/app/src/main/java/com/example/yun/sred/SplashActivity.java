package com.example.yun.sred;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.example.yun.sred.permission.permissionCheck;

import static android.Manifest.permission.RECORD_AUDIO;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        permissionCheck permissionCheck = null;
        Context context = getApplication();
    //    permissionCheck.isCheck(SplashActivity.this,context,"RECORD_AUDIO", "오디오" );
        //   permissionCheck.isCheck(SplashActivity.this,context,"WRITE_EXTERNAL_STORAGE", "외부저장소" );
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

        startActivity(intent);
        finish();
    }
}
