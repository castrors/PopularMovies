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

import static com.castrodev.popularmovies.data.MovieContract.MovieEntry.FAVORITED_TRUE;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
    public static final String LOG_TAG = MainFragment.class.getSimpleName();

    private MovieAdapter mMovieAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    private GridView mGridViewMovies;

    private static final String SELECTED_KEY = "selected_position";

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
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_REMOTE_ID,
            MovieContract.MovieEntry.COLUMN_FAVORITED
    };

    static final int COL_MOVIE_ID = 0;
    static final int COL_MOVIE_ORIGINAL_TITLE = 1;
    public static final int COL_MOVIE_IMAGE_URL = 2;
    static final int COL_MOVIE_RATING = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_SYNOPSIS = 5;
    static final int COL_MOVIE_REMOTE_ID = 6;


    @Override
    public void onItemClick(Cursor cursor) {
        if (cursor != null) {
            String sortingPreference = Utility.getPreferredSorting(getActivity());
            ((Callback) getActivity())
                    .onItemSelected(MovieContract.MovieEntry.buildMovieSortingWithRemoteId(
                            sortingPreference, cursor.getLong(COL_MOVIE_REMOTE_ID)
                    ));
        }
    }


    public interface Callback {

        void onItemSelected(Uri movieId);
    }

    public MainFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // The ForecastAdapter will take data from a source and
        // use it to populate the ListView it's attached to.
        mMovieAdapter = new MovieAdapter(getActivity(), null, 0);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mGridViewMovies = (GridView) rootView.findViewById(R.id.movies_grid);
        mGridViewMovies.setAdapter(mMovieAdapter);

        mGridViewMovies.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // CursorAdapter returns a cursor at the correct position for getItem(), or null
                // if it cannot seek to that position.
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    String sortingPreference = Utility.getPreferredSorting(getActivity());
                    ((Callback) getActivity())
                            .onItemSelected(MovieContract.MovieEntry.buildMovieSortingWithRemoteId(
                                    sortingPreference, cursor.getLong(COL_MOVIE_REMOTE_ID)
                            ));
                }
                mPosition = position;
            }
        });

        if (savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
            // The listview probably hasn't even been populated yet.  Actually perform the
            // swapout in onLoadFinished.
            mPosition = savedInstanceState.getInt(SELECTED_KEY);
        }


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
        getLoaderManager().restartLoader(MOVIES_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle bundle) {

        String sortOrder = MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE + " ASC";

        String sortingPreference = Utility.getPreferredSorting(getActivity());

        Uri uri = MovieContract.MovieEntry.CONTENT_URI;
        String selectionClause = null;
        String[] selectionArgs = null;

        if (sortingPreference.equals(getString(R.string.pref_sorting_favorited))) {
            selectionClause = MovieContract.MovieEntry.COLUMN_FAVORITED + " = ?";
            selectionArgs = new String[]{String.valueOf(FAVORITED_TRUE)};
        } else {
            uri = MovieContract.MovieEntry.buildMovieWithSortingPreference(
                    sortingPreference);
        }
        
        return new CursorLoader(getActivity(),
                uri,
                MOVIES_COLUMNS,
                selectionClause,
                selectionArgs,
                sortOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mGridViewMovies.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
