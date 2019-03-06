package com.example.yun.sred.permission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.widget.Toast;

public class permissionCheck {
    String permissionChk;
    boolean result = false;
    Activity activity;
    Context mContext;

    public boolean isCheck(final Activity act, final Context context, String per, String permissionText) {
        permissionChk = per;
        this.mContext = context;
        this.activity = act;


        Log.d("permission", permissionChk);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 사용자의 안드로이드 OS버전이 마시멜로우 이상인지 체크. 맞다면 IF문 내부의 소스코드 작동.
            // 사용자의 단말기에 "전화 걸기" 기능이 허용되어 있는지 확인.
            int permissionResult = mContext.checkSelfPermission(permissionChk); // 해당 퍼미션 체크.

            if (permissionResult == PackageManager.PERMISSION_DENIED) { // 해당 퍼미션 권한여부 체크.

                /*
                 * 해당 권한이 거부된 적이 있는지 유무 판별 해야함.
                 * 거부된 적이 있으면 true, 거부된 적이 없으면 false 리턴
                 */
                if (ActivityCompat.shouldShowRequestPermissionRationale(activity, permissionChk)) { // 거부된 적이 있으면 해당 권한을 사용할 때 상세 내용을 설명. 거부한 적 있으면 true 리턴.
                    AlertDialog.Builder dialog = new AlertDialog.Builder(activity);
                    dialog.setTitle("권한이 필요합니다.")
                            .setMessage("이 기능을 사용하기 위해서는 단말기의 \"" + permissionText + "\"권한이 필요합니다. 계속 하시겠습니까?")
                            .setPositiveButton("네", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                        activity.requestPermissions(new String[]{String.valueOf(permissionChk)}, 1000);
                                        result = true;
                                    }
                                }
                            })
                            .setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    Toast.makeText(activity, "기능을 취소했습니다", Toast.LENGTH_SHORT).show();
                                    result = false;
                                }
                            }).create().show();
                } else {
                    activity.requestPermissions(new String[]{Manifest.permission.CALL_PHONE}, 1000);
                }
            } else {
                result = true;
            }
        } else {
            result = true;
        }
        return result;
    }

    public void sendIntent(Intent i) {
        Intent temp = i;
        activity.startActivity(temp);
    }

}


