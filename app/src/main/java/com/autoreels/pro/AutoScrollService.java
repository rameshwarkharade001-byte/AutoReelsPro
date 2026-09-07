package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.Intent;
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
    private View floatingSidebar;
    private boolean isContinuousScrolling = false;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Button continuousBtn;

    private final Runnable loopScroll = new Runnable() {
        @Override
        public void run() {
            if (isContinuousScrolling) {
                SharedPreferences sp = getSharedPreferences("ScrollPrefs", Context.MODE_PRIVATE);
                boolean invert = sp.getBoolean("invert_scroll", false);
                performScrollGesture(!invert, 220); // Swipe next
                
                int delaySecs = sp.getInt("jump_delay", 14);
                handler.postDelayed(this, delaySecs * 1000L);
            }
        }
    };

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(this)) {
            buildFullFloatingSidebar();
        }
    }

    private void buildFullFloatingSidebar() {
        if (floatingSidebar != null) return;

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

        params.gravity = Gravity.TOP | Gravity.START;
        params.x = 0;
        params.y = 300;

        LinearLayout bar = new LinearLayout(this);
        bar.setOrientation(LinearLayout.VERTICAL);
        bar.setGravity(Gravity.CENTER_HORIZONTAL);
        bar.setPadding(8, 14, 8, 14);

        SharedPreferences sp = getSharedPreferences("ScrollPrefs", Context.MODE_PRIVATE);
        String colorHex = sp.getString("widget_color", "#E620B2AA");

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor(colorHex));
        bg.setCornerRadius(35);
        bg.setStroke(2, Color.parseColor("#FFFFFF"));
        bar.setBackground(bg);

        // 1. Drag Bar Handle (↔)
        TextView drag = new TextView(this);
        drag.setText("↔");
        drag.setTextColor(Color.WHITE);
        drag.setTextSize(14);
        drag.setGravity(Gravity.CENTER);
        drag.setPadding(0, 2, 0, 8);
        bar.addView(drag);

        // 2. Fast Jump Up (⏫)
        bar.addView(makeIconButton("⏫", v -> performScrollGesture(false, 150)));

        // 3. Slow Scroll Up (▲)
        bar.addView(makeIconButton("▲", v -> performScrollGesture(false, 350)));

        // 4. Continuous Auto-Scroll Toggle (▶ / ⏸)
        continuousBtn = makeIconButton("▶", v -> {
            isContinuousScrolling = !isContinuousScrolling;
            if (isContinuousScrolling) {
                continuousBtn.setText("⏸");
                handler.removeCallbacks(loopScroll);
                handler.post(loopScroll);
            } else {
                continuousBtn.setText("▶");
                handler.removeCallbacks(loopScroll);
            }
        });
        bar.addView(continuousBtn);

        // 5. Slow Scroll Down (▼)
        bar.addView(makeIconButton("▼", v -> performScrollGesture(true, 350)));

        // 6. Fast Jump Down / Next Reel (⏬)
        bar.addView(makeIconButton("⏬", v -> performScrollGesture(true, 150)));

        // 7. Settings Shortcut (⚙)
        bar.addView(makeIconButton("⚙", v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
        }));

        // 8. Collapse / Hide (✕)
        bar.addView(makeIconButton("✕", v -> {
            isContinuousScrolling = false;
            handler.removeCallbacks(loopScroll);
            bar.setVisibility(View.GONE);
        }));

        // Touch Dragging
        bar.setOnTouchListener(new View.OnTouchListener() {
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
                        params.x = initX + (int) (event.getRawX() - touchX);
                        params.y = initY + (int) (event.getRawY() - touchY);
                        if (floatingSidebar != null && windowManager != null) {
                            windowManager.updateViewLayout(floatingSidebar, params);
                        }
                        return true;
                }
                return false;
            }
        });

        floatingSidebar = bar;
        try {
            windowManager.addView(floatingSidebar, params);
        } catch (Exception ignored) {}
    }

    private Button makeIconButton(String label, View.OnClickListener listener) {
        Button b = new Button(this);
        b.setText(label);
        b.setTextColor(Color.WHITE);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setBackgroundColor(Color.TRANSPARENT);
        b.setPadding(0, 6, 0, 6);
        b.setLayoutParams(new LinearLayout.LayoutParams(90, 85));
        b.setOnClickListener(listener);
        return b;
    }

    private void performScrollGesture(boolean scrollDown, int durationMs) {
        DisplayMetrics dm = getResources().getDisplayMetrics();
        Path path = new Path();

        float startY = scrollDown ? dm.heightPixels * 0.80f : dm.heightPixels * 0.22f;
        float endY = scrollDown ? dm.heightPixels * 0.20f : dm.heightPixels * 0.78f;
        float x = dm.widthPixels / 2.0f;

        path.moveTo(x, startY);
        path.lineTo(x, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(path, 0, durationMs));
        dispatchGesture(builder.build(), null, null);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString().toLowerCase();

        SharedPreferences sp = getSharedPreferences("ScrollPrefs", Context.MODE_PRIVATE);
        boolean isGlobal = sp.getBoolean("global_scroll", false);

        if (isGlobal) {
            if (floatingSidebar != null) floatingSidebar.setVisibility(View.VISIBLE);
        } else {
            // Strict App Matching (Instagram, Shorts, Facebook)
            boolean match = pkg.contains("instagram") || pkg.contains("youtube") || pkg.contains("katana") || pkg.contains("facebook");
            if (floatingSidebar != null) {
                floatingSidebar.setVisibility(match ? View.VISIBLE : View.GONE);
            }
            if (!match && isContinuousScrolling) {
                isContinuousScrolling = false;
                handler.removeCallbacks(loopScroll);
                if (continuousBtn != null) continuousBtn.setText("▶");
            }
        }
    }

    @Override
    public void onInterrupt() {
        isContinuousScrolling = false;
        handler.removeCallbacks(loopScroll);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isContinuousScrolling = false;
        handler.removeCallbacks(loopScroll);
        if (floatingSidebar != null && windowManager != null) {
            try {
                windowManager.removeView(floatingSidebar);
            } catch (Exception ignored) {}
        }
    }
            }
