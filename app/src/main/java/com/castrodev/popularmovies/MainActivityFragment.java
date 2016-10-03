package com.castrodev.popularmovies;

import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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
import java.util.Date;
import java.util.Locale;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_main, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        fetchMoviesTask.execute();
    }

    public class FetchMoviesTask extends AsyncTask<String, Void, Movie[]> {

        private static final String DATE_PATTERN = "yyyy-MM-dd";

        private final String LOG_TAG = FetchMoviesTask.class.getSimpleName();

        private Movie[] getMovieDataFromJson(String moviesJsonString)
                throws JSONException {

            final String TMDB_LIST = "results";
            final String MOVIE_ORIGINAL_TITLE = "original_title";
            final String MOVIE_IMAGE = "poster_path";
            final String MOVIE_SYNOPSIS = "overview";
            final String MOVIE_RATING = "vote_average";
            final String MOVIE_RELEASE_DATE = "release_date";

            JSONObject moviesJson = new JSONObject(moviesJsonString);
            JSONArray moviesArray = moviesJson.getJSONArray(TMDB_LIST);

            Movie[] resultStrs = new Movie[moviesArray.length()];

            for(int i = 0; i < moviesArray.length(); i++) {
                String originalTitle;
                String imageUrl;
                String synopsis;
                Double rating;
                Date releaseDate;

                JSONObject currentMovie = moviesArray.getJSONObject(i);

                originalTitle = currentMovie.getString(MOVIE_ORIGINAL_TITLE);
                imageUrl = currentMovie.getString(MOVIE_IMAGE);
                synopsis = currentMovie.getString(MOVIE_SYNOPSIS);
                rating = currentMovie.getDouble(MOVIE_RATING);
                releaseDate = getDate(currentMovie.getString(MOVIE_RELEASE_DATE));

                resultStrs[i] = new Movie(imageUrl,originalTitle,synopsis,rating,releaseDate);
                Log.v(LOG_TAG, resultStrs[i].toString());
            }
            return resultStrs;

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

        @Override
        protected Movie[] doInBackground(String... params) {

            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String moviesJsonStr = null;


            try {
                // Construct the URL for the OpenWeatherMap query
                // Possible parameters are avaiable at OWM's forecast API page, at
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3";
                final String POPULAR_URL = "/movie/popular?";
                final String TOP_RATED_URL = "/movie/top_rated?";

                final String API_KEY_PARAM = "api_key";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL.concat(POPULAR_URL)).buildUpon()
                        .appendQueryParameter(API_KEY_PARAM, BuildConfig.TMDB_API_KEY)
                        .build();

                URL url = new URL(builtUri.toString());

                Log.v(LOG_TAG, builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attemping
                // to parse it.
                return null;
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

            try {
                return getMovieDataFromJson(moviesJsonStr);
            } catch (JSONException e) {
                Log.e(LOG_TAG, e.getMessage(), e);
                e.printStackTrace();
            }
            Log.v(LOG_TAG, moviesJsonStr);
            // This will only happen if there was an error getting or parsing the forecast.
            return null;
        }

//        @Override
//        protected void onPostExecute(String[] result) {
//            if (result != null) {
//                mForecastAdapter.clear();
//                for(String dayForecastStr : result) {
//                    mForecastAdapter.add(dayForecastStr);
//                }
//                // New data is back from the server.  Hooray!
//            }
//        }
    }
}
