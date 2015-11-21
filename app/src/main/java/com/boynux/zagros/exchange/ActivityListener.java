package com.boynux.zagros.exchange;

/**
 * Created by mamad on 11/27/2014.
 */
public interface ActivityListener {
    public void onDataReady(Object data);
    public void showProgress(String message);
    public void hideProgress();
}
