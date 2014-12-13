package com.boynux.sarafy;

import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.support.v4.widget.SimpleCursorAdapter.ViewBinder;

import java.text.DecimalFormat;

/**
 * Created by mamad on 12/4/2014.
 */
public class ExchangeRateBinder implements ViewBinder {
    @Override
    public boolean setViewValue(View view, Cursor cursor, int i) {
        switch(view.getId()) {
            case R.id.commodity_change_price:
                Drawable background = view.getResources().getDrawable(R.drawable.selector_card_background);
                Double d = cursor.getDouble(i) * 100;

                if (d < -0.01) {
                    background = view.getResources().getDrawable(R.drawable.selector_card_background_negative);
                } else if (d > 0.01) {
                    background = view.getResources().getDrawable(R.drawable.selector_card_background_positive);
                }

                int sdk = android.os.Build.VERSION.SDK_INT;
                if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                    ((LinearLayout) view.getParent().getParent()).setBackgroundDrawable(background);
                } else {
                    ((LinearLayout) view.getParent().getParent()).setBackground(background);
                }

                // setBackground resets padding!
                ((LinearLayout) view.getParent().getParent()).setPadding(
                        view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                        view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                        view.getResources().getDimensionPixelSize(R.dimen.card_padding_all),
                        view.getResources().getDimensionPixelSize(R.dimen.card_padding_all)
                );

                Log.d("ViewBinder", String.format("%d", i));

                DecimalFormat format = new DecimalFormat("#0.00");
                ((TextView)view).setText(String.format("%s%%", format.format(d)));
                return true;
            default:
                return false;

        }
    }
}

