package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

public class AutoScrollService extends AccessibilityService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private long scrollInterval = 12000; // 12 Seconds per reel

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                performSwipe();
                handler.postDelayed(this, scrollInterval);
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString().toLowerCase();

        boolean isTargetApp = pkg.contains("instagram") || 
                              pkg.contains("youtube") || 
                              pkg.contains("katana") || 
                              pkg.contains("facebook");

        if (isTargetApp) {
            if (!isRunning) {
                isRunning = true;
                handler.removeCallbacks(scrollRunnable);
                handler.postDelayed(scrollRunnable, scrollInterval);
            }
        } else {
            if (isRunning) {
                isRunning = false;
                handler.removeCallbacks(scrollRunnable);
            }
        }
    }

    private void performSwipe() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        float startX = width / 2f;
        float startY = height * 0.82f;
        float endX = width / 2f;
        float endY = height * 0.18f;

        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 240));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onInterrupt() {
        isRunning = false;
        handler.removeCallbacks(scrollRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isRunning = false;
        handler.removeCallbacks(scrollRunnable);
    }
}
