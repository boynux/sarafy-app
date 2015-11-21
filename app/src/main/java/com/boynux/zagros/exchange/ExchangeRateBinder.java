package com.boynux.zagros.exchange;

import android.content.Context;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.boynux.zagros.R;
import com.boynux.zagros.exchange.Models.ExchangeRate;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.ocpsoft.prettytime.PrettyTime;

/**
 * Created by mamad on 12/4/2014.
 */
public class ExchangeRateBinder extends RecyclerView.Adapter<ExchangeRateBinder.ViewHolder> {
    private static final String LOG_TAG = ExchangeRateBinder.class.getSimpleName();

    private ExchangeRate[] mItems;
    private Context mContext;

    public static final class ViewHolder extends RecyclerView.ViewHolder {
        ImageView flag;
        TextView title;
        TextView sell;
        TextView changes;
        TextView update;

        public ViewHolder(View view) {
            super(view);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.length;
    }

    public ExchangeRateBinder(Context context, ExchangeRate[] list) {
        mItems = list;
        mContext = context;
    }

    public void update(ExchangeRate[] items) {
        Log.d(LOG_TAG, "Attempting to update ListView..");

        mItems = items;
        notifyDataSetChanged();
    }

    public ExchangeRate[] getItems() {
        return this.mItems;
    }

    @Override
    public ExchangeRateBinder.ViewHolder onCreateViewHolder(ViewGroup parent,
                                                   int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.exchange_rates_row, parent, false);

        ViewHolder holder = new ViewHolder(view);

        holder.title = (TextView) view.findViewById(R.id.commodity_title);
        holder.flag = (ImageView) view.findViewById(R.id.country_flag_image);
        holder.sell = (TextView) view.findViewById(R.id.commodity_sell_price);
        holder.changes = (TextView) view.findViewById(R.id.commodity_change_price);
        holder.update = (TextView) view.findViewById(R.id.commodity_last_update);

        return holder;
    }

    /**
     * Called by RecyclerView to display the data at the specified position. This method
     * should update the contents of the {@link ViewHolder#itemView} to reflect the item at
     * the given position.
     * <p/>
     * Note that unlike {@link ListView}, RecyclerView will not call this
     * method again if the position of the item changes in the data set unless the item itself
     * is invalidated or the new position cannot be determined. For this reason, you should only
     * use the <code>position</code> parameter while acquiring the related data item inside this
     * method and should not keep a copy of it. If you need the position of an item later on
     * (e.g. in a click listener), use {@link ViewHolder#getPosition()} which will have the
     * updated position.
     *
     * @param holder   The ViewHolder which should be updated to represent the contents of the
     *                 item at the given position in the data set.
     * @param position The position of the item within the adapter's data set.
     */
    @Override
    public void onBindViewHolder(ExchangeRateBinder.ViewHolder holder, int position) {
        ExchangeRate item = mItems[position];
        Log.d(LOG_TAG, "Set title to " + item.to());

        holder.title.setText(item.to());

        setFlag(holder.flag, item.getFlagUrl());
        setRate(holder.sell, item.getAsk());
        setChanges(holder.changes, item.getChanges());
        setDate(holder.update, item.getDate());

        holder.itemView.setTag("Negative");
    }

    private void setFlag(ImageView view, String imageUrl) {
        final String countryId = imageUrl.substring(imageUrl.lastIndexOf('/') + 1, imageUrl.lastIndexOf('.')).replaceAll(" ", "_").toLowerCase();
        Log.d(LOG_TAG, "Detected country flag: " + countryId);
        view.setImageResource(mContext.getResources().getIdentifier(countryId, "mipmap", mContext.getPackageName()));
    }

    private void setRate(TextView view, String rate) {
        DecimalFormat formatter = (DecimalFormat) DecimalFormat.getInstance(Locale.US);
        DecimalFormatSymbols symbols = formatter.getDecimalFormatSymbols();

        symbols.setGroupingSeparator(',');
        formatter.setDecimalFormatSymbols(symbols);
        formatter.setMaximumFractionDigits(0);

        view.setText(formatter.format(Double.parseDouble(rate)));
    }

    private void setDate(TextView view, String dateString) {
        DateFormat dateFormatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        try {
            dateFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date date = dateFormatter.parse(dateString);

            PrettyTime p = new PrettyTime();
            view.setText(p.format(date));
        } catch (ParseException e) {
            Log.e(LOG_TAG, "Could not convert date: " + e.getMessage());
        }
    }

    private void setChanges(TextView view, String changes) {
        Double d = Math.round(Double.parseDouble(changes) * 100 * 100) / 100.0 ;

        if (d <= -0.01) {
            view.setTextColor(
                    ContextCompat.getColor(mContext, R.color.rate_change_negative_indicator));
        } else if (d >= 0.01) {
            view.setTextColor(
                    ContextCompat.getColor(mContext, R.color.rate_change_positive_indicator));
        }

        DecimalFormat format = new DecimalFormat("+#0.00;-#");
        view.setText(String.format("%s%%", format.format(d)));
    }
}


