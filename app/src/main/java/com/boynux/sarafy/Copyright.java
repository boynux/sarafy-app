package com.boynux.sarafy;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;


public class Copyright extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_copyright);

        WebView view = (WebView)findViewById(R.id.copyright_web_view);
        final AlertDialog dialog = new AlertDialog.Builder(this).create();
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Loading ....", "Please wait while loading data");

        view.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageFinished(WebView view, String url) {
                // super.onPageFinished(view, url);

                if(progressDialog.isShowing())
                    progressDialog.dismiss();
            }

            @Override
            public void onReceivedError(WebView view, int errorCode, String description, String failingUrl) {
                Log.e("Disclaimer", "Could not load copyright url");
                view.loadUrl("file:///android_asset/url-load-error.html");

                Toast.makeText(Copyright.this, "Woooops! Could not load requested item.", Toast.LENGTH_SHORT).show();
            }
        });

        view.loadUrl(getString(R.string.copyrightUrl));
    }
}
