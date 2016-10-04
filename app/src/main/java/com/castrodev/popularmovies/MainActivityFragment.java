package com.castrodev.popularmovies;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

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

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment {

    public static final String MOVIE_OBJECT = "movie_object";
    private MovieAdapter movieAdapter;

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        movieAdapter = new MovieAdapter(getActivity(), new ArrayList<Movie>());

        GridView gridView = (GridView) rootView.findViewById(R.id.movies_grid);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Movie movie = movieAdapter.getItem(position);
                Intent intent = new Intent(getActivity(), DetailActivity.class);
                intent.putExtra(MOVIE_OBJECT, movie);
                startActivity(intent);
            }
        });
        gridView.setAdapter(movieAdapter);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    private void updateMovies() {
        FetchMoviesTask fetchMoviesTask = new FetchMoviesTask();
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String sortingOption = prefs.getString(getString(R.string.pref_sorting_key),
                getString(R.string.pref_sorting_most_popular));

        fetchMoviesTask.execute(sortingOption);
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

            if (params.length == 0) {
                return null;
            }

            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            String moviesJsonStr = null;

            try {
                final String FORECAST_BASE_URL = "http://api.themoviedb.org/3";
                final String API_KEY_PARAM = "api_key";

                String sortingOption = params[0];
                Uri builtUri = Uri.parse(FORECAST_BASE_URL.concat(sortingOption)).buildUpon()
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
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                moviesJsonStr = buffer.toString();
            } catch (IOException e) {
                Log.e(LOG_TAG, "Error " + e);
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
            return null;
        }

        @Override
        protected void onPostExecute(Movie[] result) {
            if (result != null) {
                movieAdapter.clear();
                for(Movie movieResult : result) {
                    movieAdapter.add(movieResult);
                }
            }
        }
    }
}
