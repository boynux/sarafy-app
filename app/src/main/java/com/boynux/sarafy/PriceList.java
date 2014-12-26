package com.boynux.sarafy;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;
import java.util.Random;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.database.CursorWrapper;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.RectShape;
import android.graphics.drawable.shapes.RoundRectShape;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdSize;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.doubleclick.PublisherAdRequest;
import com.google.android.gms.ads.doubleclick.PublisherAdView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PriceList extends ActionBarActivity implements
ActionBar.TabListener, ActivityListener {

	/**
	 * The {@link android.support.v4.view.PagerAdapter} that will provide
	 * fragments for each of the sections. We use a {@link FragmentPagerAdapter}
	 * derivative, which will keep every loaded fragment in memory. If this
	 * becomes too memory intensive, it may be best to switch to a
	 * {@link android.support.v4.app.FragmentStatePagerAdapter}.
	 */
	SectionsPagerAdapter mSectionsPagerAdapter;

	/**
	 * The {@link ViewPager} that will host the section contents.
	 */
	ViewPager mViewPager;

    ProgressDialog progress;

    MenuItem refreshItem;

    Animation refreshAnimation;
	/**
	 * Helper to insert/select exchange rates from SQLite database.
	 */
	ExchangeRateContract mContract;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Set up the action bar.
		final ActionBar actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);

		// Create the adapter that will return a fragment for each of the three
		// primary sections of the activity.
		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		// Set up the ViewPager with the sections adapter.
		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);

		// When swiping between different sections, select the corresponding
		// tab. We can also use ActionBar.Tab#select() to do this if we have
		// a reference to the Tab.
		mViewPager
		.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
			@Override
			public void onPageSelected(int position) {
				actionBar.setSelectedNavigationItem(position);
			}
		});

		// For each of the sections in the app, add a tab to the action bar.
		for (int i = 0; i < mSectionsPagerAdapter.getCount(); i++) {
			// Create a tab with text corresponding to the page title defined by
			// the adapter. Also specify this Activity object, which implements
			// the TabListener interface, as the callback (listener) for when
			// this tab is selected.
			actionBar.addTab(actionBar.newTab()
					.setText(mSectionsPagerAdapter.getPageTitle(i))
					.setTabListener(this));
		}

		mContract = new ExchangeRateContract(this);
		mContract.open();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

        final Menu m = menu;

        refreshItem = menu.findItem(R.id.action_refresh);
        MenuItemCompat.setActionView(refreshItem, R.layout.refresh_action_view);
        // refreshItem.setActionView(R.layout.refresh_action_view);
        // menu.performIdentifierAction(refreshItem.getItemId(), 0);
        refreshAnimation = AnimationUtils.loadAnimation(PriceList.this, R.anim.rotate_cw);
        refreshAnimation.setRepeatCount(Animation.INFINITE);

        MenuItemCompat.getActionView(refreshItem).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                m.performIdentifierAction(refreshItem.getItemId(), 0);
            }
        });

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        refresh();
    }

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch(item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                refresh();
                return true;
            case R.id.action_disclaimer:
                Intent disclaimer = new Intent(this, Disclaimer.class);
                startActivity(disclaimer);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
	}

    public void refresh() {
        try {
            AsyncHttpRequest request = new AsyncHttpRequest(this);
            request.execute("http://forward-liberty-225.appspot.com/exchange-rates/average");
        } catch(Exception e) {
            e.printStackTrace();
            Logger.getAnonymousLogger().warning(String.format("could not retrieve rates online [%s]", e.toString()));
        }
    }

    public void showAlertDialog(String title, String message) {
        Log.d("PriceList", message);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }
	@Override
	public void onTabSelected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
		// When the given tab is selected, switch to the corresponding page in
		// the ViewPager.
		mViewPager.setCurrentItem(tab.getPosition());
	}

	@Override
	public void onTabUnselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

	@Override
	public void onTabReselected(ActionBar.Tab tab,
			FragmentTransaction fragmentTransaction) {
	}

    private void processOnlineCommodityRates(String ratesString)
            throws InterruptedException, ExecutionException, DataFormatException {

        Logger.getAnonymousLogger().info(ratesString);

        // TODO: Consider moving this expression into related Fragment itself.
        //       Might be easier to implement and communicate with Fragment.
        new AsyncTask<String, Void, Boolean> () {

            @Override
            protected void onPreExecute() {
                showProgress("Processing data...");
            }

            @Override
            protected Boolean doInBackground(String... params) {
                try {
                    JSONArray json = new JSONArray(params[0]);
                    ArrayList<ExchangeRate> result = new ArrayList<ExchangeRate>();

                    for (int index = 0; index < json.length(); ++index) {
                        JSONObject item = json.getJSONObject(index);
                        NumberFormat format = new DecimalFormat("###,###");

                        ExchangeRate rate = new ExchangeRate();

                        rate.From = item.getString("fr");
                        rate.To = item.getString("to");
                        rate.LastUpdate = item.getString("date");
                        rate.Changes = item.getDouble("changes");
                        rate.Rates = new String[]{
                                format.format(item.getDouble("bid")),
                                format.format(item.getDouble("ask")),
                        };
                        rate.ToFlagURL = item.getString("flag_url");

                        result.add(rate);
                    }

                    Log.d("WebService", String.format("Updating [%d] commodity rates.", result.size()));

                    for (ExchangeRate rate : result)
                        mContract.updateExchangeRate("ExchangeRates", rate.To, rate.Rates, rate.Changes, rate.LastUpdate, rate.ToFlagURL);

                    return true;
                } catch (JSONException e) {
                    Logger.getAnonymousLogger().warning(String.format("Can not fetch information from provided json object! [%s]", e));
                }

                // TODO: inform user about the incident!
                Log.e("WebService", "Could not fetch information from json object.");


                return false;
            }

            @Override
            protected void onPostExecute(Boolean success) {

                hideProgress();
                if(success)
                    LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(new Intent("DataChange"));
            }
        }.execute(ratesString);
    }

    @Override
    public void onDataReady(Object data) {
        if(data == "" || data == null) {
            showAlertDialog("Error", "Could not access web service.\nPlease check Internet connection.");
            return;
        }

        try {
            Log.d("WebService", "Data is ready, processing data!");
            processOnlineCommodityRates((String) data);
        } catch (ExecutionException e) {
            Log.w("WebService", e.toString());
        } catch (InterruptedException e) {
            Log.w("WebService", e.toString());
        } catch (DataFormatException e) {
            Log.w("WebService", e.toString());
        }
    }

    @Override
    public void showProgress(String message) {
        if(refreshItem != null && MenuItemCompat.getActionView(refreshItem) != null) {
            MenuItemCompat.getActionView(refreshItem).startAnimation(refreshAnimation);
        } else if(progress != null && progress.isShowing()) {
            progress.setMessage(message);
        } else {
            progress = ProgressDialog.show(this, "Wait", message);
        }
    }

    @Override
    public void hideProgress() {
        if(progress != null && progress.isShowing()) {
            progress.dismiss();
        }

       if(refreshItem != null && MenuItemCompat.getActionView(refreshItem) != null) {
            MenuItemCompat.getActionView(refreshItem).clearAnimation();
        }
    }

    public class ExchangeRate
    {
        public String From;
        public String To;
        public String[] Rates;
        public Double Changes = 0.0;
        public String LastUpdate = new SimpleDateFormat(ExchangeRateContract.ExchangeEntry.DATE_FORMAT).format(new Date());
        public String ToFlagURL = "";
    }
	/**
	 * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
	 * one of the sections/tabs/pages.
	 */
	public class SectionsPagerAdapter extends FragmentPagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int position) {
			// getItem is called to instantiate the fragment for the given page.
			// Return a PlaceholderFragment (defined as a static inner class
			// below).
			return PlaceholderFragment.newInstance(PlaceholderFragment.Type
					.values()[position]);
		}

		@Override
		public int getCount() {
			// Show 3 total pages.
			return 1;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section3).toUpperCase(l);
			case 2:
				return getString(R.string.title_section3).toUpperCase(l);
			}
			return null;
		}
	}

	/**
	 * nullColumnHack A placeholder fragment containing a simple view.
	 */
	public static class PlaceholderFragment extends Fragment {
		/**
		 * Fragment type Enumeration.
		 */
		public enum Type {
			ExchangeRate, Disclaimer, GoldPrice
        };

		/**
		 * The fragment argument representing the section number for this
		 * fragment.
		 */
		private static final String ARG_SECTION_TYPE = "section_type";
		private Type sectionType = Type.ExchangeRate;
		private SimpleCursorAdapter mAdapter;
		private ExchangeRateContract mContract;
		private Cursor mCursor;
        private BroadcastReceiver mBroadcastReceiver;
        private View mAdView;
		/**
		 * mDbHelper Returns a new instance of this fragment for the given
		 * section number.
		 */
		public static PlaceholderFragment newInstance(Type sectionType) {
			PlaceholderFragment fragment = new PlaceholderFragment();

			Bundle args = new Bundle();
			args.putInt(ARG_SECTION_TYPE, sectionType.ordinal());
			fragment.setArguments(args);

			return fragment;
		}

		public PlaceholderFragment() {
		}

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);

            mBroadcastReceiver = new FragmentReceiver();

            LocalBroadcastManager.getInstance(getActivity()).registerReceiver(
                    mBroadcastReceiver, new IntentFilter("DataChange")
            );
        }

        @Override
        public void onAttach(Activity activity) {
           super.onAttach(activity);
        }

        @Override
        public void onDetach() {
            super.onDetach();

            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(mBroadcastReceiver);
        }


		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

            final View rootView;
            ListView list = null;
            mContract = new ExchangeRateContract(getActivity());
            mContract.open();

            sectionType = Type.values()[getArguments().getInt(ARG_SECTION_TYPE)];

            String[] dataColumns = {
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_TITLE,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE1,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE2,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_CHANGES,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_LAST_UPDATE,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_FLAG_URL,
            };
            int[] viewIds = {R.id.commodity_title, R.id.commodity_buy_price,
                    R.id.commodity_sell_price, R.id.commodity_change_price,
                    R.id.commodity_last_update, R.id.country_flag_image};

            switch (sectionType) {
                case ExchangeRate:
                    rootView = inflater.inflate(R.layout.fragment_excahage_rates,
                            container, false);
                    mCursor = mContract
                            .getCommoditiesByCategory("ExchangeRates");

                    list = (ListView) rootView
                            .findViewById(R.id.exchangeRateListView);
                    break;
                case GoldPrice:
                    rootView = inflater.inflate(R.layout.fragment_gold_price,
                            container, false);
                    mCursor = mContract
                            .getCommoditiesByCategory("GoldPrices");

                    list = (ListView) rootView
                            .findViewById(R.id.goldPricesListView);
                    break;

                default:
                    rootView = inflater.inflate(R.layout.disclaimer, container,
                            false);
                    break;
            }

            if (list != null) {
                final CursorWrapper cursorWrapper = new AdCursorWrapper(mCursor);

                mAdapter = new SimpleCursorAdapter(getActivity(),
                        R.layout.exchange_rates_row, cursorWrapper, dataColumns,
                        viewIds, 0){

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
                                LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                                mAdView = inflater.inflate(R.layout.ad_banner_card, null);
                                AdView adView = (AdView) mAdView.findViewById(R.id.adView);

                                AdRequest adRequest = new AdRequest.Builder()
                                        .addTestDevice("51B1D8C3AE319654FF77B3D5606B662E")
                                        .addTestDevice(AdRequest.DEVICE_ID_EMULATOR)
                                        .build();

                                adView.loadAd(adRequest);
                            }

                            return mAdView;
                        }

                        return super.newView(context, cursor, parent);
                    }
                };

                mAdapter.setViewBinder(new ExchangeRateBinder());

                list.setAdapter(mAdapter);
            }

            return rootView;
        }

        public class FragmentReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction() != null && intent.getAction().equals("DataChange")) {
                    Log.d("Fragment", "Got it!");
                    if (PlaceholderFragment.this.mAdapter != null) {
                        PlaceholderFragment.this.mAdapter.changeCursor(
                            new AdCursorWrapper(mContract.getCommoditiesByCategory("ExchangeRates"))
                        );
                    }
                }
            }
        }
	}
}
