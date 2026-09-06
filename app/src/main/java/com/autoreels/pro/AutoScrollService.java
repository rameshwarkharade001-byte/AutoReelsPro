package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;

public class AutoScrollService extends AccessibilityService {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isRunning = false;
    private final long scrollInterval = 10000; // 10 Seconds

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                performSwipe();
                handler.postDelayed(this, scrollInterval);
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        isRunning = true;
        handler.removeCallbacks(scrollRunnable);
        handler.postDelayed(scrollRunnable, 3000); // 3 सेकंदात पहिला स्वाइप
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        // Continuous running mode
    }

    private void performSwipe() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        float startX = width / 2.0f;
        float startY = height * 0.80f;
        float endX = width / 2.0f;
        float endY = height * 0.20f;

        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 300));
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
