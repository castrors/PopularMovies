package com.castrodev.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ListView;

import com.castrodev.popularmovies.data.MovieContract;
import com.castrodev.popularmovies.sync.PopularMoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>{
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieAdapter mMovieAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridView;

    public static final String MOVIE_OBJECT = "movie_object";
    private static final int MOVIES_LOADER = 0;

    private static final String[] MOVIES_COLUMNS = {
            // In this case the id needs to be fully qualified with a table name, since
            // the content provider joins the location & weather tables in the background
            // (both have an _id column)
            // On the one hand, that's annoying.  On the other, you can search the weather table
            // using the location set by the user, which is only in the Location table.
            // So the convenience is worth it.
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_IMAGE_URL,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_ORIGINAL_TITLE = 1;
    static final int COL_MOVIE_IMAGE_URL = 2;
    static final int COL_MOVIE_RATING = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_SYNOPSIS = 5;



    public interface Callback {

        void onItemSelected(Uri movieId);
    }

    public MainActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mGridView = (GridView) rootView.findViewById(R.id.movies_grid);
        mGridView.setAdapter(mMovieAdapter);
        // We'll call our MainActivity
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
//                if (cursor != null) {
//                    String locationSetting = Utility.getPreferredLocation(getActivity());
//                    ((Callback) getActivity())
//                            .onItemSelected(MovieContract.MovieEntry.buildMovieWithSortingPreference(
//                                    locationSetting, cursor.getLong(COL_WEATHER_DATE)
//                            ));
//                }
                mPosition = position;
            }
        });

        // If there's instance state, mine it for useful information.
        // The end-goal here is that the user never knows that turning their device sideways
        // does crazy lifecycle related things.  It should feel like some stuff stretched out,
        // or magically appeared to take advantage of room, but data or place in the app was never
        // actually *lost*.
//        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
//            // The listview probably hasn't even been populated yet.  Actually perform the
//            // swapout in onLoadFinished.
//            mPosition = savedInstanceState.getInt(SELECTED_KEY);
//        }

        return rootView;
    }


    @Override
    public void onStart() {
        super.onStart();
        updateMovies();
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(MOVIES_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    private void updateMovies() {
        PopularMoviesSyncAdapter.syncImmediately(getActivity());
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {
        String sortingPreference = Utility.getPreferredSorting(getActivity());
        Uri moviesUri = MovieContract.MovieEntry.buildMovieWithSortingPreference(
                sortingPreference);

        return new CursorLoader(getActivity(),
                moviesUri,
                MOVIES_COLUMNS,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridView.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
