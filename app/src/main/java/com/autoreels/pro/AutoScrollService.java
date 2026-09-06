package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;

public class AutoScrollService extends AccessibilityService {

    private Handler handler = new Handler(Looper.getMainLooper());
    private boolean isAutoScrollActive = false;
    private long scrollDelay = 12000; // डीफॉल्ट १२ सेकंद
    private WindowManager windowManager;
    private LinearLayout floatingControlView;

    private Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isAutoScrollActive) {
                performSwipeUp();
                handler.postDelayed(this, scrollDelay);
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        createFloatingControl();
    }

    private void createFloatingControl() {
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        floatingControlView = new LinearLayout(this);
        floatingControlView.setOrientation(LinearLayout.VERTICAL);
        floatingControlView.setBackgroundColor(Color.parseColor("#CC111827"));
        floatingControlView.setPadding(15, 15, 15, 15);

        Button toggleBtn = new Button(this);
        toggleBtn.setText("▶ Start");
        toggleBtn.setBackgroundColor(Color.parseColor("#10B981"));
        toggleBtn.setTextColor(Color.WHITE);

        Button nextBtn = new Button(this);
        nextBtn.setText("⏭ Next");
        nextBtn.setBackgroundColor(Color.parseColor("#3B82F6"));
        nextBtn.setTextColor(Color.WHITE);

        toggleBtn.setOnClickListener(v -> {
            if (isAutoScrollActive) {
                isAutoScrollActive = false;
                handler.removeCallbacks(scrollRunnable);
                toggleBtn.setText("▶ Start");
                toggleBtn.setBackgroundColor(Color.parseColor("#10B981"));
            } else {
                isAutoScrollActive = true;
                handler.post(scrollRunnable);
                toggleBtn.setText("⏸ Pause");
                toggleBtn.setBackgroundColor(Color.parseColor("#EF4444"));
            }
        });

        nextBtn.setOnClickListener(v -> performSwipeUp());

        floatingControlView.addView(toggleBtn);
        floatingControlView.addView(nextBtn);

        int layoutType = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O 
                ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY 
                : WindowManager.LayoutParams.TYPE_PHONE;

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutType,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 20;
        params.y = 200;

        try {
            windowManager.addView(floatingControlView, params);
        } catch (Exception ignored) {}
    }

    private void performSwipeUp() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        float startX = width / 2f;
        float startY = height * 0.78f;
        float endX = width / 2f;
        float endY = height * 0.22f;

        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 250));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        isAutoScrollActive = false;
        handler.removeCallbacks(scrollRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isAutoScrollActive = false;
        handler.removeCallbacks(scrollRunnable);
        if (floatingControlView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingControlView);
            } catch (Exception ignored) {}
        }
    }
}
