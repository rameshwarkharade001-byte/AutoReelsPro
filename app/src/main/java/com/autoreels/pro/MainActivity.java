package com.autoreels.pro;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Main Background: Dark Slate #0B0F19
        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(60, 100, 60, 60);
        root.setGravity(Gravity.CENTER_HORIZONTAL);
        root.setBackgroundColor(Color.parseColor("#0B0F19"));

        // Title
        TextView title = new TextView(this);
        title.setText("AutoScroll Studio");
        title.setTextSize(28);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.parseColor("#FFFFFF"));
        title.setGravity(Gravity.CENTER);
        root.addView(title);

        // Subtitle
        TextView subtitle = new TextView(this);
        subtitle.setText("Hands-Free Social Experience");
        subtitle.setTextSize(14);
        subtitle.setTextColor(Color.parseColor("#64748B"));
        subtitle.setGravity(Gravity.CENTER);
        subtitle.setPadding(0, 8, 0, 80);
        root.addView(subtitle);

        // Card Container
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(50, 50, 50, 50);
        
        GradientDrawable cardBg = new GradientDrawable();
        cardBg.setColor(Color.parseColor("#161F30"));
        cardBg.setCornerRadius(28);
        card.setBackground(cardBg);

        // Feature Indicator
        TextView statusLabel = new TextView(this);
        statusLabel.setText("SUPPORTED APPS");
        statusLabel.setTextSize(11);
        statusLabel.setTextColor(Color.parseColor("#38BDF8"));
        statusLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(statusLabel);

        TextView appsLabel = new TextView(this);
        appsLabel.setText("Instagram  •  YouTube Shorts  •  Facebook");
        appsLabel.setTextSize(14);
        appsLabel.setTextColor(Color.parseColor("#E2E8F0"));
        appsLabel.setPadding(0, 10, 0, 40);
        card.addView(appsLabel);

        TextView info = new TextView(this);
        info.setText("Enable the engine service once. Videos will automatically glide to the next one smoothly.");
        info.setTextSize(13);
        info.setTextColor(Color.parseColor("#94A3B8"));
        info.setLineSpacing(1.2f, 1.2f);
        info.setPadding(0, 0, 0, 40);
        card.addView(info);

        // Single Master Button
        Button activateBtn = new Button(this);
        activateBtn.setText("ACTIVATE SERVICE");
        activateBtn.setTextSize(15);
        activateBtn.setTextColor(Color.parseColor("#0F172A"));
        activateBtn.setTypeface(Typeface.DEFAULT_BOLD);

        GradientDrawable btnBg = new GradientDrawable();
        btnBg.setColor(Color.parseColor("#38BDF8"));
        btnBg.setCornerRadius(20);
        activateBtn.setBackground(btnBg);
        activateBtn.setPadding(30, 35, 30, 35);

        activateBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });

        card.addView(activateBtn);
        root.addView(card);

        setContentView(root);
    }
}
