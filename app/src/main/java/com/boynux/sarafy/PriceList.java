package com.boynux.sarafy;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Locale;
import java.util.concurrent.ExecutionException;
import java.util.logging.Logger;
import java.util.zip.DataFormatException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.ListView;
import android.widget.TextView;

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
        refreshItem.setActionView(R.layout.refresh_action_view);
        // menu.performIdentifierAction(refreshItem.getItemId(), 0);
        refreshAnimation = AnimationUtils.loadAnimation(PriceList.this, R.anim.rotate_cw);
        refreshAnimation.setRepeatCount(Animation.INFINITE);

        refreshItem.getActionView().setOnClickListener(new View.OnClickListener() {
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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(message);
        builder.setTitle(title);
        builder.setPositiveButton("OK",new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                ;
            }
        });

        builder.create().show();
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
        new AsyncTask<String, Void, ExchangeRate[]> () {

            @Override
            protected void onPreExecute() {
                showProgress("Processing data...");
            }
            @Override
            protected ExchangeRate[] doInBackground(String... params) {
                try {
                    JSONArray json = new JSONArray(params[0]);
                    ArrayList<ExchangeRate> result = new ArrayList<ExchangeRate>();

                    for (int index = 0; index < json.length(); ++index) {
                        JSONObject set = json.getJSONObject(index);
                        for (int itemsIndex = 0; itemsIndex < set.names().length(); ++itemsIndex) {

                            NumberFormat format = new DecimalFormat("###,###");

                            String from = set.names().getString(itemsIndex);
                            JSONObject setExchanges = set.getJSONObject(from);
                            for (int itemIndexInner = 0; itemIndexInner < setExchanges.names().length(); ++itemIndexInner) {
                                ExchangeRate rate = new ExchangeRate();

                                rate.From = set.names().getString(itemsIndex);
                                rate.To = setExchanges.names().getString(itemIndexInner);

                                JSONObject responseRates = setExchanges.getJSONObject(rate.To);
                                rate.Rates = new String[]{
                                        format.format(responseRates.getDouble("BID")),
                                        format.format(responseRates.getDouble("ASK"))
                                };

                                result.add(rate);
                            }
                        }
                    }

                    ExchangeRate[] rates = new ExchangeRate[result.size()];
                    return result.toArray(rates);
                } catch (JSONException e) {
                    Logger.getAnonymousLogger().warning(String.format("Can not fetch information from provided json object! [%s]", e));
                }

                // TODO: inform user about the incident!
                Log.e("WebService", "Could not fetch information from json object.");

                return new ExchangeRate[0];
            }

            @Override
            protected void onPostExecute(ExchangeRate[] rates) {
                Log.d("WebService", String.format("Updating [%d] commodity rates.", rates.length));
                for (ExchangeRate rate : rates)
                    mContract.updateExchangeRate("ExchangeRates", rate.To, rate.Rates);

                hideProgress();
                sendBroadcast(new Intent("DataChange"));
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
        if(refreshItem != null && refreshItem.getActionView() != null) {
            refreshItem.getActionView().startAnimation(refreshAnimation);
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
        } else if(refreshItem != null && refreshItem.getActionView() != null) {
            refreshItem.getActionView().clearAnimation();
        }
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
			return 2;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			Locale l = Locale.getDefault();
			switch (position) {
			case 0:
				return getString(R.string.title_section1).toUpperCase(l);
			case 1:
				return "Disclaimer".toUpperCase(l);
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
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
        }

        @Override
        public void onAttach(Activity activity) {
           super.onAttach(activity);

           getActivity().registerReceiver(new FragmentReceiver(), new IntentFilter("DataChange"));
        }

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
					ExchangeRateContract.ExchangeEntry.COLUMN_NAME_VALUE3,
                    ExchangeRateContract.ExchangeEntry.COLUMN_NAME_LAST_UPDATE, };
			int[] viewIds = { R.id.commodity_title, R.id.commodity_buy_price,
					R.id.commodity_sell_price, R.id.commodity_change_price,
                    R.id.commodity_last_update };

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
                        case R.id.commodity_last_update:
                            super.setViewText(v, text);
                            break;
						default:
							super.setViewText(v, text);
							break;
						}
					}
				};

				list.setAdapter(mAdapter);
			}

			return rootView;
		}

        public class FragmentReceiver extends BroadcastReceiver {
            @Override
            public void onReceive(Context context, Intent intent) {
                Log.d("Fragment", "Got it!");
                if(PlaceholderFragment.this.mAdapter != null) {
                    PlaceholderFragment.this.mAdapter.changeCursor(mContract.getCommoditiesByCategory("ExchangeRates"));
                }
            }
        }
	}
}
