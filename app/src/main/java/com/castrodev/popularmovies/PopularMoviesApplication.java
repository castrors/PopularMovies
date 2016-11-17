package com.castrodev.popularmovies;

import android.app.Application;

import com.facebook.stetho.Stetho;

public class PopularMoviesApplication extends Application {
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
    }
}