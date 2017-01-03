/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.castrodev.popularmovies.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

public final class MovieProvider extends ContentProvider {

    // The URI Matcher used by this content provider.
    private static final UriMatcher sUriMatcher = buildUriMatcher();
    private MovieDbHelper mOpenHelper;

    static final int MOVIE = 100;
    static final int MOVIE_WITH_SORTING_PREFERENCE = 101;
    static final int MOVIE_WITH_SORTING_AND_REMOTE_ID = 102;

    static final int TRAILER = 200;
    static final int TRAILER_WITH_MOVIE_REMOTE_ID = 201;

    private static final SQLiteQueryBuilder sMovieQueryBuilder;
    private static final SQLiteQueryBuilder sTrailerQueryBuilder;

    static {
        sMovieQueryBuilder = new SQLiteQueryBuilder();
        sTrailerQueryBuilder = new SQLiteQueryBuilder();

        //This is an inner join which looks like
        //weather INNER JOIN location ON weather.location_id = location._id
//        sMovieQueryBuilder.setTables(
//                MovieContract.MovieEntry.TABLE_NAME + " INNER JOIN " +
//                        MovieContract.TrailerEntry.TABLE_NAME +
//                        " ON " + MovieContract.MovieEntry.TABLE_NAME +
//                        "." + MovieContract.MovieEntry.COLUMN_REMOTE_ID +
//                        " = " + MovieContract.TrailerEntry.TABLE_NAME +
//                        "." + MovieContract.TrailerEntry.COLUMN_REMOTE_ID);
        sMovieQueryBuilder.setTables(MovieContract.MovieEntry.TABLE_NAME);
        sTrailerQueryBuilder.setTables(MovieContract.TrailerEntry.TABLE_NAME);
    }

    //location.location_setting = ?
    private static final String sSortingPreferenceSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_SORTING_PREFERENCE + " = ? ";

    //location.location_setting = ? AND date = ?
    private static final String sMovieSortingPreferenceAndRemoteIdSelection =
            MovieContract.MovieEntry.TABLE_NAME +
                    "." + MovieContract.MovieEntry.COLUMN_SORTING_PREFERENCE + " = ? AND " +
                    MovieContract.MovieEntry.COLUMN_REMOTE_ID + " = ? ";

    private static final String sTrailerRemoteIdSelection =
            MovieContract.TrailerEntry.TABLE_NAME +
                    "." + MovieContract.TrailerEntry.COLUMN_REMOTE_ID + " = ? ";

