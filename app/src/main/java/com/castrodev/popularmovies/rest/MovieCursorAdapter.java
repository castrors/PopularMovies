package com.castrodev.popularmovies.rest;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.castrodev.popularmovies.MainFragment;
import com.castrodev.popularmovies.OnItemClickListener;
import com.castrodev.popularmovies.R;
import com.squareup.picasso.Picasso;

/**
 * Created by rodrigocastro on 03/10/16.
 */

public class MovieCursorAdapter extends CursorRecyclerViewAdapter<MovieCursorAdapter.ViewHolder> {

    public static final String IMAGE_URL_PREFIX = "http://image.tmdb.org/t/p/w185/";

    Context mContext;
    ViewHolder mVh;
    OnItemClickListener mListener;

    public MovieCursorAdapter(Context context, Cursor cursor, OnItemClickListener listener){
        super(context, cursor);
        mContext = context;
        mListener = listener;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView mIconView;
        public ViewHolder(View view){
            super(view);
            mIconView = (ImageView) view.findViewById(R.id.movie_image);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.movie_item, parent, false);
        ViewHolder vh = new ViewHolder(itemView);
        mVh = vh;
        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final Cursor cursor){

        DatabaseUtils.dumpCursor(cursor);

        Picasso.with(mContext)
                .load(IMAGE_URL_PREFIX.concat(cursor.getString(MainFragment.COL_MOVIE_IMAGE_URL)))
                .into(viewHolder.mIconView);

        viewHolder.mIconView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mListener.onItemClick(cursor);
            }
        });
    }
}
