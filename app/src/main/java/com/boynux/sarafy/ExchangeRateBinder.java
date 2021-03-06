package com.boynux.sarafy;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;

import org.apache.http.impl.cookie.DateParseException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.ExecutionException;

/**
 * Created by mamad on 12/4/2014.
 */
public class ExchangeRateBinder implements ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int i) {
        switch(view.getId()) {
            case R.id.commodity_last_update:
                DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                try {
                    dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
                    Date date = dateFormatter.parse(cursor.getString(i));
                    dateFormatter.setTimeZone(TimeZone.getDefault());
                    ((TextView)view).setText(dateFormatter.format(date));

                    return true;
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                return false;
            case R.id.commodity_change_price:
                Drawable background = view.getResources().getDrawable(R.drawable.selector_card_background);
                Double d = cursor.getDouble(i) * 100;

                if (d < -0.01) {
                    background = view.getResources().getDrawable(R.drawable.selector_card_background_negative);
                } else if (d > 0.01) {
                    background = view.getResources().getDrawable(R.drawable.selector_card_background_positive);
                }

                LinearLayout layout = (LinearLayout)view.getParent().getParent();
                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    layout.setBackgroundDrawable(background);
                } else {
                    layout.setBackground(background);
                }

                // setBackground resets padding!
                layout.setPadding(
                    view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                    view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                    view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                    view.getResources().getDimensionPixelSize(R.dimen.card_padding_all)
                );


                Log.d("ViewBinder", String.format("%d", i));

                DecimalFormat format = new DecimalFormat("#0.00");
                ((TextView) view).setText(String.format("%s%%", format.format(d)));
                return true;

            case R.id.country_flag_image:
                try {
                    final ImageView flagImage = ((ImageView)view);
                    URL url = new URL(cursor.getString(i));

                    new AsyncTask<URL, Void, Bitmap>() {
                        @Override
                        public Bitmap doInBackground(URL... params) {
                            URL url = params[0];

                            try {

                                return BitmapFactory.decodeStream(url.openConnection().getInputStream());
                            } catch (IOException e) {
                                Log.e("image", "Could not load image.");
                            }

                            return null;
                        }

                        @Override
                        protected void onPostExecute(Bitmap bitmap) {
                            flagImage.setImageBitmap(bitmap);
                        }
                    }.execute(url);


                } catch (MalformedURLException e) {
                    Log.e("image", "Could not load image [Malformed URL].");
                }

                return true;
            default:
                return false;

        }
    }
}

