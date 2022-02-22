package com.strod.apkinstaller.manager;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.storage.StorageManager;
import android.os.storage.StorageVolume;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.text.TextUtils;

import com.strod.apkinstaller.R;
import com.strod.apkinstaller.compare.LocalFileNameAndTypeComparator;
import com.strod.apkinstaller.model.LocalFile;
import com.strod.apkinstaller.utils.FileUtils;
import com.strod.apkinstaller.utils.LocalStorageFileUtils;
import com.strod.apkinstaller.utils.UriUtil;

import java.io.File;
import java.io.FilenameFilter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static android.os.Build.VERSION.SDK_INT;

public class FileManager {

    private final static String TAG = FileManager.class.getSimpleName();
    private static FileManager sInstance;

    private FileManager() {
    }

    public static FileManager getInstance() {
        if (sInstance == null) {
            synchronized (FileManager.class) {
                if (sInstance == null) {
                    sInstance = new FileManager();
                }
            }
        }
        return sInstance;
    }


    public List<LocalFile> getFileListFromLocal(Context context) {
        List<LocalFile> fileBeanList = new ArrayList<>();
        Cursor c = null;
        try {
            c = context.getContentResolver().query(MediaStore.Files.getContentUri("external")
                    , new String[]{"_id", "_data", "_size", "_display_name", "mime_type"}, null, null
                    , MediaStore.Files.FileColumns.DATE_MODIFIED + " desc ");
            while (c.moveToNext()) {
//                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Audio.Media._ID));
                int id = c.getInt(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns._ID));
                String path = c.getString(c.getColumnIndex(MediaStore.Files.FileColumns.DATA));
                long size = c.getLong(c.getColumnIndex(MediaStore.Files.FileColumns.SIZE));
                String name = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DISPLAY_NAME));
//                String type = c.getString(c.getColumnIndexOrThrow(MediaStore.Files.FileColumns.MIME_TYPE));
//                bean.setMine_type(type);

