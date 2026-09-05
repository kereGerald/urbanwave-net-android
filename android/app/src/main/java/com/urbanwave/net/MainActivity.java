package com.urbanwave.net;

import android.net.Uri;
import android.os.Bundle;
import android.webkit.WebView;
import android.content.Intent;
import com.getcapacitor.BridgeActivity;
import com.getcapacitor.BridgeWebViewClient;

public class MainActivity extends BridgeActivity {

  // Only these hosts are considered "inside the app" — anything else (a
  // future support link, WhatsApp link, etc.) opens in the user's real
  // browser instead of getting trapped in this WebView.
  private static final String APP_HOST = "urbanwave-billingsystem.onrender.com";

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    WebView webView = this.bridge.getWebView();
    webView.setWebViewClient(new BridgeWebViewClient(this.bridge) {
      @Override
      public boolean shouldOverrideUrlLoading(WebView view, String url) {
        Uri uri = Uri.parse(url);
        String host = uri.getHost();
        if (host != null && host.equals(APP_HOST)) {
          // Same app domain — let Capacitor's normal handling load it in-app.
          return super.shouldOverrideUrlLoading(view, url);
        }
        // Anything else — hand off to the system browser/app chooser.
        startActivity(new Intent(Intent.ACTION_VIEW, uri));
        return true;
      }
    });
  }

  @Override
  public void onBackPressed() {
    WebView webView = this.bridge.getWebView();
    if (webView != null && webView.canGoBack()) {
      webView.goBack();
    } else {
      // At the root screen, send the app to the background rather than
      // destroying the activity — matches how most polished Android apps
      // behave on back-at-root, rather than abruptly killing the session.
      moveTaskToBack(true);
    }
  }
}
