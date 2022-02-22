package com.strod.apkinstaller.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.storage.StorageVolume;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by laiying on 2020/10/29.
 */
public class LocalStorageFileUtils {

    private static final String TAG = LocalStorageFileUtils.class.getSimpleName();

    @TargetApi(Build.VERSION_CODES.N)
    public static File getVolumeDirectory(StorageVolume volume) {
        try {
            Field f = StorageVolume.class.getDeclaredField("mPath");
            f.setAccessible(true);
            return (File) f.get(volume);
        } catch (Exception e) {
            // This shouldn't fail, as mPath has been there in every version
            throw new RuntimeException(e);
        }
    }

    /** Retrofit of {@link StorageVolume#getDescription(Context)} to older apis */
    public static String getDeviceDescriptionLegacy(Context context, File file) {
        String path = file.getPath();

        switch (path) {
            case "/storage/emulated/legacy":
            case "/storage/emulated/0":
            case "/mnt/sdcard":
                return "内部存储";
            case "/storage/sdcard":
            case "/storage/sdcard1":
                return "SD卡";
            case "/":
                return "根目录";
            default:
                return file.getName();
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static String[] getExtSdCardPathsForActivity(Context context) {
        List<String> paths = new ArrayList<>();
        for (File file : context.getExternalFilesDirs("external")) {
            if (file != null) {
                int index = file.getAbsolutePath().lastIndexOf("/Android/data");
                if (index < 0) {
                    Log.w(TAG, "Unexpected external file dir: " + file.getAbsolutePath());
                } else {
                    String path = file.getAbsolutePath().substring(0, index);
                    try {
                        path = new File(path).getCanonicalPath();
                    } catch (IOException e) {
                        // Keep non-canonical path.
                    }
                    paths.add(path);
                }
            }
        }
        if (paths.isEmpty()) paths.add("/storage/sdcard1");
        return paths.toArray(new String[0]);
    }

    public static boolean canListFiles(File f) {
        return f.canRead() && f.isDirectory();
    }
}
