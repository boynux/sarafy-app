package com.boynux.zagros.exchange;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import com.amazonaws.mobile.AWSMobileClient;
import com.boynux.zagros.R;
import com.boynux.zagros.exchange.Models.ExchangeRate;


public class CopyrightFragment extends Fragment {

    final static String LOG_TAG = CopyrightFragment.class.getSimpleName();
    /**
     * Called to have the fragment instantiate its user interface view.
     * This is optional, and non-graphical fragments can return null (which
     * is the default implementation).  This will be called between
     * {@link #onCreate(Bundle)} and {@link #onActivityCreated(Bundle)}.
     * <p/>
     * <p>If you return a View from here, you will later be called in
     * {@link #onDestroyView} when the view is being released.
     *
     * @param inflater           The LayoutInflater object that can be used to inflate
     *                           any views in the fragment,
     * @param container          If non-null, this is the parent view that the fragment's
     *                           UI should be attached to.  The fragment should not add the view itself,
     *                           but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     *                           from a previous saved state as given here.
     * @return Return the View for the fragment's UI, or null.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_copyright, container, false);

        final WebView webView = (WebView)view.findViewById(R.id.copyright_web_view);
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), "Loading ....", "Please wait while loading data");

        final WebSettings settings = webView.getSettings();
        settings.setDefaultTextEncodingName("utf-8");

        new AsyncTask<Void, Void, String>() {

            @Override
            protected void onPreExecute() {
                // showProgress("Processing data...");
            }

            @Override
            protected String doInBackground(Void ...params) {
                Log.d(LOG_TAG, "Fetching information from AWS ...");
                try {
                    final ZagrosProxy proxy = AWSMobileClient
                            .defaultMobileClient()
                            .getCloudFunctionFactory(getContext())
                            .build(ZagrosProxy.class);

                    return proxy.zagrosCopyrightContent();
                } catch (Exception e) {
                    Log.e(LOG_TAG, "Failed to fetch copyright content : " + e.getMessage(), e);

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
            protected void onPostExecute(String result) {
                // hideProgress();
                if (result != null) {
                    Log.d(LOG_TAG, result);
                    webView.loadData(result, "text/html; charset=utf-8", "base64");
                }

                if(progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }
        }.execute();

        return view;
    }
}
