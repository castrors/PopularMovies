package com.castrodev.popularmovies.view;

import android.database.Cursor;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;

import com.castrodev.popularmovies.R;
import com.castrodev.popularmovies.data.MovieContract;

import java.util.ArrayList;
import java.util.List;

import static com.castrodev.popularmovies.data.MovieContract.MovieEntry.FAVORITED_TRUE;

public class SettingsActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_general);

        bindPreferenceSummaryToValue(findPreference(getString(R.string.pref_sorting_key)));
    }

    private void bindPreferenceSummaryToValue(Preference preference) {
        preference.setOnPreferenceChangeListener(this);

        onPreferenceChange(preference,
                PreferenceManager
                        .getDefaultSharedPreferences(preference.getContext())
                        .getString(preference.getKey(), ""));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object value) {
        String stringValue = value.toString();

        if (preference instanceof ListPreference) {
            ListPreference listPreference = (ListPreference) preference;

            if (mustShowFavoritedOption() && !containsFavoritedOption(listPreference)) {
                addFavoritedToPreferenceList(listPreference);
            }

            int prefIndex = listPreference.findIndexOfValue(stringValue);
            if (prefIndex >= 0) {
                preference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        } else {
            preference.setSummary(stringValue);
        }
        return true;
    }

    private boolean mustShowFavoritedOption() {

        String mSelectionClause = MovieContract.MovieEntry.COLUMN_FAVORITED + " = ?";
        String[] mSelectionArgs = {String.valueOf(FAVORITED_TRUE)};

        Cursor cursor = getContentResolver().query(
                MovieContract.MovieEntry.CONTENT_URI,
                new String[]{"count(*) AS count"},
                mSelectionClause,
                mSelectionArgs,
                null
        );
        cursor.moveToFirst();
        int count = cursor.getInt(0);

        return count > 0;
    }

    private boolean containsFavoritedOption(ListPreference listPreference) {
        boolean contains = false;
        for (CharSequence charSequence : listPreference.getEntries()) {
            if (charSequence.equals(getString(R.string.pref_sorting_label_favorited))) {
                contains = true;
            }
        }
        return contains;
    }

    private void addFavoritedToPreferenceList(ListPreference listPreference) {
        CharSequence[] entries = listPreference.getEntryValues();
        CharSequence[] values = listPreference.getEntries();

        entries = addFavoriteToEntries(entries);
        listPreference.setEntryValues(entries);

        values = addFavoriteToValues(values);
        listPreference.setEntries(values);
    }

    private CharSequence[] addFavoriteToValues(CharSequence[] values) {
        List<CharSequence> newValues = new ArrayList<>();
        for (int i = 0; i < values.length; i++) {
            newValues.add(values[i]);
        }

        newValues.add(getString(R.string.pref_sorting_label_favorited));
        values = newValues.toArray(new
                CharSequence[newValues.size()]);
        return values;
    }

    @NonNull
    private CharSequence[] addFavoriteToEntries(CharSequence[] entries) {
        List<CharSequence> newEntries = new ArrayList<>();
        for (int i = 0; i < entries.length; i++) {
            newEntries.add(entries[i]);
        }
        newEntries.add(getString(R.string.pref_sorting_favorited));
        entries = newEntries.toArray(new
                CharSequence[newEntries.size()]);
        return entries;
    }
}
