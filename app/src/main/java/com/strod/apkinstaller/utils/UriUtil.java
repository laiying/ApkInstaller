package com.strod.apkinstaller.utils;

import android.net.Uri;

import java.io.File;

/**
 * Created by laiying on 2022/2/22.
 */
public class UriUtil {

    public static final String LOCAL_RESOURCE_SCHEME = "res";

    public static Uri getUriForResourceId(int resourceId) {
        return new Uri.Builder()
                .scheme(LOCAL_RESOURCE_SCHEME)
                .path(String.valueOf(resourceId))
                .build();
    }

    public static Uri getUriForFile(File file) {
        return Uri.fromFile(file);
    }
}
