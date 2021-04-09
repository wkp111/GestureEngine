package com.wkp.gestureengine;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;


public class GestureAccessibilityService extends AccessibilityService {
    private volatile boolean eventing;
    private static final Handler HANDLER = new Handler(Looper.getMainLooper());

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        int eventType = event.getEventType();
        if (eventType == AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED && FloatingVideoService.updating) {
            if (eventing) {
                return;
            }
            eventing = true;
            // 向上滚动
            performScrollForward();
            HANDLER.postDelayed(new Runnable() {
                @Override
                public void run() {
                    eventing = false;
                    FloatingVideoService.resetUpdating();
                    NotificationUtil.removeNotification(getApplicationContext());
                }
            }, 1000);
        }
    }

    @Override
    public void onInterrupt() {
    }

    /**
     * 模拟上滑操作
     */
    public void performScrollForward() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Path path = new Path();
            path.moveTo(600,2000);
            path.lineTo(600,100);
            dispatchGesture(new GestureDescription.Builder()
                    .addStroke(new GestureDescription.StrokeDescription(path, 100, 50))
                    .build(), new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription gestureDescription) {
                    super.onCompleted(gestureDescription);
                }

                @Override
                public void onCancelled(GestureDescription gestureDescription) {
                    super.onCancelled(gestureDescription);
                }
            }, null);
            return;
        }
        AccessibilityNodeInfo nodeInfo = findFocusedInfo(getRootInActiveWindow());
        while (nodeInfo != null) {
            boolean performAction = nodeInfo.performAction(AccessibilityNodeInfo.ACTION_SCROLL_FORWARD);
            if (performAction) {
                break;
            }
            nodeInfo = nodeInfo.getParent();
        }
    }

    /**
     * 查找当前聚焦的节点
     * @param root 根节点
     * @return 聚焦节点
     */
    public AccessibilityNodeInfo findFocusedInfo(AccessibilityNodeInfo root) {
        if (root == null) {
            return null;
        }
        if (root.isFocused()) {
            return root;
        }
        for (int i = 0; i < root.getChildCount(); i++) {
            AccessibilityNodeInfo child = root.getChild(i);
            if (child.isFocused()) {
                return child;
            }
            AccessibilityNodeInfo nodeInfo = findFocusedInfo(child);
            if (nodeInfo != null) {
                return nodeInfo;
            }
        }
        return null;
    }
}
