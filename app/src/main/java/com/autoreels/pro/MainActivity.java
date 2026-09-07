package com.autoreels.pro;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.view.Gravity;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class MainActivity extends Activity {

    // सध्याचे चालू असलेले व्हर्जन
    private static final int CURRENT_VERSION_CODE = 1;
    // तुझ्या GitHub रिपॉझिटरीचे नाव
    private static final String GITHUB_USER_REPO = "rameshwarthorade001-byte/AutoReelsPro";

    private Switch autoSwitch;
    private Switch globalSwitch;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = getSharedPreferences("ScrollPrefs", Context.MODE_PRIVATE);

        ScrollView scrollView = new ScrollView(this);
        scrollView.setBackgroundColor(Color.parseColor("#0C232E"));

        LinearLayout root = new LinearLayout(this);
        root.setOrientation(LinearLayout.VERTICAL);
        scrollView.addView(root);

        // Header
        LinearLayout topBar = new LinearLayout(this);
        topBar.setOrientation(LinearLayout.VERTICAL);
        topBar.setPadding(40, 50, 40, 40);
        GradientDrawable grad = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[]{Color.parseColor("#00A896"), Color.parseColor("#028090")}
        );
        topBar.setBackground(grad);

        TextView title = new TextView(this);
        title.setText("Automatic Scroll");
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        title.setTypeface(Typeface.DEFAULT_BOLD);
        title.setGravity(Gravity.CENTER);
        topBar.addView(title);
        root.addView(topBar);

        LinearLayout body = new LinearLayout(this);
        body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(35, 30, 35, 30);
        root.addView(body);

        // 1. Automatic Scroll Card
        LinearLayout card1 = buildCard();
        LinearLayout row1 = buildRow("Automatic Scroll", "Turn ON floating navigation service");
        autoSwitch = new Switch(this);
        autoSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            if (isChecked) checkPermissions();
        });
        row1.addView(autoSwitch);
        card1.addView(row1);
        body.addView(card1);

        // 2. Global Scroll Card
        LinearLayout card2 = buildCard();
        LinearLayout row2 = buildRow("Global Scroll", "Enable scrolling on every screen");
        globalSwitch = new Switch(this);
        globalSwitch.setChecked(prefs.getBoolean("global_scroll", false));
        globalSwitch.setOnCheckedChangeListener((v, isChecked) -> {
            prefs.edit().putBoolean("global_scroll", isChecked).apply();
            Toast.makeText(this, isChecked ? "Global Scroll Active" : "Selected Apps Only Active", Toast.LENGTH_SHORT).show();
        });
        row2.addView(globalSwitch);
        card2.addView(row2);
        body.addView(card2);

        // Action Buttons
        LinearLayout btnRow = new LinearLayout(this);
        btnRow.setOrientation(LinearLayout.HORIZONTAL);
        btnRow.setWeightSum(3.0f);
        LinearLayout.LayoutParams btnLp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        btnLp.setMargins(0, 20, 0, 20);
        btnRow.setLayoutParams(btnLp);

        btnRow.addView(buildMenuBtn("APPS", this::showAppsDialog));
        btnRow.addView(buildMenuBtn("THEME", this::showThemeDialog));
        btnRow.addView(buildMenuBtn("SETTINGS", this::showSettingsDialog));
        body.addView(btnRow);

        setContentView(scrollView);

        // ॲप चालू होताच बॅकग्राउंडला ऑटो-अपडेट चेक करणे
        checkForAppUpdate();
    }

    private LinearLayout buildCard() {
        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(35, 35, 35, 35);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#143642"));
        bg.setCornerRadius(22);
        card.setBackground(bg);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        lp.setMargins(0, 0, 0, 25);
        card.setLayoutParams(lp);
        return card;
    }

    private LinearLayout buildRow(String heading, String sub) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1.0f);
        textCol.setLayoutParams(lp);

        TextView t = new TextView(this);
        t.setText(heading);
        t.setTextSize(16);
        t.setTextColor(Color.WHITE);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        textCol.addView(t);

        TextView s = new TextView(this);
        s.setText(sub);
        s.setTextSize(12);
        s.setTextColor(Color.parseColor("#80CED7"));
        textCol.addView(s);

        row.addView(textCol);
        return row;
    }

    private LinearLayout buildMenuBtn(String label, Runnable onClick) {
        LinearLayout box = new LinearLayout(this);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(0, 160, 1.0f);
        lp.setMargins(8, 0, 8, 0);
        box.setLayoutParams(lp);
        box.setOrientation(LinearLayout.VERTICAL);
        box.setGravity(Gravity.CENTER);

        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.parseColor("#1A4B5C"));
        bg.setCornerRadius(18);
        box.setBackground(bg);

        TextView t = new TextView(this);
        t.setText(label);
        t.setTextColor(Color.WHITE);
        t.setTextSize(12);
        t.setTypeface(Typeface.DEFAULT_BOLD);
        box.addView(t);

        box.setOnClickListener(v -> onClick.run());
        return box;
    }

    // --- इन-ॲप ऑटो अपडेटर लॉजिक ---
    private void checkForAppUpdate() {
        new Thread(() -> {
            try {
                // GitHub Releases API वरून लेटेस्ट व्हर्जन चेक करणे
                URL url = new URL("https://api.github.com/repos/" + GITHUB_USER_REPO + "/releases/latest");
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("User-Agent", "AutoScrollApp");

                if (conn.getResponseCode() == 200) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line);
                    }
                    reader.close();

                    JSONObject json = new JSONObject(sb.toString());
                    String tagName = json.optString("tag_name", "v1.0");
                    // tag_name मधून व्हर्जन नंबर काढणे (उदा. v2.0 -> 2)
                    int latestVer = Integer.parseInt(tagName.replaceAll("[^0-9]", ""));

                    if (latestVer > CURRENT_VERSION_CODE) {
                        String downloadUrl = json.getJSONArray("assets").getJSONObject(0).getString("browser_download_url");
                        runOnUiThread(() -> showUpdateDialog(downloadUrl, tagName));
                    }
                }
            } catch (Exception ignored) {}
        }).start();
    }

    private void showUpdateDialog(String downloadUrl, String newVersion) {
        new AlertDialog.Builder(this)
                .setTitle("🚀 नवीन अपडेट उपलब्ध आहे (" + newVersion + ")")
                .setMessage("ॲपमध्ये नवीन फीचर्स आणि सुधारणा आल्या आहेत. कृपया अपडेट करा.")
                .setCancelable(false)
                .setPositiveButton("Update Now", (d, w) -> startDownloadAndInstall(downloadUrl))
                .setNegativeButton("Later", null)
                .show();
    }

    private void startDownloadAndInstall(String downloadUrl) {
        Toast.makeText(this, "अपडेट डाऊनलोड होत आहे...", Toast.LENGTH_SHORT).show();

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        request.setTitle("Auto Scroll Update");
        request.setDescription("Downloading latest version...");
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "update.apk");

        DownloadManager dm = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
        long downloadId = dm.enqueue(request);

        registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    File file = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "update.apk");
                    installApk(file);
                }
            }
        }, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void installApk(File file) {
        if (!file.exists()) return;

        Uri apkUri = FileProvider.getUriForFile(this, getPackageName() + ".provider", file);
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(apkUri, "application/vnd.android.package-archive");
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    private void showSettingsDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Scrolling Settings");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        int currentDelay = prefs.getInt("jump_delay", 14);
        TextView delayLabel = new TextView(this);
        delayLabel.setText("Jump Pages Delay: " + currentDelay + "s");
        layout.addView(delayLabel);

        SeekBar delayBar = new SeekBar(this);
        delayBar.setMax(30);
        delayBar.setProgress(currentDelay);
        delayBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int val = Math.max(progress, 3);
                delayLabel.setText("Jump Pages Delay: " + val + "s");
                prefs.edit().putInt("jump_delay", val).apply();
            }
            @Override public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override public void onStopTrackingTouch(SeekBar seekBar) {}
        });
        layout.addView(delayBar);

        CheckBox invertCb = new CheckBox(this);
        invertCb.setText("Invert Scrolling Direction");
        invertCb.setChecked(prefs.getBoolean("invert_scroll", false));
        invertCb.setOnCheckedChangeListener((v, isChecked) -> prefs.edit().putBoolean("invert_scroll", isChecked).apply());
        layout.addView(invertCb);

        b.setView(layout);
        b.setPositiveButton("Done", null);
        b.show();
    }

    private void showThemeDialog() {
        String[] colors = {"Teal Lagoon (Default)", "Emerald Green", "Neon Orange", "Dark Slate"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Select Widget Theme");
        b.setItems(colors, (dialog, which) -> {
            String hex = "#E620B2AA";
            if (which == 1) hex = "#E62ECC71";
            if (which == 2) hex = "#E6E67E22";
            if (which == 3) hex = "#E62C3E50";
            prefs.edit().putString("widget_color", hex).apply();
            Toast.makeText(this, "Theme Saved!", Toast.LENGTH_SHORT).show();
        });
        b.show();
    }

    private void showAppsDialog() {
        String[] targets = {"Instagram (Active)", "YouTube Shorts (Active)", "Facebook Reels (Active)"};
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("Selected Apps Engine");
        b.setItems(targets, null);
        b.setPositiveButton("OK", null);
        b.show();
    }

    private void checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this)) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:" + getPackageName()));
            startActivity(intent);
            Toast.makeText(this, "Allow overlay permission first", Toast.LENGTH_LONG).show();
            autoSwitch.setChecked(false);
            return;
        }
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        startActivity(intent);
    }
                                  }
                   
