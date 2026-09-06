package com.autoreels.pro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {

    private TextView countText;
    private TextView timeText;
    private Handler updateHandler = new Handler(Looper.getMainLooper());

    private Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            updateStats();
            updateHandler.postDelayed(this, 1000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(40, 80, 40, 40);
        root.setBackgroundColor(Color.parseColor("#090D16"));

        // Header Title
        TextView header = new TextView(this);
        header.setText("FlowReels");
        header.setTextSize(26);
        header.setTypeface(Typeface.DEFAULT_BOLD);
        header.setTextColor(Color.WHITE);
        root.addView(header);

        TextView subHeader = new TextView(this);
        subHeader.setText("Smart Auto-Scroll & Reels Tracker");
        subHeader.setTextSize(13);
        subHeader.setTextColor(Color.parseColor("#64748B"));
        subHeader.setPadding(0, 5, 0, 50);
        root.addView(subHeader);

        // Stats Row (Cards)
        LinearLayout statsRow = new LinearLayout(this);
        statsRow.setOrientation(LinearLayout.HORIZONTAL);
        statsRow.setWeightSum(2.0f);

        // Reel Count Card
        LinearLayout countCard = createMetricCard("REELS SCROLLED");
        countText = new TextView(this);
        countText.setText("0");
        countText.setTextSize(28);
        countText.setTypeface(Typeface.DEFAULT_BOLD);
        countText.setTextColor(Color.parseColor("#38BDF8"));
        countCard.addView(countText);
        statsRow.addView(countCard);

        // Spacer
        TextView spacer = new TextView(this);
        spacer.setWidth(25);
        statsRow.addView(spacer);

        // Active Time Card
        LinearLayout timeCard = createMetricCard("WATCH TIME");
        timeText = new TextView(this);
        timeText.setText("0m");
        timeText.setTextSize(28);
        timeText.setTypeface(Typeface.DEFAULT_BOLD);
        timeText.setTextColor(Color.parseColor("#34D399"));
        timeCard.addView(timeText);
        statsRow.addView(timeCard);

        root.addView(statsRow);

        // Active Target Apps Status
        LinearLayout statusBox = new LinearLayout(this);
        statusBox.setOrientation(LinearLayout.VERTICAL);
        statusBox.setPadding(35, 35, 35, 35);
        statusBox.setBackground(getRoundedDrawable("#131B2E", 20));
        
        LinearLayout.LayoutParams statusParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        statusParams.setMargins(0, 40, 0, 40);
        statusBox.setLayoutParams(statusParams);

        TextView targetTitle = new TextView(this);
        targetTitle.setText("SMART SHIELD ACTIVE");
        targetTitle.setTextSize(11);
        targetTitle.setTypeface(Typeface.DEFAULT_BOLD);
        targetTitle.setTextColor(Color.parseColor("#94A3B8"));
        statusBox.addView(targetTitle);

        TextView targetDesc = new TextView(this);
        targetDesc.setText("Instagram  •  YouTube Shorts  •  Facebook");
        targetDesc.setTextSize(14);
        targetDesc.setTextColor(Color.WHITE);
        targetDesc.setPadding(0, 8, 0, 8);
        statusBox.addView(targetDesc);

        TextView note = new TextView(this);
        note.setText("Zero screen touch outside target apps. Swipes strictly when video completes.");
        note.setTextSize(12);
        note.setTextColor(Color.parseColor("#475569"));
        statusBox.addView(note);

        root.addView(statusBox);

        // Primary Engine Button
        Button engineBtn = new Button(this);
        engineBtn.setText("OPEN ENGINE ACCESS");
        engineBtn.setTextSize(14);
        engineBtn.setTypeface(Typeface.DEFAULT_BOLD);
        engineBtn.setTextColor(Color.parseColor("#090D16"));
        engineBtn.setBackground(getRoundedDrawable("#38BDF8", 16));
        engineBtn.setPadding(0, 35, 0, 35);

        engineBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        root.addView(engineBtn);
        setContentView(root);
    }

    private LinearLayout createMetricCard(String label) {
        LinearLayout card = new LinearLayout(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        card.setLayoutParams(params);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(35, 35, 35, 35);
        card.setBackground(getRoundedDrawable("#131B2E", 20));

        TextView lbl = new TextView(this);
        lbl.setText(label);
        lbl.setTextSize(10);
        lbl.setTypeface(Typeface.DEFAULT_BOLD);
        lbl.setTextColor(Color.parseColor("#64748B"));
        lbl.setPadding(0, 0, 0, 10);
        card.addView(lbl);

        return card;
    }

    private GradientDrawable getRoundedDrawable(String hexColor, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(hexColor));
        gd.setCornerRadius(radius);
        return gd;
    }

    private void updateStats() {
        SharedPreferences sp = getSharedPreferences("FlowReelsData", Context.MODE_PRIVATE);
        int reels = sp.getInt("reel_count", 0);
        long totalSecs = sp.getLong("total_time_sec", 0);

        countText.setText(String.valueOf(reels));
        long minutes = totalSecs / 60;
        timeText.setText(minutes + "m");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateStats();
        updateHandler.post(updateRunnable);
    }

    @Override
    protected void onPause() {
        super.onPause();
        updateHandler.removeCallbacks(updateRunnable);
    }
}
