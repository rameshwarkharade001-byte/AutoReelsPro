package com.autoreels.pro;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

    private TextView totalCountView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        root.setPadding(50, 80, 50, 50);
        root.setBackgroundColor(Color.parseColor("#0F172A"));

        // Header Title
        TextView title = new TextView(this);
        title.setText("FlowReels Studio");
        title.setTextSize(26);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setTextColor(Color.WHITE);
        root.addView(title);

        TextView sub = new TextView(this);
        sub.setText("Smart Hands-Free Companion");
        sub.setTextSize(13);
        sub.setTextColor(Color.parseColor("#94A3B8"));
        sub.setPadding(0, 8, 0, 40);
        root.addView(sub);

        // Stats Card
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(40, 40, 40, 40);
        card.setBackground(createCardBackground("#1E293B", 24));

        TextView statLabel = new TextView(this);
        statLabel.setText("TOTAL REELS TRACKED");
        statLabel.setTextSize(11);
        statLabel.setTextColor(Color.parseColor("#38BDF8"));
        statLabel.setTypeface(Typeface.DEFAULT_BOLD);
        card.addView(statLabel);

        totalCountView = new TextView(this);
        totalCountView.setText("0 Reels");
        totalCountView.setTextSize(32);
        totalCountView.setTypeface(Typeface.DEFAULT_BOLD);
        totalCountView.setTextColor(Color.WHITE);
        totalCountView.setPadding(0, 10, 0, 0);
        card.addView(totalCountView);
        root.addView(card);

        // Step 1: Overlay Permission Button
        Button btnOverlay = createStyledButton("1. ALLOW FLOATING CONTROLLER", "#334155", Color.WHITE);
        btnOverlay.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                } else {
                    Toast.makeText(this, "Floating permission already granted", Toast.LENGTH_SHORT).show();
                }
            }
        });
        root.addView(btnOverlay);

        // Step 2: Accessibility Permission Button
        Button btnAccess = createStyledButton("2. START AUTO ENGINE", "#38BDF8", Color.parseColor("#0F172A"));
        btnAccess.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        root.addView(btnAccess);

        setContentView(root);
    }

    private Button createStyledButton(String text, String bgColor, int textColor) {
        Button b = new Button(this);
        b.setText(text);
        b.setTextSize(13);
        b.setTypeface(Typeface.DEFAULT_BOLD);
        b.setTextColor(textColor);
        b.setBackground(createCardBackground(bgColor, 18));
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 30, 0, 0);
        b.setLayoutParams(lp);
        b.setPadding(0, 35, 0, 35);
        return b;
    }

    private GradientDrawable createCardBackground(String hexColor, int radius) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(Color.parseColor(hexColor));
        gd.setCornerRadius(radius);
        return gd;
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences sp = getSharedPreferences("FlowData", Context.MODE_PRIVATE);
        int total = sp.getInt("reels_count", 0);
        if (totalCountView != null) {
            totalCountView.setText(total + " Reels");
        }
    }
}
