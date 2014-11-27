package com.boynux.sarafy;

import java.lang.reflect.Type;
import java.util.zip.DataFormatException;

/**
 * Created by mamad on 11/27/2014.
 */
public interface ActivityListener {
    public void onDataReady(Object data);
    public void showProgress(String message);
    public void hideProgress();
}
