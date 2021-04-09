package com.wkp.gestureengine;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.provider.Settings;
import android.text.TextUtils;
import android.view.accessibility.AccessibilityManager;
import java.util.List;

public class AccessibilityUtil {
    /**
     * Check当前辅助服务是否启用
     *
     * @param serviceName serviceName
     * @return 是否启用
     */
    public static boolean checkAccessibilityEnabled(Context context, String serviceName) {
        AccessibilityManager accessibilityManager = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        List<AccessibilityServiceInfo> accessibilityServices =
                accessibilityManager.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_GENERIC);
        for (AccessibilityServiceInfo info : accessibilityServices) {
            ResolveInfo resolveInfo = info.getResolveInfo();
            if (resolveInfo != null) {
                if (resolveInfo.serviceInfo != null) {
                    if (TextUtils.equals(resolveInfo.serviceInfo.name, serviceName)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * 前往开启辅助服务界面
     */
    public static void goAccessSetting(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
