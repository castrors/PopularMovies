package com.castrodev.popularmovies.data;

import net.simonvt.schematic.annotation.Database;
import net.simonvt.schematic.annotation.Table;

/**
 * Created by rodrigocastro on 31/10/16.
 */
@Database(version = MovieDatabase.VERSION)
public final class MovieDatabase {
    private MovieDatabase(){}

    public static final int VERSION = 2;

    @Table(MovieColumns.class) public static final String MOVIES = "movies";

}

