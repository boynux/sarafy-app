package com.boynux.zagros.exchange;

/**
 * Created by mamad on 10/30/15.
 */

public class ZagrosProxyInfo {
    private String data;
    private String path;
    private String method = "GET";

    public ZagrosProxyInfo() {}

    public ZagrosProxyInfo(String path, String method, String data) {
        this.data = data;
        this.path = path;
        this.method = method;
    }

    public ZagrosProxyInfo(String path, String method)   {
        this(path, method, "");
    }

    public ZagrosProxyInfo(String path)   {
        this(path, "GET", "");
    }

    public String getData() {
        return this.data;
    }

    public String getMethod() {
        return method;
    }

    public String getPath() {
        return path;
    }
}

