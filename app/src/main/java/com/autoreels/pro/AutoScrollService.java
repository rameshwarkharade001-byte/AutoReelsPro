package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AutoScrollService extends AccessibilityService {

    private WindowManager windowManager;
    private View floatingView;
    private boolean isPlaying = false;
    private int reelsCount = 0;
    private final long scrollInterval = 14000; // 14-sec balanced video duration
    private final Handler handler = new Handler(Looper.getMainLooper());
    private TextView counterText;
    private Button toggleBtn;

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isPlaying) {
                performSwipeUp();
                reelsCount++;
                updateCounter();
                handler.postDelayed(this, scrollInterval);
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            initFloatingWidget();
        }
    }

    private void initFloatingWidget() {
        if (floatingView != null) return;

        int layoutFlag = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) ?
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                WindowManager.LayoutParams.TYPE_PHONE;

        final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.WRAP_CONTENT,
                WindowManager.LayoutParams.WRAP_CONTENT,
                layoutFlag,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
                PixelFormat.TRANSLUCENT
        );

        params.gravity = Gravity.TOP | Gravity.END;
        params.x = 20;
        params.y = 250;

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.HORIZONTAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        layout.setPadding(25, 15, 25, 15);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#E60F172A"));
        bg.setCornerRadius(60);
        bg.setStroke(2, Color.parseColor("#38BDF8"));
        layout.setBackground(bg);

        // Counter Text
        counterText = new TextView(this);
        counterText.setText("0 Reels");
        counterText.setTextColor(Color.parseColor("#38BDF8"));
        counterText.setTextSize(12);
        counterText.setTypeface(Typeface.DEFAULT_BOLD);
        counterText.setPadding(10, 0, 15, 0);
        layout.addView(counterText);

        // Play / Pause Button
        toggleBtn = new Button(this);
        toggleBtn.setText("▶ Start");
        toggleBtn.setTextSize(12);
        toggleBtn.setTextColor(Color.WHITE);
        toggleBtn.setBackgroundColor(Color.TRANSPARENT);
        toggleBtn.setOnClickListener(v -> {
            isPlaying = !isPlaying;
            if (isPlaying) {
                toggleBtn.setText("⏸ Stop");
                handler.removeCallbacks(scrollRunnable);
                handler.postDelayed(scrollRunnable, scrollInterval);
            } else {
                toggleBtn.setText("▶ Start");
                handler.removeCallbacks(scrollRunnable);
            }
        });
        layout.addView(toggleBtn);

        // Next Button
        Button nextBtn = new Button(this);
        nextBtn.setText("⏭ Next");
        nextBtn.setTextSize(12);
        nextBtn.setTextColor(Color.parseColor("#94A3B8"));
        nextBtn.setBackgroundColor(Color.TRANSPARENT);
        nextBtn.setOnClickListener(v -> {
            performSwipeUp();
            reelsCount++;
            updateCounter();
        });
        layout.addView(nextBtn);

        // Smooth Drag Listener
        layout.setOnTouchListener(new View.OnTouchListener() {
            private int initX, initY;
            private float touchX, touchY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        initX = params.x;
                        initY = params.y;
                        touchX = event.getRawX();
                        touchY = event.getRawY();
                        return false;
                    case MotionEvent.ACTION_MOVE:
                        params.x = initX - (int) (event.getRawX() - touchX);
                        params.y = initY + (int) (event.getRawY() - touchY);
                        if (floatingView != null && windowManager != null) {
                            windowManager.updateViewLayout(floatingView, params);
                        }
                        return true;
                }
                return false;
            }
        });

        floatingView = layout;
        try {
            windowManager.addView(floatingView, params);
        } catch (Exception ignored) {}
    }

    private void updateCounter() {
        if (counterText != null) {
            counterText.setText(reelsCount + " Reels");
        }
        SharedPreferences sp = getSharedPreferences("FlowData", Context.MODE_PRIVATE);
        int total = sp.getInt("reels_count", 0) + 1;
        sp.edit().putInt("reels_count", total).apply();
    }

    private void performSwipeUp() {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Path path = new Path();
        path.moveTo(dm.widthPixels / 2.0f, dm.heightPixels * 0.80f);
        path.lineTo(dm.widthPixels / 2.0f, dm.heightPixels * 0.20f);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, 240));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {}

    @Override
    public void onInterrupt() {
        isPlaying = false;
        handler.removeCallbacks(scrollRunnable);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isPlaying = false;
        handler.removeCallbacks(scrollRunnable);
        if (floatingView != null && windowManager != null) {
            try {
                windowManager.removeView(floatingView);
            } catch (Exception ignored) {}
        }
    }
}
