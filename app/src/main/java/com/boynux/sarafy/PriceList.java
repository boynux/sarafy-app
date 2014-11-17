package com.boynux.sarafy;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.IOException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import android.database.Cursor;
import android.net.http.AndroidHttpClient;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class PriceList extends ActionBarActivity implements
ActionBar.TabListener {

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
		return true;
	}

	@Override
	protected void onStart() {
        super.onStart();

        try {
            ExchangeRate[] rates = getOnlineCommodityRates("http://forward-liberty-225.appspot.com/exchange-rates/average");

            for(ExchangeRate rate : rates)
                mContract.updateExchangeRate("ExchangeRates", rate.To, rate.Rates);

        } catch (Exception e) {
            Logger.getAnonymousLogger().warning(String.format("could not retreive rates online [%s]", e.toString()));

            mContract.updateExchangeRate("ExchangeRates", "USD", new String[]{
                    "32500", "26500", "1.4"});
            mContract.updateExchangeRate("ExchangeRates", "GBP", new String[]{
                    "43800", "38500", "0.8"});

            mContract.updateExchangeRate("GoldPrices", "Full coin", new String[]{
                    "11300000", "8540000", "0.7"});
            mContract.updateExchangeRate("GoldPrices", "Half coin", new String[]{
                    "5850000", "4480000", "0.5"});
            mContract.updateExchangeRate("GoldPrices", "18c", new String[]{
                    "9870000", "N/A", "0.5"});
        }
    }
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
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

    private ExchangeRate[] getOnlineCommodityRates(String url)
            throws InterruptedException, ExecutionException, DataFormatException {
        String ratesString = new RetrievePricesTask().execute(url).get();

        Logger.getAnonymousLogger().info(ratesString);

        try {
            JSONArray json = new JSONArray(ratesString);
            ArrayList<ExchangeRate> result = new ArrayList<ExchangeRate>();

            for (int index = 0; index < json.length(); ++index) {
                JSONObject set = json.getJSONObject(index);
                for (int itemsIndex = 0; itemsIndex < set.names().length(); ++itemsIndex) {
                    ExchangeRate rate = new ExchangeRate();
                    NumberFormat format = DecimalFormat.getInstance(Locale.US);
                    rate.From = set.names().getString(itemsIndex);
                    rate.To = "USD";
                    rate.Rates = new String[]{
                        format.format(set.getJSONObject(rate.From).getDouble("USD")),
                        format.format(set.getJSONObject(rate.From).getDouble("USD"))
                    };

                    result.add(rate);
                }
            }

            ExchangeRate[] rates = new ExchangeRate[result.size()];
            return result.toArray(rates);

        } catch (JSONException e) {
            Logger.getAnonymousLogger().warning(String.format("Can not fetch information from provided json object! [%s]", e));
        }

        throw new DataFormatException("Could not fetch information from json object.");
    }

    public class ExchangeRate
    {
        public String From;
        public String To;
        public String[] Rates;
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
			return 3;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return getString(R.string.title_section2).toUpperCase(l);
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
			ExchangeRate, GoldPrice, Disclaimer
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
		public void onStart() {
			super.onStart();
		};

		@Override
		public View onCreateView(LayoutInflater inflater, ViewGroup container,
				Bundle savedInstanceState) {

			View rootView;
			ListView list = null;
			mContract = new ExchangeRateContract(
					getActivity());
			mContract.open();

			sectionType = Type.values()[getArguments().getInt(ARG_SECTION_TYPE)];

			String[] dataColumns = {
					ExchangeRateContract.ExchangeEntry.COLUMN_NAME_TITLE,
					ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE1,
					ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE2,
					ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE3 };
			int[] viewIds = { R.id.commodity_title, R.id.commodity_buy_price,
					R.id.commodity_sell_price, R.id.commodity_change_price };

			switch (sectionType) {
			case ExchangeRate:
				rootView = inflater.inflate(R.layout.fragment_excahage_rates,
						container, false);
				mCursor = mContract
						.getComoditiesByCategory("ExchangeRates");

				list = (ListView) rootView
						.findViewById(R.id.exchangeRateListView);
				break;
			case GoldPrice:
				rootView = inflater.inflate(R.layout.fragment_gold_price,
						container, false);
				mCursor = mContract
						.getComoditiesByCategory("GoldPrices");

				list = (ListView) rootView
						.findViewById(R.id.goldPricesListView);
				break;

			default:
				rootView = inflater.inflate(R.layout.disclaimer, container,
						false);
				break;
			}

			if (list != null) {
				mAdapter = new SimpleCursorAdapter(getActivity(),
						R.layout.exchange_rates_row, mCursor, dataColumns,
						viewIds, 0) {
					@Override
					public void setViewText(TextView v, String text) {

						switch (v.getId()) {
						case R.id.commodity_buy_price:
						case R.id.commodity_sell_price:
							try {
								super.setViewText(v, String.format("%,d",
										Integer.parseInt(text)));
							} catch (NumberFormatException e) {
								super.setViewText(v, text);
							}
							break;
						case R.id.commodity_change_price:
							super.setViewText(v, String.format("%s%%", text));
							break;
						default:
							super.setViewText(v, text);
							break;
						}
					}
				};

				list.setAdapter(mAdapter);
				mAdapter = new SimpleCursorAdapter(getActivity(),
						R.layout.exchange_rates_row, mCursor, dataColumns,
						viewIds, 0) {
					@Override
					public void setViewText(TextView v, String text) {

						switch (v.getId()) {
						case R.id.commodity_buy_price:
						case R.id.commodity_sell_price:
							try {
								super.setViewText(
										v,
										String.format("%,d",
												Integer.parseInt(text)));
							} catch (NumberFormatException e) {
								super.setViewText(v, text);
							}
							break;
						case R.id.commodity_change_price:
							super.setViewText(v, String.format("%s%%", text));
							break;
						default:
							super.setViewText(v, text);
							break;
						}
					}
				};
			}

			return rootView;
		}

	}
}
