package com.castrodev.popularmovies;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ListView;

import com.castrodev.popularmovies.data.MovieContract;
import com.castrodev.popularmovies.data.MovieProvider;
import com.castrodev.popularmovies.rest.Movie;
import com.castrodev.popularmovies.rest.MovieCursorAdapter;
import com.castrodev.popularmovies.sync.PopularMoviesSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class MainActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {
    public static final String LOG_TAG = MainActivityFragment.class.getSimpleName();

    private MovieCursorAdapter mMovieCursorAdapter;
    private int mPosition = GridView.INVALID_POSITION;
    private RecyclerView mRecyclerViewMoviesGrid;

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
    public static final int COL_MOVIE_IMAGE_URL = 2;
    static final int COL_MOVIE_RATING = 3;
    static final int COL_MOVIE_RELEASE_DATE = 4;
    static final int COL_MOVIE_SYNOPSIS = 5;


    @Override
    public void onItemClick(Movie movie) {
        String sortingPreference = Utility.getPreferredSorting(getActivity());
//        ((Callback) getActivity()).onItemSelected(MovieContract.MovieEntry.buildMovieSortingWithRemoteId(sortingPreference, ));
    }


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
        mMovieCursorAdapter = new MovieCursorAdapter(getActivity(), null, this);

        View rootView = inflater.inflate(R.layout.fragment_main, container, false);

        // Get a reference to the ListView, and attach this adapter to it.
        mRecyclerViewMoviesGrid = (RecyclerView) rootView.findViewById(R.id.movies_grid);
        mRecyclerViewMoviesGrid.setLayoutManager(
                new GridLayoutManager(mRecyclerViewMoviesGrid.getContext(), 2)
        );
        mRecyclerViewMoviesGrid.setHasFixedSize(true);
        mRecyclerViewMoviesGrid.setAdapter(mMovieCursorAdapter);


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
//        Uri moviesUri = MovieContract.MovieEntry.buildMovieWithSortingPreference(
//                sortingPreference);

        return new CursorLoader(getActivity(),
                MovieProvider.Movies.CONTENT_URI,
                null,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mMovieCursorAdapter.swapCursor(data);
        if (mPosition != ListView.INVALID_POSITION) {
            // If we don't need to restart the loader, and there's a desired position to restore
            // to, do so now.
            mRecyclerViewMoviesGrid.smoothScrollToPosition(mPosition);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
