package com.boynux.sarafy;

import android.content.Context;
import android.database.Cursor;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.support.v4.widget.SimpleCursorAdapter;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

/**
 * Created by mamad on 12/26/2014.
 */
public class AdExtendedCursorAdapter extends SimpleCursorAdapter {
    private View mAdView;

    public AdExtendedCursorAdapter(Context context, int layout, Cursor c, String[] from, int[] to, int flags) {
        super(context, layout, c, from, to, flags);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        if(((Cursor)getItem(position)).getString(0).equals("ADVERT")) {
            return 1;
        }

        return 0;
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        if (cursor.getString(0) == "ADVERT") {

            Log.d("Binder", "Showing advertisement!");

            if (mAdView == null) {
                LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                mAdView = inflater.inflate(R.layout.ad_banner_card, null);
                AdView adView = (AdView) mAdView.findViewById(R.id.adView);

                AdRequest adRequest = new AdRequest.Builder().build();

                adView.loadAd(adRequest);
            }

            return mAdView;
        }

        return super.newView(context, cursor, parent);
    }
}
