package com.wkp.gestureengine;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import androidx.annotation.Nullable;
import com.huawei.hms.mlsdk.common.LensEngine;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzer;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerFactory;
import com.huawei.hms.mlsdk.gesture.MLGestureAnalyzerSetting;
import java.io.IOException;

public class FloatingVideoService extends Service {
    private static final String TAG = "FloatingVideoService";
    public static boolean isStarted;
    private WindowManager windowManager;
    private WindowManager.LayoutParams layoutParams;
    private View displayView;
    private MLGestureAnalyzer mAnalyzer;
    private LensEnginePreview mSurfaceView;
    public static volatile boolean updating;

    @Override
    public void onCreate() {
        super.onCreate();
        isStarted = true;
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        layoutParams = new WindowManager.LayoutParams();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            layoutParams.type = WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY;
        } else {
            layoutParams.type = WindowManager.LayoutParams.TYPE_PHONE;
        }
        layoutParams.format = PixelFormat.RGBA_8888;
        layoutParams.gravity = Gravity.START | Gravity.TOP;
        layoutParams.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        layoutParams.width = 100;
        layoutParams.height = 100;
        layoutParams.x = 300;
        layoutParams.y = 10;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        showFloatingWindow();
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mAnalyzer != null) {
            mAnalyzer.stop();
        }
        if (mSurfaceView != null) {
            mSurfaceView.release();
        }
    }

    public void clickSpace() {
        if (updating) {
            return;
        }
        updating = true;
        NotificationUtil.sendNotification(getApplicationContext());
    }

    public static void resetUpdating() {
        updating = false;
    }

    @SuppressLint("NewApi")
    private void showFloatingWindow() {
        if (Settings.canDrawOverlays(this)) {
            LayoutInflater layoutInflater = LayoutInflater.from(this);
            displayView = layoutInflater.inflate(R.layout.floating_video, null);
            displayView.setOnTouchListener(new FloatingOnTouchListener());
            mSurfaceView = displayView.findViewById(R.id.surface_view);
            windowManager.addView(displayView, layoutParams);
            MLGestureAnalyzerSetting setting = new MLGestureAnalyzerSetting.Factory().create();
            mAnalyzer = MLGestureAnalyzerFactory.getInstance().getGestureAnalyzer(setting);
            mAnalyzer.setTransactor(new HandKeypointTransactor(this));
            LensEngine lensEngine = new LensEngine.Creator(getApplicationContext(), mAnalyzer)
                    .setLensType(LensEngine.FRONT_LENS)
                    .applyDisplayDimension(640, 480)
                    .applyFps(25.0f)
                    .enableAutomaticFocus(true)
                    .create();
            try {
                mSurfaceView.start(lensEngine);
            } catch (IOException e) {
                Log.i(TAG, "showFloatingWindow IOException: " + e);
            }
        }
    }

    private class FloatingOnTouchListener implements View.OnTouchListener {
        private int x;
        private int y;

        @Override
        public boolean onTouch(View view, MotionEvent event) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    x = (int) event.getRawX();
                    y = (int) event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    int nowX = (int) event.getRawX();
                    int nowY = (int) event.getRawY();
                    int movedX = nowX - x;
                    int movedY = nowY - y;
                    x = nowX;
                    y = nowY;
                    layoutParams.x = layoutParams.x + movedX;
                    layoutParams.y = layoutParams.y + movedY;
                    windowManager.updateViewLayout(view, layoutParams);
                    break;
                default:
                    break;
            }
            return true;
        }
    }
}
