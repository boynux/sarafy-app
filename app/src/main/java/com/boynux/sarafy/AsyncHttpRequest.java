package com.boynux.sarafy;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import com.boynux.sarafy.PriceList.ExchangeRate;

public class AsyncHttpRequest extends AsyncTask<String, Void, String> {

    private ActivityListener activity;

    public AsyncHttpRequest(ActivityListener activity)
    {
        this.activity = activity;
    }

    protected String doInBackground(String... urls)
    {
        try {
            HttpClient httpclient = new DefaultHttpClient();
            HttpResponse response = httpclient.execute(new HttpGet(urls[0]));
            StatusLine statusLine = response.getStatusLine();

            if (statusLine.getStatusCode() == HttpStatus.SC_OK) {
                ByteArrayOutputStream out = new ByteArrayOutputStream();
                response.getEntity().writeTo(out);
                out.close();

                return out.toString();
            }
        } catch (Exception e){
            Logger.getAnonymousLogger().warning(e.toString());
        }

        return "";
    }

    @Override
    protected void onPostExecute(String ratesString)
    {
        activity.hideProgress();
        activity.onDataReady(ratesString);
    }

    @Override
    protected void onPreExecute() {
        activity.showProgress("Fetching data ...");
    }
}
