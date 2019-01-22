package com.example.android.newsapp;

import android.text.TextUtils;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

//class that pulls the news data
public final class QueryUtils {

    //log message tag
    public static final String LOG_TAG = QueryUtils.class.getSimpleName();

    //constructor not used
    private QueryUtils() {
    }

    //verifies and returns URL
    private static URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.url_issue), e);
        }
        return url;
    }

    //makes http request to url and returns a string
    private static String makeHttpRequest(URL url) throws IOException {
        String jsonResponse = "";

        // If null return empty string
        if (url == null) {
            return jsonResponse;
        }

        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setReadTimeout(10000);
            urlConnection.setConnectTimeout(15000);
            urlConnection.setRequestMethod(App.getContext().getResources().getString(R.string.get));
            urlConnection.connect();

            // If successful response code 200 then read stream and parse
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                jsonResponse = readFromStream(inputStream);
            } else {
                Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.response_error) + urlConnection.getResponseCode());
            }
        } catch (IOException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.json_error), e);
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                //catching io exception on stream close
                inputStream.close();
            }
        }
        return jsonResponse;
    }

    //take input stream (json response) and convert to a string
    private static String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, 
                    Charset.forName(App.getContext().getResources().getString(R.string.utf)));
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

    //returns the news objects taken from the json response
    private static List<News> extractDataFromJson(String newsJSON) {
        // if json is empty return null
        if (TextUtils.isEmpty(newsJSON)) {
            return null;
        }

        // create news array list to add news
        List<News> news = new ArrayList<>();

        //try to parse the json and catch exceptions to prevent crashing - print errors to logs
        try {

            // Create JSONObject from the json response
            JSONObject jsonResponse = new JSONObject(newsJSON);

            // Extract the JSONArray associated with the key called "response",
            JSONArray newsArray = jsonResponse.getJSONObject(App.getContext().getResources().getString(R.string.response))
                    .getJSONArray(App.getContext().getResources().getString(R.string.results));

            // For each news item, create a news object
            for (int i = 0; i < newsArray.length(); i++) {
                // Get a news item from the returned json newsArray
                JSONObject currentNewsItem = newsArray.getJSONObject(i);

                String sectionName = currentNewsItem.getString(App.getContext().getResources().getString(R.string.section_name));
                String webTitle = currentNewsItem.getString(App.getContext().getResources().getString(R.string.web_title));
                String webPublicationDate = currentNewsItem.getString(App.getContext().getResources().getString(R.string.web_publication_date));
                webPublicationDate = webPublicationDate.substring(5, 10) + ("-") +
                        webPublicationDate.substring(0, 4);
                String webUrl = currentNewsItem.getString(App.getContext().getResources().getString(R.string.web_url));
                String author;

                JSONArray authorArray;
                if (currentNewsItem.getJSONArray(App.getContext().getResources().getString(R.string.tags))
                        != null) {
                    authorArray = currentNewsItem.getJSONArray(App.getContext().getResources().getString(R.string.tags));
                } else {
                    authorArray = null;
                }

                if (authorArray.length() != 0) {
                    JSONObject authorArrayJSONObject = authorArray.getJSONObject(0);

                    String addArticleBy = App.getContext().getResources().getString(R.string.article_by);

                    author = addArticleBy + authorArrayJSONObject.getString(App.getContext().getResources().getString(R.string.web_title));
                } else {
                    author = App.getContext().getResources().getString(R.string.author_not_available);
                }

                News newsObject = new News(webTitle, sectionName, author, webPublicationDate, webUrl);

                // Add the news object to the arrayList
                news.add(newsObject);

            }
        } catch (JSONException e) {
            // errors thrown in the try block will catch here and print a log message
            Log.e(App.getContext().getResources().getString(R.string.query_utils), App.getContext().getResources().getString(R.string.json_problem), e);

        }
        // Return the list of news
        return news;
    }

    //Query the Guardian api and return the news objects
    public static List<News> fetchNewsData(String requestUrl) {
        // Create URL object
        URL url = createUrl(requestUrl);

        // Perform HTTP request to the URL and receive a JSON response back
        String jsonResponse = null;
        try {
            jsonResponse = makeHttpRequest(url);
        } catch (IOException e) {
            Log.e(LOG_TAG, App.getContext().getResources().getString(R.string.problem_http), e);
        }

        // Extract relevant fields from the JSON response and create a list of news
        List<News> news = extractDataFromJson(jsonResponse);

        // Return the list of news
        return news;
    }

}
