package com.castrodev.popularmovies;

import java.util.Date;

/**
 * Created by rodrigocastro on 02/10/16.
 */

public class Movie {
    String imageUrl;
    String originalTitle;
    String synopsis;
    Double rating;
    Date releaseDate;

    public Movie(String imageUrl, String originalTitle, String synopsis, Double rating, Date releaseDate) {
        this.imageUrl = imageUrl;
        this.originalTitle = originalTitle;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;
    }

    @Override
    public String toString() {
        return "Movie{" +
                "imageUrl='" + imageUrl + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", rating=" + rating +
                ", releaseDate=" + releaseDate +
                '}';
    }
}
