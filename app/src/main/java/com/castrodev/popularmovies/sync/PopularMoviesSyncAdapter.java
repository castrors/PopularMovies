package com.castrodev.popularmovies.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;

import com.castrodev.popularmovies.BuildConfig;
import com.castrodev.popularmovies.R;
import com.castrodev.popularmovies.util.Utility;
import com.castrodev.popularmovies.data.MovieContract;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class PopularMoviesSyncAdapter extends AbstractThreadedSyncAdapter {
    public final String LOG_TAG = PopularMoviesSyncAdapter.class.getSimpleName();
    private static final String DATE_PATTERN = "yyyy-MM-dd";

    // Interval at which to sync with the weather, in seconds.
    // 60 seconds (1 minute) * 180 = 3 hours
    public static final int SYNC_INTERVAL = 60 * 180;
    public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;
    private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
//    private static final int WEATHER_NOTIFICATION_ID = 3004;
//
//
//    private static final String[] NOTIFY_WEATHER_PROJECTION = new String[] {
//            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
//            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
//            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
//            WeatherContract.WeatherEntry.COLUMN_SHORT_DESC
//    };

    // these indices must match the projection
    private static final int INDEX_WEATHER_ID = 0;
    private static final int INDEX_MAX_TEMP = 1;
    private static final int INDEX_MIN_TEMP = 2;
    private static final int INDEX_SHORT_DESC = 3;

    private final String MOVIE_BASE_URL = "http://api.themoviedb.org/3";
    private final String API_KEY_PARAM = "api_key";

    public PopularMoviesSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }


    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(LOG_TAG, "Starting sync");

        String sortingOption = Utility.getPreferredSorting(getContext());



        if (tryToGetMovies(sortingOption)) return;

        return;
    }

    private boolean tryToGetTrailers(String remoteId) {
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String trailersJsonStr = null;

        try {

            Uri builtUri = Uri.parse(MOVIE_BASE_URL.concat("/movie/").concat(remoteId).concat("/videos")).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return true;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return true;
            }
            trailersJsonStr = buffer.toString();
            getTrailerDataFromJson(trailersJsonStr, remoteId);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error " + e);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }

        return false;
    }

    private boolean tryToGetMovies(String sortingOption) {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String moviesJsonStr = null;

        try {

            Uri builtUri = Uri.parse(MOVIE_BASE_URL.concat(sortingOption)).buildUpon()
                    .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                    .build();

            URL url = new URL(builtUri.toString());

            Log.v(LOG_TAG, builtUri.toString());

            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return true;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return true;
            }
            moviesJsonStr = buffer.toString();
            getMovieDataFromJson(moviesJsonStr, sortingOption);
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error " + e);
            return true;
        } catch (JSONException e) {
            e.printStackTrace();
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(LOG_TAG, "Error closing stream", e);
                }
            }
        }
        return false;
    }


    private void getMovieDataFromJson(String moviesJsonString, String sortingOption)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String MOVIE_REMOTE_ID = "id";
        final String MOVIE_ORIGINAL_TITLE = "original_title";
        final String MOVIE_IMAGE = "poster_path";
        final String MOVIE_SYNOPSIS = "overview";
        final String MOVIE_RATING = "vote_average";
        final String MOVIE_RELEASE_DATE = "release_date";

        try {

            JSONObject moviesJson = new JSONObject(moviesJsonString);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

            ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

            for (int i = 0; i < moviesArray.length(); i++) {
                String originalTitle;
                String imageUrl;
                String synopsis;
                Double rating;
                Date releaseDate;
                Long remoteId;

                JSONObject currentMovie = moviesArray.getJSONObject(i);

                remoteId = currentMovie.getLong(MOVIE_REMOTE_ID);
                originalTitle = currentMovie.getString(MOVIE_ORIGINAL_TITLE);
                imageUrl = currentMovie.getString(MOVIE_IMAGE);
                synopsis = currentMovie.getString(MOVIE_SYNOPSIS);
                rating = currentMovie.getDouble(MOVIE_RATING);
                releaseDate = getDate(currentMovie.getString(MOVIE_RELEASE_DATE));

                tryToGetTrailers(remoteId.toString());

                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                        MovieContract.MovieEntry.CONTENT_URI);
                builder.withValue(MovieContract.MovieEntry.COLUMN_REMOTE_ID, remoteId);
                builder.withValue(MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE, originalTitle);
                builder.withValue(MovieContract.MovieEntry.COLUMN_IMAGE_URL, imageUrl);
                builder.withValue(MovieContract.MovieEntry.COLUMN_SYNOPSIS, synopsis);
                builder.withValue(MovieContract.MovieEntry.COLUMN_RATING, rating);
                builder.withValue(MovieContract.MovieEntry.COLUMN_RELEASE_DATE, releaseDate.getTime());
                builder.withValue(MovieContract.MovieEntry.COLUMN_SORTING_PREFERENCE, sortingOption);
                batchOperations.add(builder.build());

            }


            int inserted = 0;
            // add to database
            if (batchOperations.size() > 0) {

                try {
                    getContext().getContentResolver().applyBatch(MovieContract.CONTENT_AUTHORITY, batchOperations);
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }

//                notifyWeather();
            }

            Log.d(LOG_TAG, "Sync Complete. " + batchOperations.size() + " Movies Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private void getTrailerDataFromJson(String trailersJsonString, String remoteId)
            throws JSONException {

        final String TMDB_LIST = "results";
        final String TRAILER_ID = "id";
        final String TRAILER_KEY = "key";
        final String TRAILER_NAME = "name";

        try {

            JSONObject trailersJson = new JSONObject(trailersJsonString);
            JSONArray trailersArray = trailersJson.getJSONArray(TMDB_LIST);

            ArrayList<ContentProviderOperation> batchOperations = new ArrayList<>();

            for (int i = 0; i < trailersArray.length(); i++) {
                String id;
                String key;
                String name;

                JSONObject currentTrailer = trailersArray.getJSONObject(i);

                id = currentTrailer.getString(TRAILER_ID);
                key = currentTrailer.getString(TRAILER_KEY);
                name = currentTrailer.getString(TRAILER_NAME);

                ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(
                        MovieContract.TrailerEntry.CONTENT_URI);
                builder.withValue(MovieContract.TrailerEntry.COLUMN_REMOTE_ID, remoteId);
                builder.withValue(MovieContract.TrailerEntry.COLUMN_ID, id);
                builder.withValue(MovieContract.TrailerEntry.COLUMN_KEY, key);
                builder.withValue(MovieContract.TrailerEntry.COLUMN_NAME, name);
                batchOperations.add(builder.build());

            }


            int inserted = 0;
            // add to database
            if (batchOperations.size() > 0) {

                try {
                    getContext().getContentResolver().applyBatch(MovieContract.CONTENT_AUTHORITY, batchOperations);
                } catch (RemoteException | OperationApplicationException e) {
                    Log.e(LOG_TAG, "Error applying batch insert", e);
                }

            }

            Log.d(LOG_TAG, "Sync Complete. " + batchOperations.size() + " Trailers Inserted");

        } catch (JSONException e) {
            Log.e(LOG_TAG, e.getMessage(), e);
            e.printStackTrace();
        }
    }

    private Date getDate(String dateStr) {
        SimpleDateFormat formatter = new SimpleDateFormat(DATE_PATTERN, Locale.getDefault());
        try {
            return formatter.parse(dateStr);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return new Date();
    }


    /**
     * Helper method to schedule the sync adapter periodic execution
     */
    public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            // we can enable inexact timers in our periodic sync
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, flexTime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    /**
     * Helper method to have the sync adapter sync immediately
     *
     * @param context The context used to access the account service
     */
    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    /**
     * Helper method to get the fake account to be used with SyncAdapter, or make a new one
     * if the fake account doesn't exist yet.  If we make a new account, we call the
     * onAccountCreated method so we can initialize things.
     *
     * @param context The context used to access the account service
     * @return a fake account.
     */
    public static Account getSyncAccount(Context context) {
        // Get an instance of the Android account manager
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

        // Create the account type and default account
        Account newAccount = new Account(
                context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

        // If the password doesn't exist, the account doesn't exist
        if (null == accountManager.getPassword(newAccount)) {

        /*
         * Add the account and account type, no password or user data
         * If successful, return the Account object, otherwise report an error.
         */
            if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
                return null;
            }
            /*
             * If you don't set android:syncable="true" in
             * in your <provider> element in the manifest,
             * then call ContentResolver.setIsSyncable(account, AUTHORITY, 1)
             * here.
             */

            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        /*
         * Since we've created an account
         */
        PopularMoviesSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);

        /*
         * Without calling setSyncAutomatically, our periodic sync will not be enabled.
         */
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);

        /*
         * Finally, let's do a sync to get things started
         */
        syncImmediately(context);
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}