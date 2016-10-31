package com.castrodev.popularmovies;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Created by rodrigocastro on 27/10/16.
 */
public class Utility {
    public static String getPreferredSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_most_popular));
    }
}
