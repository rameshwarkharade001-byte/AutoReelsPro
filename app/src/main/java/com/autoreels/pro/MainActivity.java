package com.autoreels.pro;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.graphics.Color;
import android.view.Gravity;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 100, 60, 60);
        layout.setGravity(Gravity.CENTER_HORIZONTAL);
        layout.setBackgroundColor(Color.parseColor("#F9FAFB"));

        TextView title = new TextView(this);
        title.setText("Auto Reels Pro");
        title.setTextSize(26);
        title.setTextColor(Color.parseColor("#111827"));
        title.setPadding(0, 0, 0, 30);
        layout.addView(title);

        TextView desc = new TextView(this);
        desc.setText("Instagram, YouTube आणि Facebook रील्स आपोआप स्क्रोल करण्यासाठी खालील दोन्ही परमिशन चालू करा:");
        desc.setTextSize(14);
        desc.setTextColor(Color.parseColor("#4B5563"));
        desc.setPadding(0, 0, 0, 50);
        layout.addView(desc);

        // Overlay Permission Button
        Button overlayBtn = new Button(this);
        overlayBtn.setText("1. फ्लोटिंग ओव्हरले परमिशन द्या");
        overlayBtn.setBackgroundColor(Color.parseColor("#2563EB"));
        overlayBtn.setTextColor(Color.WHITE);
        overlayBtn.setOnClickListener(v -> {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (!Settings.canDrawOverlays(this)) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + getPackageName()));
                    startActivity(intent);
                }
            }
        });
        layout.addView(overlayBtn);

        // Spacer
        TextView spacer = new TextView(this);
        spacer.setHeight(30);
        layout.addView(spacer);

        // Accessibility Permission Button
        Button accessBtn = new Button(this);
        accessBtn.setText("2. ऑटो-स्क्रोल सर्व्हिस चालू करा");
        accessBtn.setBackgroundColor(Color.parseColor("#059669"));
        accessBtn.setTextColor(Color.WHITE);
        accessBtn.setOnClickListener(v -> {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        });
        layout.addView(accessBtn);

        setContentView(layout);
    }
}
