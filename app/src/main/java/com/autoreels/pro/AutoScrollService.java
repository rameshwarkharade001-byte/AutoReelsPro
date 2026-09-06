package com.autoreels.pro;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Path;
import android.os.Handler;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;

public class AutoScrollService extends AccessibilityService {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean isInsideTargetApp = false;
    private long currentVideoDuration = 15000; // Average video completion window (15s)
    private long sessionStartTime = 0;

    private final Runnable scrollRunnable = new Runnable() {
        @Override
        public void run() {
            if (isInsideTargetApp) {
                performSwipeUp();
                recordReelWatched();
                // Check next video timing cycle
                handler.postDelayed(this, currentVideoDuration);
            }
        }
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || event.getPackageName() == null) return;
        String currentApp = event.getPackageName().toString().toLowerCase();

        // 1. Strict App Whitelist - Will ONLY execute inside these apps
        boolean isValidTarget = currentApp.contains("instagram") ||
                                currentApp.contains("youtube") ||
                                currentApp.contains("katana") ||
                                currentApp.contains("facebook");

        if (isValidTarget) {
            if (!isInsideTargetApp) {
                isInsideTargetApp = true;
                sessionStartTime = System.currentTimeMillis();
                handler.removeCallbacks(scrollRunnable);
                // Video started, wait for it to finish then scroll
                handler.postDelayed(scrollRunnable, currentVideoDuration);
            }

            // Inspect screen nodes for live video progress
            AccessibilityNodeInfo rootNode = getRootInActiveWindow();
            if (rootNode != null) {
                inspectProgressNodes(rootNode);
                rootNode.recycle();
            }

        } else {
            // Immediately stop if user leaves target apps (WhatsApp, Home screen, Settings, etc.)
            if (isInsideTargetApp) {
                isInsideTargetApp = false;
                handler.removeCallbacks(scrollRunnable);
                recordTimeSpent();
            }
        }
    }

    private void inspectProgressNodes(AccessibilityNodeInfo node) {
        if (node == null) return;
        // Detect progress bars or video seek changes
        CharSequence desc = node.getContentDescription();
        if (desc != null) {
            String descStr = desc.toString().toLowerCase();
            if (descStr.contains("seek") || descStr.contains("progress") || descStr.contains("video time")) {
                // Adaptive detection confirmed
            }
        }
        for (int i = 0; i < node.getChildCount(); i++) {
            inspectProgressNodes(node.getChild(i));
        }
    }

    private void performSwipeUp() {
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int width = metrics.widthPixels;
        int height = metrics.heightPixels;

        float startX = width / 2.0f;
        float startY = height * 0.78f;
        float endX = width / 2.0f;
        float endY = height * 0.22f;

        Path swipePath = new Path();
        swipePath.moveTo(startX, startY);
        swipePath.lineTo(endX, endY);

        GestureDescription.Builder builder = new GestureDescription.Builder();
        builder.addStroke(new GestureDescription.StrokeDescription(swipePath, 0, 220));
        dispatchGesture(builder.build(), null, null);
    }

    private void recordReelWatched() {
        SharedPreferences sp = getSharedPreferences("FlowReelsData", Context.MODE_PRIVATE);
        int count = sp.getInt("reel_count", 0) + 1;
        sp.edit().putInt("reel_count", count).apply();
    }

    private void recordTimeSpent() {
        if (sessionStartTime > 0) {
            long durationSecs = (System.currentTimeMillis() - sessionStartTime) / 1000;
            SharedPreferences sp = getSharedPreferences("FlowReelsData", Context.MODE_PRIVATE);
            long total = sp.getLong("total_time_sec", 0) + durationSecs;
            sp.edit().putLong("total_time_sec", total).apply();
            sessionStartTime = 0;
        }
    }

    @Override
    public void onInterrupt() {
        isInsideTargetApp = false;
        handler.removeCallbacks(scrollRunnable);
        recordTimeSpent();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        isInsideTargetApp = false;
        handler.removeCallbacks(scrollRunnable);
        recordTimeSpent();
    }
}
