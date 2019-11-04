package com.example.yun.sred;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.TypefaceProvider;

public class SplashActivity extends AppCompatActivity  implements ActivityCompat.OnRequestPermissionsResultCallback{
    private static final String TAG = "SplashActivity";
    private final int PERMISSIONS_REQUEST = 0;
    private final String[] permissionList = new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();

        setContentView(R.layout.activity_splash);
        requestPermission();
    }

    /**
     * 원래 권한 설정은 그 권한을 필요로 할 때 요청해야한다.
     * 하지만 지금은 필요한 레코드, 스토리지 접근 권한을 모두 요청하게 코드를 작성했다.
     */
    void requestPermission() {

         try {
             if (Build.VERSION.SDK_INT >= 23) {
                 //오디오, 스토리지 권한 체크
                 if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
                     ActivityCompat.requestPermissions(this, permissionList, PERMISSIONS_REQUEST);
                 }
             } else {
                 Toast.makeText(this, "android version is below 23", Toast.LENGTH_LONG).show();
             }
         }
         catch (Exception e){
             e.printStackTrace();
         }
    }
    //권한확인 콜백 메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            switch (requestCode) {
                case PERMISSIONS_REQUEST: {

                    if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(TAG, "permissions enable");
                        Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                        intent.putExtra("activityName", "splash");
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(this, "Permission Denied", Toast.LENGTH_LONG).show();
                        finish();
                    }
                    return;
                }
            }
        }

    }

