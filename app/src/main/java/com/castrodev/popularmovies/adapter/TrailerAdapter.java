package com.castrodev.popularmovies.adapter;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.castrodev.popularmovies.R;
import com.castrodev.popularmovies.view.DetailFragment;

/**
 * Created by rodrigocastro on 03/10/16.
 */

public class TrailerAdapter extends CursorAdapter {

    public static class ViewHolder {
        public TextView textView;

        public ViewHolder(View view) {
            textView = (TextView) view.findViewById(R.id.trailer_name);
        }
    }

    public TrailerAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.trailer_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();
        viewHolder.textView.setText(cursor.getString(DetailFragment.COL_TRAILER_NAME));

    }

}
