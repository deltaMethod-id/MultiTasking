package com.vindevs.multitasking;

import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.content.Intent;
import android.net.Uri;
import android.graphics.Color;
import android.view.View;
import android.view.Window;
import android.view.WindowInsets;
import android.webkit.ValueCallback;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class MainActivity extends Activity {

    private WebView webView;
    private View rootLayout;

    private ValueCallback<Uri[]> filePathCallback;

    private static final int FILE_PICKER_REQUEST = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        rootLayout = findViewById(R.id.rootLayout);
        webView = findViewById(R.id.webView);

        Window window = getWindow();

        /*
         * Jangan fullscreen.
         * Status bar tetap ditampilkan.
         */
        window.getDecorView().setSystemUiVisibility(0);

        /*
         * Android 15 / API 35:
         * ambil inset system bar secara native.
         */
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            rootLayout.setOnApplyWindowInsetsListener(
                new View.OnApplyWindowInsetsListener() {

                    @Override
                    public WindowInsets onApplyWindowInsets(
                            View v,
                            WindowInsets insets) {

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {

                            android.graphics.Insets bars =
                                insets.getInsets(
                                    WindowInsets.Type.systemBars()
                                );

                            v.setPadding(
                                bars.left,
                                bars.top,
                                bars.right,
                                bars.bottom
                            );

                        } else {

                            v.setPadding(
                                insets.getSystemWindowInsetLeft(),
                                insets.getSystemWindowInsetTop(),
                                insets.getSystemWindowInsetRight(),
                                insets.getSystemWindowInsetBottom()
                            );
                        }

                        return insets;
                    }
                }
            );

            rootLayout.requestApplyInsets();
        }

        /*
         * WebView
         */
        WebSettings settings = webView.getSettings();

        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);

        webView.setBackgroundColor(Color.BLACK);

        webView.setWebViewClient(
            new WebViewClient()
        );

        /*
         * File Picker
         */
        webView.setWebChromeClient(
            new WebChromeClient() {

                @Override
                public boolean onShowFileChooser(
                        WebView view,
                        ValueCallback<Uri[]> callback,
                        FileChooserParams params) {

                    if (filePathCallback != null) {
                        filePathCallback.onReceiveValue(null);
                    }

                    filePathCallback = callback;

                    try {

                        Intent intent =
                            params.createIntent();

                        startActivityForResult(
                            intent,
                            FILE_PICKER_REQUEST
                        );

                        return true;

                    } catch (Exception e) {

                        filePathCallback = null;
                        return false;
                    }
                }
            }
        );

        webView.loadUrl(
            "file:///android_asset/index.html"
        );
    }

    @Override
    protected void onActivityResult(
            int requestCode,
            int resultCode,
            Intent data) {

        super.onActivityResult(
            requestCode,
            resultCode,
            data
        );

        if (requestCode != FILE_PICKER_REQUEST) {
            return;
        }

        if (filePathCallback == null) {
            return;
        }

        Uri[] results = null;

        if (resultCode == RESULT_OK && data != null) {

            if (data.getClipData() != null) {

                int count =
                    data.getClipData().getItemCount();

                results = new Uri[count];

                for (int i = 0; i < count; i++) {

                    results[i] =
                        data.getClipData()
                            .getItemAt(i)
                            .getUri();
                }

            } else if (data.getData() != null) {

                results = new Uri[] {
                    data.getData()
                };
            }
        }

        filePathCallback.onReceiveValue(results);

        filePathCallback = null;
    }

    @Override
    public void onBackPressed() {

        if (webView.canGoBack()) {
            webView.goBack();
        } else {
            super.onBackPressed();
        }
    }
}