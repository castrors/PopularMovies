package com.castrodev.popularmovies.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.castrodev.popularmovies.R;

import java.util.Calendar;

/**
 * Created by rodrigocastro on 27/10/16.
 */
public class Utility {
    public static String getPreferredSorting(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        return prefs.getString(context.getString(R.string.pref_sorting_key),
                context.getString(R.string.pref_sorting_most_popular));
    }

    public static String getYearFromMillis(long millis){
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);

        return String.valueOf(calendar.get(Calendar.YEAR));
    }
}
