package com.boynux.zagros.exchange.Models;

import android.os.Parcel;
import android.os.Parcelable;

import com.google.gson.Gson;

import java.io.Serializable;

/**
 * Created by mamad on 11/2/15.
 */
public class ExchangeRate implements Serializable {
    private String fr;
    private String to;
    private String bid;
    private String ask;
    private String date;
    private String flag_url;
    private String changes;

    public String from() {
        return this.fr;
    }

    public String to() {
        return to;
    }

    public String getBid() {
        return this.bid;
    }

    public String getAsk() {
        return this.ask;
    }

    public String getDate() {
        return this.date;
    }

    public String getFlagUrl() {
        return this.flag_url;
    }

    public String getChanges() {
        return this.changes;
    }

    public String toJson()
    {
        Gson gson = new Gson();
        return gson.toJson(this);
    }

    public static String toJsonArray(ExchangeRate[] rates) {
        Gson gson = new Gson();
        return gson.toJson(rates);
    }

    public static ExchangeRate[] fromJsonArray(String json) {
        Gson gson = new Gson();

        return gson.fromJson(json, ExchangeRate[].class);
    }

    public static ExchangeRate fromJson(String json) {
        Gson gson = new Gson();

        return gson.fromJson(json, ExchangeRate.class);
    }
}
