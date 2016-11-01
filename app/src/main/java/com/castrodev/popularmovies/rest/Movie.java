package com.castrodev.popularmovies.rest;

import android.database.Cursor;
import android.os.Parcel;
import android.os.Parcelable;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Created by rodrigocastro on 02/10/16.
 */

public class Movie implements Parcelable {

    public static final String DATE_YEAR_PATTERN = "yyyy";

    Long remoteId;
    String imageUrl;
    String originalTitle;
    String synopsis;
    String sortingPreference;
    Double rating;
    Date releaseDate;

    public Movie(Long remoteId, String imageUrl, String originalTitle, String synopsis, String sortingPreference, Double rating, Date releaseDate) {
        this.remoteId = remoteId;
        this.imageUrl = imageUrl;
        this.originalTitle = originalTitle;
        this.sortingPreference = sortingPreference;
        this.synopsis = synopsis;
        this.rating = rating;
        this.releaseDate = releaseDate;

    }

    protected Movie(Parcel in) {
        remoteId = in.readLong();
        imageUrl = in.readString();
        originalTitle = in.readString();
        sortingPreference = in.readString();
        synopsis = in.readString();
        rating = in.readDouble();
        releaseDate = new Date(in.readLong());
    }

    @Override
    public String toString() {
        return "Movie{" +
                "remoteId='" + remoteId + '\'' +
                ", imageUrl='" + imageUrl + '\'' +
                ", originalTitle='" + originalTitle + '\'' +
                ", sortingPreference='" + sortingPreference + '\'' +
                ", synopsis='" + synopsis + '\'' +
                ", rating=" + rating +
                ", releaseDate=" + releaseDate +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(remoteId);
        parcel.writeString(imageUrl);
        parcel.writeString(originalTitle);
        parcel.writeString(sortingPreference);
        parcel.writeString(synopsis);
        parcel.writeDouble(rating);
        parcel.writeLong(releaseDate.getTime());

    }

    public static final Creator<Movie> CREATOR = new Creator<Movie>() {
        @Override
        public Movie createFromParcel(Parcel in) {
            return new Movie(in);
        }

        @Override
        public Movie[] newArray(int size) {
            return new Movie[size];
        }
    };

    public String getReleaseDate() {
        SimpleDateFormat date = new SimpleDateFormat(DATE_YEAR_PATTERN, Locale.getDefault());
        return date.format(releaseDate);
    }

    public String getRating() {
        return rating.toString()+"/10";
    }

    public static Movie fromCursor(Cursor cursor) {
        return null;
    }
}
