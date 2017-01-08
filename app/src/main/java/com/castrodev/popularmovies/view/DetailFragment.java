package com.castrodev.popularmovies.view;

import android.content.ContentValues;
import android.content.Intent;
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
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.castrodev.popularmovies.listener.OnItemClickListener;
import com.castrodev.popularmovies.R;
import com.castrodev.popularmovies.adapter.TrailerAdapter;
import com.castrodev.popularmovies.data.MovieContract;
import com.squareup.picasso.Picasso;

import java.util.Date;

import static com.castrodev.popularmovies.util.Utility.getYearFromMillis;
import static com.castrodev.popularmovies.data.MovieContract.MovieEntry.FAVORITED_FALSE;
import static com.castrodev.popularmovies.data.MovieContract.MovieEntry.FAVORITED_TRUE;
import static com.castrodev.popularmovies.rest.MovieCursorAdapter.IMAGE_URL_PREFIX;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, OnItemClickListener {

    static final String DETAIL_URI = "URI";
    public static final String TRAILER_URL_PREFIX = "https://www.youtube.com/watch?v=";
    private Uri mUri;

    private static final int DETAIL_LOADER = 0;
    private static final int TRAILER_LOADER = 1;

    private static final String[] DETAIL_COLUMNS = {
            MovieContract.MovieEntry.TABLE_NAME + "." + MovieContract.MovieEntry._ID,
            MovieContract.MovieEntry.COLUMN_REMOTE_ID,
            MovieContract.MovieEntry.COLUMN_IMAGE_URL,
            MovieContract.MovieEntry.COLUMN_ORIGINAL_TITLE,
            MovieContract.MovieEntry.COLUMN_SYNOPSIS,
            MovieContract.MovieEntry.COLUMN_RATING,
            MovieContract.MovieEntry.COLUMN_RELEASE_DATE,
            MovieContract.MovieEntry.COLUMN_SORTING_PREFERENCE,
            MovieContract.MovieEntry.COLUMN_FAVORITED

    };

    private static final String[] TRAILER_COLUMNS = {
            MovieContract.TrailerEntry.TABLE_NAME + "." + MovieContract.TrailerEntry._ID,
            MovieContract.TrailerEntry.COLUMN_REMOTE_ID,
            MovieContract.TrailerEntry.COLUMN_KEY,
            MovieContract.TrailerEntry.COLUMN_NAME,
            MovieContract.TrailerEntry.COLUMN_ID
    };

    public static final int COL_MOVIE_ID = 0;
    public static final int COL_MOVIE_REMOTE_ID = 1;
    public static final int COL_MOVIE_IMAGE_URL = 2;
    public static final int COL_MOVIE_ORIGINAL_TITLE = 3;
    public static final int COL_MOVIE_SYNOPSIS = 4;
    public static final int COL_MOVIE_RATING = 5;
    public static final int COL_MOVIE_RELEASE_DATE = 6;
    public static final int COL_MOVIE_SORTING_PREFERENCE = 7;
    public static final int COL_MOVIE_FAVORITED = 8;

    public static final int COL_TRAILER_KEY = 2;
    public static final int COL_TRAILER_NAME = 3;

    private ImageView mImageMoviePoster;
    private TextView mTextMovieTitle;
    private TextView mTextReleaseYear;
    private TextView mTextRating;
    private TextView mTextSynopsis;
    private Button mButtonFavorited;
    private TrailerAdapter mTrailerAdapter;
    private ListView mListViewTrailers;
    private int favorited;
    private long movieRemoteId;

    public DetailFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        Bundle arguments = getArguments();
        if (arguments != null) {
            mUri = arguments.getParcelable(DetailFragment.DETAIL_URI);
            movieRemoteId = MovieContract.MovieEntry.getMovieRemoteIdFromUri(mUri);
        }
        View rootView = inflater.inflate(R.layout.fragment_detail, container, false);

        mImageMoviePoster = (ImageView) rootView.findViewById(R.id.image_movie_poster);
        mTextMovieTitle = (TextView) rootView.findViewById(R.id.text_movie_title);
        mTextReleaseYear = (TextView) rootView.findViewById(R.id.text_release_year);
        mTextRating = (TextView) rootView.findViewById(R.id.text_rating);
        mTextSynopsis = (TextView) rootView.findViewById(R.id.text_synopsis);
        mButtonFavorited = (Button) rootView.findViewById(R.id.button_favorited);
        mButtonFavorited.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ContentValues mUpdateValues = new ContentValues();

                String mSelectionClause = MovieContract.MovieEntry.COLUMN_REMOTE_ID + " = ?";
                String[] mSelectionArgs = {String.valueOf(movieRemoteId)};

                int mRowsUpdated = 0;

                mUpdateValues.put(MovieContract.MovieEntry.COLUMN_FAVORITED, favorited == FAVORITED_FALSE ? FAVORITED_TRUE : FAVORITED_FALSE);

                mRowsUpdated = getActivity().getContentResolver().update(
                        MovieContract.MovieEntry.CONTENT_URI,
                        mUpdateValues,
                        mSelectionClause,
                        mSelectionArgs
                );
            }
        });

        mTrailerAdapter = new TrailerAdapter(getActivity(), null, 0);

        mListViewTrailers = (ListView) rootView.findViewById(R.id.list_view_trailers);
        mListViewTrailers.setAdapter(mTrailerAdapter);
        mListViewTrailers.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                Cursor cursor = (Cursor) adapterView.getItemAtPosition(position);
                if (cursor != null) {
                    Uri link = Uri.parse(TRAILER_URL_PREFIX.concat(cursor.getString(DetailFragment.COL_TRAILER_KEY)));
                    Intent intent = new Intent(Intent.ACTION_VIEW, link);
                    startActivity(intent);
                }
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        getLoaderManager().initLoader(DETAIL_LOADER, null, this);
        getLoaderManager().initLoader(TRAILER_LOADER, null, this);
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {

        if (null != mUri) {
            switch (id) {
                case DETAIL_LOADER:
                    return new CursorLoader(
                            getActivity(),
                            mUri,
                            DETAIL_COLUMNS,
                            null,
                            null,
                            null
                    );
                case TRAILER_LOADER:
                    Uri trailerUri = MovieContract.TrailerEntry.buildTrailerWithMovieRemoteId(movieRemoteId);
                    return new CursorLoader(
                            getActivity(),
                            trailerUri,
                            TRAILER_COLUMNS,
                            null,
                            null,
                            null
                    );

            }
        }
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data != null && data.moveToFirst()) {
            switch (loader.getId()) {
                case DETAIL_LOADER:
                    String originalTitle = data.getString(COL_MOVIE_ORIGINAL_TITLE);
                    String imageUrl = data.getString(COL_MOVIE_IMAGE_URL);
                    Date releaseDate = new Date(data.getLong(COL_MOVIE_RELEASE_DATE));
                    Double rating = data.getDouble(COL_MOVIE_RATING);
                    String synopsis = data.getString(COL_MOVIE_SYNOPSIS);
                    favorited = data.getInt(COL_MOVIE_FAVORITED);
                    mButtonFavorited.setText(favorited == 0 ? R.string.mark_as_favorite : R.string.remove_favorite);

                    Picasso.with(getContext()).load(IMAGE_URL_PREFIX.concat(imageUrl)).into(mImageMoviePoster);
                    mTextMovieTitle.setText(originalTitle);
                    mTextReleaseYear.setText(getYearFromMillis(releaseDate.getTime()));
                    mTextRating.setText(rating.toString() + "/10");
                    mTextSynopsis.setText(synopsis);
                    break;
                case TRAILER_LOADER:
                    mTrailerAdapter.swapCursor(data);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

    }

    @Override
    public void onItemClick(Cursor cursor) {

    }
}
