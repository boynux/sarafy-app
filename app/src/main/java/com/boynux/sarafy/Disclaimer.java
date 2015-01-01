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
import android.widget.ProgressBar;
import android.widget.Toast;


public class Disclaimer extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        WebView view = (WebView)findViewById(R.id.disclaimer_web_view);
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
                Log.e("Disclaimer", "Could not load disclaimer url");
                view.loadUrl("file:///android_asset/url-load-error.html");

                Toast.makeText(Disclaimer.this, "Woooops! Could not load requested item.", Toast.LENGTH_SHORT).show();
            }
        });

        view.loadUrl(getString(R.string.disclaimerUrl));
    }
}
