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

import android.content.ContentResolver;
import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;
import android.text.format.Time;

/**
 * Defines table and column names for the weather database.
 */
public class MovieContract {

    public static final String CONTENT_AUTHORITY = "com.castrodev.popularmovies";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_MOVIE = "movie";

    public static long normalizeDate(long startDate) {
        // normalize the start date to the beginning of the (UTC) day
        Time time = new Time();
        time.set(startDate);
        int julianDay = Time.getJulianDay(startDate, time.gmtoff);
        return time.setJulianDay(julianDay);
    }

    public static final class MovieEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_MOVIE).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_MOVIE;

        public static final String TABLE_NAME = "movie";

        public static final String COLUMN_REMOTE_ID = "remote_id";
        public static final String COLUMN_IMAGE_URL = "image_url";
        public static final String COLUMN_ORIGINAL_TITLE = "original_title";
        public static final String COLUMN_SYNOPSIS = "synopsis";
        public static final String COLUMN_RATING = "rating";
        public static final String COLUMN_RELEASE_DATE = "release_date";
        public static final String COLUMN_SORTING_PREFERENCE = "sorting_preference";


        public static Uri buildMovieUri(long id) {
            return ContentUris.withAppendedId(CONTENT_URI, id);
        }

        public static Uri buildMovieWithSortingPreference(
                String sortingPreference) {
            return CONTENT_URI.buildUpon().appendPath(sortingPreference).build();
        }

        public static Uri buildMovieSortingWithRemoteId(String sortingPreference, long remoteId) {
            return CONTENT_URI.buildUpon().appendPath(sortingPreference)
                    .appendPath(Long.toString(remoteId)).build();
        }

        public static String getMovieSortingFromUri(Uri uri) {
            return uri.getPathSegments().get(1);
        }

        public static long getMovieRemoteIdFromUri(Uri uri) {
            return Long.valueOf(uri.getPathSegments().get(2));
        }


    }

}
