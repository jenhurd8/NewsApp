package com.example.android.newsapp;

import java.util.List;

import android.content.AsyncTaskLoader;
import android.content.Context;

public class NewsLoader extends AsyncTaskLoader<List<News>> {

    //query url
    private String mUrl;

    public NewsLoader(Context context, String url) {
        super(context);
        mUrl = url;
    }

    @Override
    protected void onStartLoading() {
        forceLoad();
    }

    @Override
    public List<News> loadInBackground() {
        if (mUrl == null) {
            return null;
        }

        List<News> news = QueryUtils.fetchNewsData(mUrl);
        return news;
    }
}
