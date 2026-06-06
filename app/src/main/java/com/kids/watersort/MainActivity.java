package com.kids.watersort;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.view.WindowManager;
import android.os.Build;

public class MainActivity extends Activity {

    private WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // 全屏沉浸式
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setFullscreenImmersive();

        setContentView(R.layout.activity_main);

        webView = findViewById(R.id.webview);
        setupWebView();
        webView.loadUrl("file:///android_asset/game.html");
    }

    private void setupWebView() {
        WebSettings settings = webView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setDomStorageEnabled(true);      // 允许 localStorage 存进度
        settings.setCacheMode(WebSettings.LOAD_DEFAULT);
        settings.setAllowFileAccess(true);
        settings.setMediaPlaybackRequiresUserGesture(false);
        // 让页面填满 WebView，禁止缩放
        settings.setUseWideViewPort(true);
        settings.setLoadWithOverviewMode(true);
        settings.setSupportZoom(false);
        settings.setBuiltInZoomControls(false);
        settings.setDisplayZoomControls(false);

        webView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                return false; // 所有链接都在 WebView 内打开
            }
        });
        webView.setWebChromeClient(new WebChromeClient());

        // 电视端：让 WebView 响应焦点，否则 D-pad 无法控制
        webView.setFocusable(true);
        webView.setFocusableInTouchMode(true);
        webView.requestFocus();
    }

    // ── 沉浸式全屏 ──────────────────────────────
    private void setFullscreenImmersive() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
        } else {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
            );
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) setFullscreenImmersive();
    }

    // ── 电视遥控器方向键 → 注入 JS 点击事件 ──────
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        switch (keyCode) {
            // 确认键 / 遥控器中间键 → 当作触摸点击
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_ENTER:
            case KeyEvent.KEYCODE_BUTTON_A:
                webView.evaluateJavascript(
                        "javascript:(function(){"
                        + "var el = document.activeElement;"
                        + "if(el && el !== document.body) el.click();"
                        + "})()", null);
                return true;

            // 遥控器返回键
            case KeyEvent.KEYCODE_BACK:
                webView.evaluateJavascript(
                        "javascript:(function(){"
                        + "var back = document.querySelector('.back-btn');"
                        + "if(back) back.click();"
                        + "})()", null);
                return true;

            // 遥控器方向键：移动焦点（让浏览器内建 Tab 顺序来处理）
            case KeyEvent.KEYCODE_DPAD_LEFT:
                webView.evaluateJavascript("javascript:moveFocus(-1)", null);
                return true;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                webView.evaluateJavascript("javascript:moveFocus(1)", null);
                return true;
            case KeyEvent.KEYCODE_DPAD_UP:
                webView.evaluateJavascript("javascript:moveFocus(-5)", null);
                return true;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                webView.evaluateJavascript("javascript:moveFocus(5)", null);
                return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // ── 生命周期 ──────────────────────────────────
    @Override
    protected void onPause() {
        super.onPause();
        webView.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        webView.onResume();
        setFullscreenImmersive();
    }

    @Override
    protected void onDestroy() {
        webView.destroy();
        super.onDestroy();
    }
}
