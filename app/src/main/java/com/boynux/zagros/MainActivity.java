//
// Copyright 2015 Amazon.com, Inc. or its affiliates (Amazon). All Rights Reserved.
//
// Code generated by AWS Mobile Hub. Amazon gives unlimited permission to 
// copy, distribute and modify it.
//

package com.boynux.zagros;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentManager.OnBackStackChangedListener;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.NavUtils;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;

import com.amazonaws.mobile.AWSMobileClient;
import com.amazonaws.mobile.user.IdentityManager;
import com.boynux.zagros.exchange.CopyrightFragment;
import com.boynux.zagros.exchange.DisclaimerFragment;
import com.boynux.zagros.exchange.MenuConfiguration;
import com.boynux.zagros.exchange.ExchangeRateFragment;
import com.boynux.zagros.exchange.Models.ExchangeRate;
import com.boynux.zagros.navigation.NavigationDrawer;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, OnBackStackChangedListener {
    /** Class name for log messages. */
    private final static String LOG_TAG = MainActivity.class.getSimpleName();

    /** Bundle key for saving/restoring the toolbar title. */
    private final static String BUNDLE_KEY_TOOLBAR_TITLE = "title";

    /** The identity manager used to keep track of the current user account. */
    private IdentityManager identityManager;

    /** The toolbar view control. */
    private Toolbar toolbar;

    /** Our navigation drawer class for handling navigation drawer logic. */
    private NavigationDrawer navigationDrawer;

    /** The helper class used to toggle the left navigation drawer open and closed. */
    private ActionBarDrawerToggle drawerToggle;

    private Button   signOutButton;
    private Button   signInButton;

    private AdView mAdView;

    private static final int PROGRESS = 0x1;

    private ProgressBar mProgress;
    private int mProgressStatus = 0;

    /**
     * Initializes the Toolbar for use with the activity.
     */
    private void setupToolbar(final Bundle savedInstanceState) {
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set up the activity to use this toolbar. As a side effect this sets the Toolbar's title
        // to the activity's title.
        setSupportActionBar(toolbar);

        if (savedInstanceState != null) {
            // Some IDEs such as Android Studio complain about possible NPE without this check.
            assert getSupportActionBar() != null;

            // Restore the Toolbar's title.
            getSupportActionBar().setTitle(
                savedInstanceState.getCharSequence(BUNDLE_KEY_TOOLBAR_TITLE));
        }
    }

    /**
     * Initializes the sign-in and sign-out buttons.
     */
    private void setupSignInButtons() {

        signOutButton = (Button) findViewById(R.id.button_signout);
        signOutButton.setOnClickListener(this);

        signInButton = (Button) findViewById(R.id.button_signin);
        signInButton.setOnClickListener(this);

        final boolean isUserSignedIn = identityManager.isUserSignedIn();
        signOutButton.setVisibility(isUserSignedIn ? View.VISIBLE : View.INVISIBLE);
        signInButton.setVisibility(!isUserSignedIn ? View.VISIBLE : View.INVISIBLE);

    }

    /**
     * Initializes the navigation drawer menu to allow toggling via the toolbar or swipe from the
     * side of the screen.
     */
    private void setupNavigationMenu(final Bundle savedInstanceState) {
        final DrawerLayout drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        final ListView drawerItems = (ListView) findViewById(R.id.nav_drawer_items);

        // Create the navigation drawer.
        navigationDrawer = new NavigationDrawer(this, toolbar, drawerLayout, drawerItems,
            R.id.main_fragment_container);

        // Add navigation drawer menu items.
        // Home isn't a demo, but is fake as a demo.
        for (MenuConfiguration.DemoFeature demoFeature : MenuConfiguration.getDemoFeatureList()) {
            navigationDrawer.addDemoFeatureToMenu(demoFeature);
        }
        setupSignInButtons();
        navigationDrawer.showHome();
    }

    private void updateBanner() {
        Log.d(LOG_TAG, "Requesting Ad banner.");

        AdRequest adRequest = new
                AdRequest.Builder().addTestDevice(AdRequest.DEVICE_ID_EMULATOR).build();
        mAdView.loadAd(adRequest);
    }

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Obtain a reference to the mobile client. It is created in the Splash Activity.
        AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        if (awsMobileClient == null) {
            // In the case that the activity is restarted by the OS after the application
            // is killed we must redirect to the splash activity to handle initialization.
            Intent intent = new Intent(this, SplashActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            return;
        }

        setContentView(R.layout.activity_main);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        // Set up the activity to use this toolbar. As a side effect this sets the Toolbar's title
        // to the activity's title.
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowHomeEnabled(true);
        }

        onBackStackChanged();

        // Obtain a reference to the identity manager.
        identityManager = awsMobileClient.getIdentityManager();
        mAdView = (AdView) findViewById(R.id.adView);
        mProgress = (ProgressBar) findViewById(R.id.progressBar);


        updateBanner();
        showFragment(ExchangeRateFragment.class, getString(R.string.app_name), false);
        // setupToolbar(savedInstanceState);
        // setupNavigationMenu(savedInstanceState);
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     * <p/>
     * <p>This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * {@link #onPrepareOptionsMenu}.
     * <p/>
     * <p>The default implementation populates the menu with standard system
     * menu items.  These are placed in the {@link Menu#CATEGORY_SYSTEM} group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     * <p/>
     * <p>You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     * <p/>
     * <p>When you add items to the menu, you can implement the Activity's
     * {@link #onOptionsItemSelected} method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onPrepareOptionsMenu
     * @see #onOptionsItemSelected
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        // Handle action bar item clicks here excluding the home button.
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.copyright_menu:
                showCopyright();
                return true;
            case R.id.disclaimer_menu:
                showDisclaimer();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private <T extends Fragment> void showFragment(Class<T> fragmentClass, String title) {
        this.showFragment(fragmentClass, title, true);
    }

    private <T extends Fragment> void showFragment(Class<T> fragmentClass, String title, boolean addToStack) {
        final FragmentManager fragmentManager = this.getSupportFragmentManager();
        Fragment fragment = fragmentManager.findFragmentByTag(fragmentClass.getSimpleName());

        if (fragment == null) {
            fragment = Fragment.instantiate(this, fragmentClass.getName());
        }

        FragmentTransaction transaction = fragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment, fragmentClass.getSimpleName())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);

        if (addToStack) {
            transaction.addToBackStack(null);
        }

        transaction.commit();

        // Set the title for the fragment.
        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            // actionBar.setTitle(title);
        }
    }

    private void showCopyright() {
        showFragment(CopyrightFragment.class, getString(R.string.title_activity_copyright));
    }

    private void showDisclaimer() {
        showFragment(DisclaimerFragment.class, getString(R.string.title_activity_disclaimer));
    }

    @Override
    protected void onSaveInstanceState(final Bundle bundle) {
        super.onSaveInstanceState(bundle);
        // Save the title so it will be restored properly to match the view loaded when rotation
        // was changed or in case the activity was destroyed.
        if (toolbar != null) {
            bundle.putCharSequence(BUNDLE_KEY_TOOLBAR_TITLE, toolbar.getTitle());
        }
    }

    @Override
    public void onClick(final View view) {
        if (view == signOutButton) {
            // The user is currently signed in with a provider. Sign out of that provider.
            identityManager.signOut();
            // Show the sign-in button and hide the sign-out button.
            signOutButton.setVisibility(View.INVISIBLE);
            signInButton.setVisibility(View.VISIBLE);

            // Close the navigation drawer.
            navigationDrawer.closeDrawer();
            return;
        }
        if (view == signInButton) {
            // Start the sign-in activity. Do not finish this activity to allow the user to navigate back.
            startActivity(new Intent(this, SignInActivity.class));
            // Close the navigation drawer.
            navigationDrawer.closeDrawer();
            return;
        }

        // ... add any other button handling code here ...

    }

    @Override
    protected void onResume() {
        super.onResume();

        // Obtain a reference to the mobile client. It is created in the Splash Activity.
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        if (awsMobileClient == null) {
            // At this point, the app is resuming after being shut down and the onCreate
            // method has already fired an intent to launch the splash activity. The splash
            // activity will refresh the user's credentials and re-initialize all required
            // components. We bail out here, because without that initialization, the steps
            // that follow here would fail. Note that if you don't have any features enabled,
            // then there may not be any steps here to skip.
            return;
        }

        // pause/resume Mobile Analytics collection
        awsMobileClient.handleOnResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        // Obtain a reference to the mobile client.
        final AWSMobileClient awsMobileClient = AWSMobileClient.defaultMobileClient();

        if (awsMobileClient != null) {
            // pause/resume Mobile Analytics collection
            awsMobileClient.handleOnPause();
        }
    }

    @Override
    public void onBackPressed() {
        final FragmentManager fragmentManager = this.getSupportFragmentManager();
        if (fragmentManager.findFragmentByTag(ExchangeRateFragment.class.getSimpleName()) == null) {
            final Class fragmentClass = ExchangeRateFragment.class;
            // if we aren't on the home fragment, navigate home.
            final Fragment fragment = Fragment.instantiate(this, fragmentClass.getName());

            fragmentManager
                .beginTransaction()
                .replace(R.id.main_fragment_container, fragment, fragmentClass.getSimpleName())
                .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                .commit();

            // Set the title for the fragment.
            final ActionBar actionBar = this.getSupportActionBar();
            if (actionBar != null) {
                actionBar.setTitle(getString(R.string.app_name));
            }
            return;
        }
        super.onBackPressed();
    }

    /**
     * Called whenever the contents of the back stack change.
     */
    @Override
    public void onBackStackChanged() {
        //Enable Up button only  if there are entries in the back stack
        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
        final ActionBar actionBar = this.getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(canback);
        }
    }

    /**
     * This method is called whenever the user chooses to navigate Up within your application's
     * activity hierarchy from the action bar.
     * <p/>
     * <p>If a parent was specified in the manifest for this activity or an activity-alias to it,
     * default Up navigation will be handled automatically. See
     * {@link #getSupportParentActivityIntent()} for how to specify the parent. If any activity
     * along the parent chain requires extra Intent arguments, the Activity subclass
     * should override the method {@link #onPrepareSupportNavigateUpTaskStack(TaskStackBuilder)}
     * to supply those arguments.</p>
     * <p/>
     * <p>See <a href="{@docRoot}guide/topics/fundamentals/tasks-and-back-stack.html">Tasks and
     * Back Stack</a> from the developer guide and
     * <a href="{@docRoot}design/patterns/navigation.html">Navigation</a> from the design guide
     * for more information about navigating within your app.</p>
     * <p/>
     * <p>See the {@link TaskStackBuilder} class and the Activity methods
     * {@link #getSupportParentActivityIntent()}, {@link #supportShouldUpRecreateTask(Intent)}, and
     * {@link #supportNavigateUpTo(Intent)} for help implementing custom Up navigation.</p>
     *
     * @return true if Up navigation completed successfully and this Activity was finished,
     * false otherwise.
     */
    @Override
    public boolean onSupportNavigateUp() {
        //This method is called when the up button is pressed. Just the pop back stack.
        // showFragment(ExchangeRateFragment.class, getString(R.string.app_name), false);
        getSupportFragmentManager().popBackStack();
        return true;
    }
}
