package com.jqorz.soundpassworddetection.frame;

import android.Manifest;
import android.content.Intent;
import android.support.annotation.NonNull;

import com.iflytek.cloud.SpeechUtility;
import com.jqorz.soundpassworddetection.R;
import com.jqorz.soundpassworddetection.base.BaseActivity;
import com.yanzhenjie.permission.AndPermission;
import com.yanzhenjie.permission.PermissionListener;
import com.yanzhenjie.permission.Rationale;
import com.yanzhenjie.permission.RationaleListener;

import java.util.List;


public class App_Main extends BaseActivity {
    private final int PERMISSION_CODE = 2222;

    @Override
    protected void init() {
        SpeechUtility.createUtility(this, "appid=" + getString(R.string.app_id));
        initPermission();

    }


    private void initPermission() {
        AndPermission.with(App_Main.this)

                .permission(Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA,
                        Manifest.permission.LOCATION_HARDWARE, Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.WRITE_SETTINGS, Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.RECORD_AUDIO, Manifest.permission.READ_CONTACTS)
                .requestCode(PERMISSION_CODE)
                .rationale(new RationaleListener() {
                               @Override
                               public void showRequestPermissionRationale(int requestCode, Rationale rationale) {
                                   AndPermission.rationaleDialog(App_Main.this, rationale).show();
                               }
                           }
                )
                .callback(new PermissionListener() {
                    @Override
                    public void onSucceed(int requestCode, @NonNull List<String> grantedPermissions) {
                        if (requestCode == PERMISSION_CODE) {
                            App_Main.this.finish();
                            startActivity(new Intent(App_Main.this, App_FaceTracking.class));
                        }
                    }

                    @Override
                    public void onFailed(int requestCode, @NonNull List<String> deniedPermissions) {
                        if (AndPermission.hasPermission(App_Main.this, Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)) {
                            App_Main.this.finish();
                            startActivity(new Intent(App_Main.this, App_FaceTracking.class));
                        } else if (requestCode == PERMISSION_CODE) {// 是否有不再提示并拒绝的权限。
                            AndPermission.defaultSettingDialog(App_Main.this, 400).show();
                        }
                    }
                }).start();
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.app_main;
    }
}
