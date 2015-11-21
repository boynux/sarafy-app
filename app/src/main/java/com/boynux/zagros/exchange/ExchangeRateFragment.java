package com.boynux.zagros.exchange;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.boynux.zagros.R;
import com.boynux.zagros.exchange.Models.ExchangeRate;

public class ExchangeRateFragment extends DemoFragmentBase {

    private static final String LOG_TAG = ExchangeRateFragment.class.getSimpleName();
    public static final String EXCHANGE_RATES_KEY = "ExchangeRates";

    private RecyclerView mRecyclerView;
    private ExchangeRateBinder mAdapter;
    private LinearLayoutManager mLayoutManager;
    private SwipeRefreshLayout mSwipeRefresh;
    private ExchangeRate[] mExchangeRates = new ExchangeRate[0];
    private ProgressBar mProgress;

    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container,
                             final Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        setHasOptionsMenu(true);

        // Inflate the layout for this fragment
        final View rootView =
            inflater.inflate(R.layout.fragment_excahage_rates, container, false);

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        String json = preferences.getString(EXCHANGE_RATES_KEY, "[]");

        ExchangeRate[] rates = ExchangeRate.fromJsonArray(json);

        mSwipeRefresh = (SwipeRefreshLayout) rootView.findViewById(R.id.swipeContainer);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                processOnlineCommodityRates();
            }
        });

        mProgress = (ProgressBar) getActivity().findViewById(R.id.progressBar);

        // Configure the refreshing colors

        mSwipeRefresh.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        mRecyclerView = (RecyclerView) rootView
                .findViewById(R.id.exchangeRateRecycleView);
        final int itemsPadding = getResources().getDimensionPixelOffset(R.dimen.card_margin_bottom);
        mRecyclerView.addItemDecoration(
                new VerticalSpaceItemDecoration(itemsPadding)
        );

        // use a linear layout manager
        mLayoutManager = new LinearLayoutManager(getContext());
        mRecyclerView.setLayoutManager(mLayoutManager);

        if (mRecyclerView != null) {
            mAdapter = new ExchangeRateBinder(getActivity(), rates);

            mRecyclerView.setAdapter(mAdapter);
        }

        return rootView;
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.  For this method
     * to be called, you must have first called {@link #setHasOptionsMenu}.  See
     * {@link Activity#onCreateOptionsMenu(Menu) Activity.onCreateOptionsMenu}
     * for more information.
     *
     * @param menu     The options menu in which you place your items.
     * @param inflater
     * @see #setHasOptionsMenu
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.exchanges, menu);

        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     * <p/>
     * <p>Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     * @see #onCreateOptionsMenu
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.refresh_menu:
                processOnlineCommodityRates();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        processOnlineCommodityRates();

        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();

        SharedPreferences preferences =
                PreferenceManager.getDefaultSharedPreferences(getContext());
        preferences.
                edit().
                putString(EXCHANGE_RATES_KEY, ExchangeRate.toJsonArray(mAdapter.getItems())).
                apply();
    }

    private void processOnlineCommodityRates() {
        new AsyncTask<Void, Void, ExchangeRate[]>() {

            @Override
            protected void onPreExecute() {
                mProgress.setVisibility(View.VISIBLE);
                mProgress.setIndeterminate(true);
                // showProgress("Processing data...");
            }

            @Override
            protected ExchangeRate[] doInBackground(Void ...params) {
                Log.d(LOG_TAG, "Fetching information from AWS ...");
                try {
                    final ZagrosProxy proxy = AWSMobileClient
                            .defaultMobileClient()
                            .getCloudFunctionFactory(getContext())
                            .build(ZagrosProxy.class);

                    return proxy.zagrosExchangeRates();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to fetch Exchange rates : " + e.getMessage(), e);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(
                                    getContext(),
                                    "Could not fetch information.\nPlease check internet connection.",
                                    Toast.LENGTH_LONG).show();
                        }
                    });
                }
                return null;
            }

            @Override
            protected void onPostExecute(ExchangeRate[] result) {
                // hideProgress();
                mProgress.setVisibility(View.GONE);

                if (mAdapter != null && result != null) {
                    mAdapter.update(result);
                }

                mSwipeRefresh.setRefreshing(false);
            }
        }.execute();
    }

    public void showError(final String errorMessage) {
        new AlertDialog.Builder(getActivity())
                .setTitle(getActivity().getResources().getString(R.string.cloud_logic_error_title))
                .setMessage(errorMessage)
                .setNegativeButton(getActivity().getResources().getString(R.string.cloud_logic_error_dismiss), null)
                .create().show();
    }
}
