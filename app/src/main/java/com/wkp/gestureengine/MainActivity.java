package com.wkp.gestureengine;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import com.wkp.runtimepermissions.callback.PermissionCallBack;
import com.wkp.runtimepermissions.util.RuntimePermissionUtil;

public class MainActivity extends AppCompatActivity {
    private boolean checkAccessibility;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (checkAccessibility) {
            checkAccessibility = false;
            if (AccessibilityUtil.checkAccessibilityEnabled(this, GestureAccessibilityService.class.getCanonicalName())) {
                startFloatingVideoService();
            } else {
                Toast.makeText(this, "请给予手势辅助权限", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onGestureButtonClick(final View view) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            RuntimePermissionUtil.checkPermissions(this, RuntimePermissionUtil.CAMERA, new PermissionCallBack() {
                @Override
                public void onCheckPermissionResult(boolean hasPermission) {
                    if (!hasPermission) {
                        Toast.makeText(view.getContext(), "请给予摄像头权限", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    RuntimePermissionUtil.checkPermissions(view.getContext(), RuntimePermissionUtil.STORAGE, new PermissionCallBack() {
                        @Override
                        public void onCheckPermissionResult(boolean hasPermissionStorage) {
                            if (!hasPermissionStorage) {
                                Toast.makeText(view.getContext(), "请给予存储权限", Toast.LENGTH_SHORT).show();
                                return;
                            }
                            startFloatingVideoService();
                        }
                    });
                }
            });
        } else {
            startFloatingVideoService();
        }
    }

    public void startFloatingVideoService() {
        if (FloatingVideoService.isStarted) {
            return;
        }
        // 辅助服务权限
        if (!AccessibilityUtil.checkAccessibilityEnabled(this, GestureAccessibilityService.class.getCanonicalName())) {
            checkAccessibility = true;
            Toast.makeText(this, "请开启手势辅助权限", Toast.LENGTH_SHORT).show();
            AccessibilityUtil.goAccessSetting(this);
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Toast.makeText(this, "当前无悬浮窗权限，请授权", Toast.LENGTH_SHORT).show();
            startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName())), 1001);
        } else {
            startService(new Intent(MainActivity.this, FloatingVideoService.class));
            finish();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1001) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
                Toast.makeText(this, "悬浮窗授权失败", Toast.LENGTH_SHORT).show();
            } else {
                startService(new Intent(MainActivity.this, FloatingVideoService.class));
                finish();
            }
        }
    }
}