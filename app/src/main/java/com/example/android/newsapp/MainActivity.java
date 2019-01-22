package com.example.android.newsapp;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<List<News>>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final int NEWS_LOADER_ID = 1;
    private TextView mEmptyNewsTextView;

    //news adapter
    private NewsAdapter mAdapter;

    private static final String apiKey = com.example.android.newsapp.BuildConfig.ApiKey;

    private static final String NEWS_REQUEST_URL =
            App.getContext().getResources().getString(R.string.query_guardian_url);

    @Override
    public Loader<List<News>> onCreateLoader(int i, Bundle bundle) {

        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);

        //gets the String value from the preferences. The second param is the default value
        String my_news_location = sharedPrefs.getString(
                getString(R.string.settings_location_key),
                getString(R.string.settings_location_default));

        String my_news_category = sharedPrefs.getString(
                getString(R.string.settings_category_key),
                getString(R.string.settings_category_default));


        // parse breaks apart the URI string that's passed into its parameter - kept comment message from lesson
        Uri baseUri = Uri.parse(NEWS_REQUEST_URL);

        // buildUpon preps the baseUri above so we can add query params
        Uri.Builder uriBuilder = baseUri.buildUpon();

        //adds a path to the baseUri ex: /politics
        uriBuilder.appendPath(getString(R.string.search));

        //queries a specific category or location
        uriBuilder.appendQueryParameter(getString(R.string.section), my_news_category);
        uriBuilder.appendQueryParameter(getString(R.string.q), my_news_location);

        //adds a query parameter to show the tag for article contributor
        uriBuilder.appendQueryParameter(getString(R.string.show_tags), getString(R.string.contributor));

        //adds the api key to authenticate to theguardian.com
        uriBuilder.appendQueryParameter(getString(R.string.api_key_xml), apiKey);

        //creates a loader when one is not detected
        return new NewsLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<News>> loader, List<News> news) {

        View loadingIndicator = findViewById(R.id.loading_indicator);
        loadingIndicator.setVisibility(View.GONE);

        String noNews = App.getContext().getResources().getString(R.string.no_news);
        mEmptyNewsTextView.setText(noNews);

        //clears any previous news loader data
        mAdapter.clear();

        if (news != null && !news.isEmpty()) {
            mAdapter.addAll(news);
        }
    }

    @Override
    public void onLoaderReset(Loader<List<News>> loader) {
        //reset the loader and clear the data
        mAdapter.clear();
    }

    @Override
    //initialize the contests of activity options menu
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate the menu specified in the menu xml
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            Intent settingsIntent = new Intent(this, Settings.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Find a reference to the list view in the layout xml
        ListView newsListView = (ListView) findViewById(R.id.list_view);

        mEmptyNewsTextView = (TextView) findViewById(R.id.empty_view);
        newsListView.setEmptyView(mEmptyNewsTextView);

        // Create a new adapter of news
        mAdapter = new NewsAdapter(this, new ArrayList<News>());

        // Set the adapter and populate the list in the UI
        newsListView.setAdapter(mAdapter);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        prefs.registerOnSharedPreferenceChangeListener(this);

        //Set an onClickListener which sends intent to web browser to open news website url
        newsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the news item that was clicked
                News currentNewsItem = mAdapter.getItem(position);

                //Convert String URL into a URI object so we can pass the intent
                Uri newsUri = Uri.parse(currentNewsItem.getUrl());

                // Create intent to view the news URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, newsUri);

                // Send the intent and launch the activity
                startActivity(websiteIntent);
            }
        });


        //check for connectivity
        ConnectivityManager connectivityManager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);

        //current data network details
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

        //if there is an active network, fetch the data
        if (networkInfo != null && networkInfo.isConnected()) {
            //gets a reference to the loader in order to interact with it
            LoaderManager loaderManager = getLoaderManager();

            //initialize the loader
            loaderManager.initLoader(NEWS_LOADER_ID, null, this);
        } else {
            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.GONE);

            mEmptyNewsTextView.setText(R.string.no_internet);
        }


    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
        if (key.equals(getString(R.string.settings_category_key)) ||
                key.equals(getString(R.string.settings_location_key))) {
            mAdapter.clear();

            mEmptyNewsTextView.setVisibility(View.GONE);

            View loadingIndicator = findViewById(R.id.loading_indicator);
            loadingIndicator.setVisibility(View.VISIBLE);

            getLoaderManager().restartLoader(NEWS_LOADER_ID, null, this);
        }
    }

}
