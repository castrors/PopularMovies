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
import android.widget.ImageView;
import android.widget.TextView;

import com.castrodev.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static com.castrodev.popularmovies.Utility.getYearFromMillis;
import static com.castrodev.popularmovies.rest.MovieCursorAdapter.IMAGE_URL_PREFIX;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {

    static final String DETAIL_URI = "URI";
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_REMOTE_ID,
            MovieContract.MovieEntry.COLUMN_IMAGE_URL,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_SORTING_PREFERENCE
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_REMOTE_ID = 1;
    public static final int COL_MOVIE_IMAGE_URL = 2;
    public static final int COL_MOVIE_ORIGINAL_TITLE = 3;
    public static final int COL_MOVIE_SYNOPSIS = 4;
    public static final int COL_MOVIE_RATING = 5;
    public static final int COL_MOVIE_RELEASE_DATE = 6;
    public static final int COL_MOVIE_SORTING_PREFERENCE = 7;

    private ImageView mImageMoviePoster;
    private TextView mTextMovieTitle;
    private TextView mTextReleaseYear;
    private TextView mTextRating;
    private TextView mTextSynopsis;

    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailActivityFragment.DETAIL_URI);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mImageMoviePoster = (ImageView) rootView.findViewById(R.id.image_movie_poster);
        mTextMovieTitle = (TextView) rootView.findViewById(R.id.text_movie_title);
        mTextReleaseYear = (TextView) rootView.findViewById(R.id.text_release_year);
        mTextRating = (TextView) rootView.findViewById(R.id.text_rating);
        mTextSynopsis = (TextView) rootView.findViewById(R.id.text_synopsis);

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if ( null != mUri ) {
            // Now create and return a CursorLoader that will take care of
            // creating a Cursor for the data being displayed.
            return new CursorLoader(
                    getActivity(),
                    mUri,
                    DETAIL_COLUMNS,
                    null,
                    null,
                    null
            );
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {

            String originalTitle = data.getString(COL_MOVIE_ORIGINAL_TITLE);
            String imageUrl = data.getString(COL_MOVIE_IMAGE_URL);
            Date releaseDate = new Date(data.getLong(COL_MOVIE_RELEASE_DATE));
            Double rating = data.getDouble(COL_MOVIE_RATING);
            String synopsis = data.getString(COL_MOVIE_SYNOPSIS);

            Picasso.with(getContext()).load(IMAGE_URL_PREFIX.concat(imageUrl)).into(mImageMoviePoster);
            mTextMovieTitle.setText(originalTitle);
            mTextReleaseYear.setText(getYearFromMillis(releaseDate.getTime()));
            mTextRating.setText(rating.toString()+"/10");
            mTextSynopsis.setText(synopsis);
        }



    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }
}
