package com.castrodev.popularmovies;

import android.database.Cursor;

import com.castrodev.popularmovies.rest.Movie;

/**
 * Created by rodrigocastro on 31/10/16.
 */

public interface OnItemClickListener {

    void onItemClick(Cursor cursor);

}