                //过滤文件不存在的
                File targetFile = new File(path);
                if (!targetFile.exists()){
                    continue;
                }
                LocalFile bean;
                if (FileUtils.isDocumentType(path)) {
                    File file = new File(path);
                    //隐藏文件不展示
                    if (file.isHidden()) {
                        continue;
                    }
                    bean = new LocalFile();
                    bean.setId(id);
                    bean.setPath(path);
                    bean.setName(name);
                    bean.setSize(size);
                    bean.setTime(file.lastModified());
                    if (FileUtils.isWordTypeForDocument(path)) {
                        bean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_word));
                    }
                    if (FileUtils.isExcelTypeForDocument(path)) {
                        bean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_excel));
                    }
                    if (FileUtils.isPPTTypeForDocument(path)) {
                        bean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_ppt));
                    }
                    if (FileUtils.isPdfTypeForDocument(path)) {
                        bean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_pdf));
                    }
                    if (FileUtils.isTxtAndXmlTypeForDocument(path)) {
                        bean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_txt));
                    }

                    fileBeanList.add(bean);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (c != null) {
                c.close();
            }
        }
        return fileBeanList;
    }

    private long getFileLength(File parentFile , boolean isPhoto , boolean isVideo) {
        return FileUtils.sizeOfDirPhotoVideo(parentFile , isPhoto ,isVideo);
    }


    private static final String DEFAULT_FALLBACK_STORAGE_PATH = "/storage/sdcard0";
    private static final String INTERNAL_SHARED_STORAGE = "Internal shared storage";
    private static final String INTERNAL_SHARED_STORAGE_ZH = "内部共享存储空间";
    public static final Pattern DIR_SEPARATOR = Pattern.compile("/");
    public static final String PREFIX_OTG = "otg:/";

    /** @return paths to all available volumes in the system (include emulated) */
    public synchronized ArrayList<LocalFile> getStorageDirectories(Context context) {
        ArrayList<LocalFile> volumes;
        if (SDK_INT >= Build.VERSION_CODES.N) {
            volumes = getStorageDirectoriesNew(context);
        } else {
            volumes = getStorageDirectoriesLegacy(context);
        }
        if (isRootExplorer()) {
            volumes.add(
                    new LocalFile(
                            "/",
                            "根目录",
                            UriUtil.getUriForResourceId(R.mipmap.ic_drawer_root_white)));
        }
        return volumes;
    }

    /**
     * @return All available storage volumes (including internal storage, SD-Cards and USB devices)
     */
    @TargetApi(Build.VERSION_CODES.N)
    public synchronized ArrayList<LocalFile> getStorageDirectoriesNew(Context context) {
        // Final set of paths
        ArrayList<LocalFile> volumes = new ArrayList<>();
        StorageManager sm = context.getApplicationContext().getSystemService(StorageManager.class);
        for (StorageVolume volume : sm.getStorageVolumes()) {
            if (!volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED)
                    && !volume.getState().equalsIgnoreCase(Environment.MEDIA_MOUNTED_READ_ONLY)) {
                continue;
            }
            File path = LocalStorageFileUtils.getVolumeDirectory(volume);
            String name = volume.getDescription(context);
            if (INTERNAL_SHARED_STORAGE.equalsIgnoreCase(name) || INTERNAL_SHARED_STORAGE_ZH.equals(name)) {
                name = "内部存储";
            }
            Uri icon;
            if (!volume.isRemovable()) {
                icon = UriUtil.getUriForResourceId(R.mipmap.ic_phone_android);
            } else {
                // HACK: There is no reliable way to distinguish USB and SD external storage
                // However it is often enough to check for "USB" String
                if (name.toUpperCase().contains("USB") || path.getPath().toUpperCase().contains("USB")) {
                    icon = UriUtil.getUriForResourceId(R.mipmap.ic_usb);
                } else {
                    icon = UriUtil.getUriForResourceId(R.mipmap.ic_sd_storage);
                }
            }
            LocalFile baseLocalFileBean = new LocalFile(name, path.getPath(), icon);
            baseLocalFileBean.setDirectory(true);
            volumes.add(baseLocalFileBean);
        }
        return volumes;
    }

    /**
     * Returns all available SD-Cards in the system (include emulated)
     *
     * <p>Warning: Hack! Based on Android source code of version 4.3 (API 18) Because there was no
     * standard way to get it before android N
     *
     * @return All available SD-Cards in the system (include emulated)
     */
    public synchronized ArrayList<LocalFile> getStorageDirectoriesLegacy(Context context) {
        List<String> rv = new ArrayList<>();

        // Primary physical SD-CARD (not emulated)
        final String rawExternalStorage = System.getenv("EXTERNAL_STORAGE");
        // All Secondary SD-CARDs (all exclude primary) separated by ":"
        final String rawSecondaryStoragesStr = System.getenv("SECONDARY_STORAGE");
        // Primary emulated SD-CARD
        final String rawEmulatedStorageTarget = System.getenv("EMULATED_STORAGE_TARGET");
        if (TextUtils.isEmpty(rawEmulatedStorageTarget)) {
            // Device has physical external storage; use plain paths.
            if (TextUtils.isEmpty(rawExternalStorage)) {
                // EXTERNAL_STORAGE undefined; falling back to default.
                // Check for actual existence of the directory before adding to list
                if (new File(DEFAULT_FALLBACK_STORAGE_PATH).exists()) {
                    rv.add(DEFAULT_FALLBACK_STORAGE_PATH);
                } else {
                    // We know nothing else, use Environment's fallback
                    rv.add(Environment.getExternalStorageDirectory().getAbsolutePath());
                }
            } else {
                rv.add(rawExternalStorage);
            }
        } else {
            // Device has emulated storage; external storage paths should have
            // userId burned into them.
            final String rawUserId;
            if (SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR1) {
                rawUserId = "";
            } else {
                final String path = Environment.getExternalStorageDirectory().getAbsolutePath();
                final String[] folders = DIR_SEPARATOR.split(path);
                final String lastFolder = folders[folders.length - 1];
                boolean isDigit = false;
                try {
                    Integer.valueOf(lastFolder);
                    isDigit = true;
                } catch (NumberFormatException ignored) {
                }
                rawUserId = isDigit ? lastFolder : "";
            }
            // /storage/emulated/0[1,2,...]
            if (TextUtils.isEmpty(rawUserId)) {
                rv.add(rawEmulatedStorageTarget);
            } else {
                rv.add(rawEmulatedStorageTarget + File.separator + rawUserId);
            }
        }
        // Add all secondary storages
        if (!TextUtils.isEmpty(rawSecondaryStoragesStr)) {
            // All Secondary SD-CARDs splited into array
            final String[] rawSecondaryStorages = rawSecondaryStoragesStr.split(File.pathSeparator);
            Collections.addAll(rv, rawSecondaryStorages);
        }
        if (SDK_INT >= Build.VERSION_CODES.M && checkStoragePermission(context)) rv.clear();
        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String strings[] = LocalStorageFileUtils.getExtSdCardPathsForActivity(context);
            for (String s : strings) {
                File f = new File(s);
                if (!rv.contains(s) && LocalStorageFileUtils.canListFiles(f)) rv.add(s);
            }
        }
        File usb = getUsbDrive();
        if (usb != null && !rv.contains(usb.getPath())) rv.add(usb.getPath());

        if (SDK_INT >= Build.VERSION_CODES.KITKAT) {
            if (SingletonUsbOtg.getInstance().isDeviceConnected()) {
                rv.add(PREFIX_OTG + "/");
            }
        }

        // Assign a label and icon to each directory
        ArrayList<LocalFile> volumes = new ArrayList<>();
        for (String file : rv) {
            File f = new File(file);
            Uri icon;

            if ("/storage/emulated/legacy".equals(file)
                    || "/storage/emulated/0".equals(file)
                    || "/mnt/sdcard".equals(file)) {
                icon = UriUtil.getUriForResourceId(R.mipmap.ic_phone_android);
            } else if ("/storage/sdcard1".equals(file)) {
                icon = UriUtil.getUriForResourceId(R.mipmap.ic_sd_storage);
            } else if ("/".equals(file)) {
                icon = UriUtil.getUriForResourceId(R.mipmap.ic_drawer_root_white);
            } else {
                icon = UriUtil.getUriForResourceId(R.mipmap.ic_sd_storage);
            }

            String name = LocalStorageFileUtils.getDeviceDescriptionLegacy(context, f);
            LocalFile baseLocalFileBean = new LocalFile(name, file, icon);
            baseLocalFileBean.setDirectory(true);
            volumes.add(baseLocalFileBean);
        }

        return volumes;
    }

    public boolean isRootExplorer() {
        return false;
//        return getBoolean(PREFERENCE_ROOTMODE);
    }

    public boolean checkStoragePermission(Context context) {
        // Verify that all required contact permissions have been granted.
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
    }

    public File getUsbDrive() {
        File parent = new File("/storage");

        try {
            for (File f : parent.listFiles())
                if (f.exists() && f.getName().toLowerCase().contains("usb") && f.canExecute()) return f;
        } catch (Exception e) {
        }

        parent = new File("/mnt/sdcard/usbStorage");
        if (parent.exists() && parent.canExecute()) return parent;
        parent = new File("/mnt/sdcard/usb_storage");
        if (parent.exists() && parent.canExecute()) return parent;

        return null;
    }

    /**
     * 遍历文件夹下所有文件
     * @param file
     * @return
     */
    public List<LocalFile> getFiles(File file){
        long startTime = System.currentTimeMillis();
        List<LocalFile> localFileBeans = new ArrayList<>();
        if (!file.exists() || !file.canRead()){//canRead() 5.18% exists() 4.86%
            return localFileBeans;
        }
        File[] files = file.listFiles();//listFiles() 1.46%
        if (files == null || files.length == 0){
            return localFileBeans;
        }
        //排序
        Arrays.sort(files, new LocalFileNameAndTypeComparator());//17.89%
        for (File f : files){
            if(FileUtils.isFilterFile(f)){
                continue;
            }
//            if (f.isHidden() || !f.exists() || !f.canRead()){//canRead() 5.18% exists() 4.86% isHidden() 1.17%
//                //过滤系统隐藏、不存在、不可读文件
//                continue;
//            }
            String path = f.getPath();
            LocalFile localFileBean = new LocalFile(f.getName(),path,f.lastModified(),f.length());//length() 4.55%
            if (f.isDirectory()){//isDirectory() 1.19%
                localFileBean.setDirectory(true);
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_dirs));
                localFileBean.setSize(-1/*FileUtils.sizeOfDirectory(f)*/);
            }else if (FileUtils.isImageFast(path)) {//isImageFast() 1.73%
                localFileBean.setImage(true);
                localFileBean.setImgPath(UriUtil.getUriForFile(f));
            }else if (FileUtils.isVideoTypeFast(path)) {//isVideoTypeFast() 1.47%
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_video));
            }else if (FileUtils.isAudioTypeFast(path)) {//isAudioTypeFast() 1.41%
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_audio));
            }else if (FileUtils.isWordTypeForDocument(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_word));
            }else if (FileUtils.isExcelTypeForDocument(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_excel));
            }else if (FileUtils.isPPTTypeForDocument(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_ppt));
            }else if (FileUtils.isPdfTypeForDocument(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_pdf));
            }else if (FileUtils.isTxtAndXmlTypeForDocument(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_txt));
            }else if (FileUtils.isZipForOtherType(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_zip));
            } else if (FileUtils.isApkType(path)) {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_apk));
            }else {
                localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_others));
            }
            localFileBeans.add(localFileBean);
        }
        long endTime = System.currentTimeMillis();
        return localFileBeans;
    }

    SimpleDateFormat sdf;
    public String formatTime(long time, String pattern) {
        if (sdf == null){
            sdf = new SimpleDateFormat(pattern);
        }
        return sdf.format(new Date(time));
    }
}
