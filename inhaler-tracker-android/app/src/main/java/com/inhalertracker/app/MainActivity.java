package com.inhalertracker.app;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Notifications.ensureChannel(this);

        if (Build.VERSION.SDK_INT >= 33
                && checkSelfPermission(Manifest.permission.POST_NOTIFICATIONS)
                   != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
        }

        WebView webView = new WebView(this);
        WebSettings s = webView.getSettings();
        s.setJavaScriptEnabled(true);
        s.setDomStorageEnabled(true); // localStorage persistence for the app data
        webView.setWebChromeClient(new WebChromeClient());
        webView.setWebViewClient(new WebViewClient());
        webView.addJavascriptInterface(new Bridge(), "AndroidBridge");
        setContentView(webView);
        webView.loadUrl("file:///android_asset/index.html");
    }

    /** Methods callable from the page as window.AndroidBridge.* */
    private class Bridge {

        @JavascriptInterface
        public boolean isAndroid() {
            return true;
        }

        /**
         * json: [{ "id": "...", "label": "Ventolin", "puffs": 2,
         *          "times": ["08:00","20:00"] }, ...]
         * Replaces every previously scheduled reminder.
         */
        @JavascriptInterface
        public void setReminders(final String json) {
            ReminderScheduler.applyFromJson(getApplicationContext(), json);
        }

        /** Shares exported CSV / JSON text via the Android share sheet. */
        @JavascriptInterface
        public void shareText(final String subject, final String text) {
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Intent send = new Intent(Intent.ACTION_SEND);
                        send.setType("text/plain");
                        send.putExtra(Intent.EXTRA_SUBJECT, subject);
                        send.putExtra(Intent.EXTRA_TEXT, text);
                        startActivity(Intent.createChooser(send, subject));
                    } catch (Exception ignored) {
                    }
                }
            });
        }
    }
}
