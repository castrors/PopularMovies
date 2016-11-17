package com.castrodev.popularmovies.data;

import net.simonvt.schematic.annotation.AutoIncrement;
import net.simonvt.schematic.annotation.DataType;
import net.simonvt.schematic.annotation.NotNull;
import net.simonvt.schematic.annotation.PrimaryKey;
import net.simonvt.schematic.annotation.Unique;

/**
 * Created by rodrigocastro on 31/10/16.
 */


public interface MovieColumns {

    @DataType(DataType.Type.INTEGER)
    @PrimaryKey
    @AutoIncrement
    public static final String _ID = "_id";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String REMOTE_ID = "remote_id";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String IMAGE_URL = "image_url";

    @DataType(DataType.Type.TEXT)
    @NotNull
    @Unique
    public static final String ORIGINAL_TITLE = "original_title";

    @DataType(DataType.Type.TEXT)
    @NotNull
    public static final String SYNOPSIS = "synopsis";

    @DataType(DataType.Type.REAL)
    @NotNull
    public static final String RATING = "rating";

    @DataType(DataType.Type.INTEGER)
    @NotNull
    public static final String RELEASE_DATE = "release_date";

    @DataType(DataType.Type.TEXT)
    public static final String SORTING_PREFERENCE = "sorting_preference";

}
