package com.castrodev.popularmovies;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.widget.CursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

/**
 * Created by rodrigocastro on 03/10/16.
 */

public class MovieAdapter extends CursorAdapter {

    public static final String IMAGE_URL_PREFIX = "http://image.tmdb.org/t/p/w185/";

    public MovieAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }


    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {

        View view = LayoutInflater.from(context).inflate(R.layout.movie_item, parent, false);

        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);

        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {

        ViewHolder viewHolder = (ViewHolder) view.getTag();

        Picasso.with(context)
                .load(IMAGE_URL_PREFIX.concat(cursor.getString(MainActivityFragment.COL_MOVIE_IMAGE_URL)))
                .into(viewHolder.iconView);

    }

    public static class ViewHolder {
        public final ImageView iconView;

        public ViewHolder(View view) {
            iconView = (ImageView) view.findViewById(R.id.movie_image);
        }
    }
}