    private Cursor getMovieBySortingPreference(Uri uri, String[] projection, String sortOrder) {
        String movieSortingFromUri = MovieContract.MovieEntry.getMovieSortingFromUri(uri);

        String[] selectionArgs;
        String selection;

        selection = sSortingPreferenceSelection;
        selectionArgs = new String[]{movieSortingFromUri};

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );
    }

    private Cursor getMovieBySortingPreferenceAndRemoteId(
            Uri uri, String[] projection, String sortOrder) {
        String sortingPreference = MovieContract.MovieEntry.getMovieSortingFromUri(uri);
        long remoteId = MovieContract.MovieEntry.getMovieRemoteIdFromUri(uri);

        return sMovieQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sMovieSortingPreferenceAndRemoteIdSelection,
                new String[]{sortingPreference, Long.toString(remoteId)},
                null,
                null,
                sortOrder
        );
    }

    private Cursor getTrailerByMovieRemoteId(
            Uri uri, String[] projection, String sortOrder) {
        long remoteId = MovieContract.TrailerEntry.getMovieIdFromUri(uri);

        return sTrailerQueryBuilder.query(mOpenHelper.getReadableDatabase(),
                projection,
                sTrailerRemoteIdSelection,
                new String[]{Long.toString(remoteId)},
                null,
                null,
                sortOrder
        );
    }


    static UriMatcher buildUriMatcher() {
        // I know what you're thinking.  Why create a UriMatcher when you can use regular
        // expressions instead?  Because you're not crazy, that's why.

        // All paths added to the UriMatcher have a corresponding code to return when a match is
        // found.  The code passed into the constructor represents the code to return for the root
        // URI.  It's common to use NO_MATCH as the code for this case.
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = MovieContract.CONTENT_AUTHORITY;

        // For each type of URI you want to add, create a corresponding code.
        matcher.addURI(authority, MovieContract.PATH_MOVIE, MOVIE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*", MOVIE_WITH_SORTING_PREFERENCE);
        matcher.addURI(authority, MovieContract.PATH_MOVIE + "/*/#", MOVIE_WITH_SORTING_AND_REMOTE_ID);
        matcher.addURI(authority, MovieContract.PATH_TRAILER, TRAILER);
        matcher.addURI(authority, MovieContract.PATH_TRAILER + "/*", TRAILER_WITH_MOVIE_REMOTE_ID);

        return matcher;
    }

    /*
        Students: We've coded this for you.  We just create a new WeatherDbHelper for later use
        here.
     */
    @Override
    public boolean onCreate() {
        mOpenHelper = new MovieDbHelper(getContext());
        return true;
    }

    /*
        Students: Here's where you'll code the getType function that uses the UriMatcher.  You can
        test this by uncommenting testGetType in TestProvider.

     */
    @Override
    public String getType(Uri uri) {

        final int match = sUriMatcher.match(uri);

        switch (match) {
            case MOVIE_WITH_SORTING_AND_REMOTE_ID:
                return MovieContract.MovieEntry.CONTENT_ITEM_TYPE;
            case MOVIE_WITH_SORTING_PREFERENCE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            case MOVIE:
                return MovieContract.MovieEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Here's the switch statement that, given a URI, will determine what kind of request it is,
        // and query the database accordingly.
        Cursor retCursor;
        switch (sUriMatcher.match(uri)) {
            // "movie/*/*"
            case MOVIE_WITH_SORTING_AND_REMOTE_ID: {
                retCursor = getMovieBySortingPreferenceAndRemoteId(uri, projection, sortOrder);
                break;
            }
            // "movie/*"
            case MOVIE_WITH_SORTING_PREFERENCE: {
                retCursor = getMovieBySortingPreference(uri, projection, sortOrder);
                break;
            }
            // "trailer/*"
            case TRAILER_WITH_MOVIE_REMOTE_ID: {
                retCursor = getTrailerByMovieRemoteId(uri, projection, sortOrder);
                break;
            }
            // "movie"
            case MOVIE: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.MovieEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }
            // "trailer"
            case TRAILER: {
                retCursor = mOpenHelper.getReadableDatabase().query(
                        MovieContract.TrailerEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            }


            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    /*
        Student: Add the ability to insert Locations to the implementation of this function.
     */
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        Uri returnUri;

        switch (match) {
            case MOVIE: {
                long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.MovieEntry.buildMovieUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            case TRAILER: {
                long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = MovieContract.TrailerEntry.buildTrailerUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            }
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        // this makes delete all rows return the number of rows deleted
        if (null == selection) selection = "1";
        switch (match) {
            case MOVIE:
                rowsDeleted = db.delete(
                        MovieContract.MovieEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRAILER:
                rowsDeleted = db.delete(
                        MovieContract.TrailerEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        // Because a null deletes all rows
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }

//    private void normalizeDate(ContentValues values) {
//        // normalize the date value
//        if (values.containsKey(MovieContract.MovieEntry.COLUMN_DATE)) {
//            long dateValue = values.getAsLong(MovieContract.MovieEntry.COLUMN_DATE);
//            values.put(MovieContract.MovieEntry.COLUMN_DATE, MovieContract.normalizeDate(dateValue));
//        }
//    }

    @Override
    public int update(
            Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int rowsUpdated;

        switch (match) {
            case MOVIE:
                rowsUpdated = db.update(MovieContract.MovieEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            case TRAILER:
                rowsUpdated = db.update(MovieContract.TrailerEntry.TABLE_NAME, values, selection,
                        selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        int returnCount;
        switch (match) {
            case MOVIE:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.MovieEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            case TRAILER:
                db.beginTransaction();
                returnCount = 0;
                try {
                    for (ContentValues value : values) {
                        long _id = db.insert(MovieContract.TrailerEntry.TABLE_NAME, null, value);
                        if (_id != -1) {
                            returnCount++;
                        }
                    }
                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                getContext().getContentResolver().notifyChange(uri, null);
                return returnCount;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    // You do not need to call this method. This is a method specifically to assist the testing
    // framework in running smoothly. You can read more at:
    // http://developer.android.com/reference/android/content/ContentProvider.html#shutdown()
    @Override
    @TargetApi(11)
    public void shutdown() {
        mOpenHelper.close();
        super.shutdown();
    }

}