package com.example.android.newsapp;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.List;

public class NewsAdapter extends ArrayAdapter<News> {

    public NewsAdapter(Context context, List<News> news) {
        super(context, 0, news);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        View listItemView = convertView;
        if (listItemView == null) {

            listItemView = LayoutInflater.from(getContext()).inflate(
                    R.layout.list_item, parent, false);
        }

        News currentNews = getItem(position);

        TextView titleView = (TextView) listItemView.findViewById(R.id.textView_title);
        titleView.setText(currentNews.getTitle());

        TextView sectionView = (TextView) listItemView.findViewById(R.id.textView_section);
        sectionView.setText(App.getContext().getResources().getString(R.string.category) + currentNews.getSection());

        TextView authorView = (TextView) listItemView.findViewById(R.id.textView_author);
        authorView.setText(currentNews.getAuthor());

        TextView dateView = (TextView) listItemView.findViewById(R.id.textView_date);
        dateView.setText(currentNews.getDate());

        return listItemView;
    }
}
