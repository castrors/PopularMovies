package com.castrodev.popularmovies;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.castrodev.popularmovies.rest.Movie;

import static com.castrodev.popularmovies.MainActivityFragment.MOVIE_OBJECT;

/**
 * A placeholder fragment containing a simple view.
 */
public class DetailActivityFragment extends Fragment {

    private Movie movie;
    public DetailActivityFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView =  inflater.inflate(R.layout.fragment_detail, container, false);

        ImageView imageMoviePoster = (ImageView) rootView.findViewById(R.id.image_movie_poster);
        TextView textMovieTitle = (TextView) rootView.findViewById(R.id.text_movie_title);
        TextView textReleaseYear = (TextView) rootView.findViewById(R.id.text_release_year);
        TextView textRating = (TextView) rootView.findViewById(R.id.text_rating);
        TextView textSynopsis = (TextView) rootView.findViewById(R.id.text_synopsis);


        Intent intent = getActivity().getIntent();
        if(intent!=null && intent.hasExtra(MOVIE_OBJECT)){
            movie = intent.getParcelableExtra(MOVIE_OBJECT);

//            Picasso.with(getContext()).load(IMAGE_URL_PREFIX.concat(movie.imageUrl)).into(imageMoviePoster);
//            textMovieTitle.setText(movie.originalTitle);
//            textReleaseYear.setText(movie.getReleaseDate());
//            textRating.setText(movie.getRating());
//            textSynopsis.setText(movie.synopsis);
        }

        return rootView;
    }
}
