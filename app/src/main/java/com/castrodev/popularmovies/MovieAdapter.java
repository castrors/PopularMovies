package com.castrodev.popularmovies;

import android.app.Activity;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import java.util.List;

/**
 * Created by rodrigocastro on 03/10/16.
 */

public class MovieAdapter extends ArrayAdapter<Movie> {


    public static final String IMAGE_URL_PREFIX = "http://image.tmdb.org/t/p/w185/";

    public MovieAdapter(Activity context, List<Movie> movies) {
        super(context, 0, movies);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Movie movie = getItem(position);

        if(convertView ==null){
            convertView = LayoutInflater.from(getContext()).inflate(
                    R.layout.movie_item, parent, false);
        }

        ImageView movieImage = (ImageView) convertView.findViewById(R.id.movie_image);
        Picasso.with(getContext()).load(IMAGE_URL_PREFIX.concat(movie.imageUrl)).into(movieImage);

        return convertView;
    }
}
