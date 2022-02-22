package com.strod.apkinstaller.utils;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class FileUtils {

    private static final int BUFFER = 8192;
    public final static String FILE_MINE_TYPE_VIDEO = "video";
    public final static String FILE_MINE_TYPE_AUDIO = "audio";
    public final static String FILE_MINE_TYPE_IMAGE = "image";
    public final static String FILE_MINE_TYPE_FILE = "file";

    private final static List<String> IMAGE_EXTENSION_COLLECTIONS = Arrays.asList(".jpg", ".png", ".jpeg",".bmp", ".gif"
            , ".heif",".heic");
    private final static Map<String, Boolean> IMAGE_EXTENSION_MAPS= new HashMap<String, Boolean>(){{
        put(".jpg", Boolean.TRUE);
        put(".png", Boolean.TRUE);
        put(".jpeg", Boolean.TRUE);
        put(".bmp", Boolean.TRUE);
        put(".gif", Boolean.TRUE);
        put(".heif", Boolean.TRUE);
        put(".heic", Boolean.TRUE);
    }};

    private final static List<String> VIDEO_EXTENSION_COLLECTIONS = Arrays.asList(".mp4", ".3gp", ".rm",".rmvb", ".avi"
            , ".wmv", ".mov", ".mkv", ".mpeg", ".vob", ".hevc", ".flv");
    private final static Map<String, Boolean> VIDEO_EXTENSION_MAPS= new HashMap<String, Boolean>(){{
        put(".mp4", Boolean.TRUE);
        put(".3gp", Boolean.TRUE);
        put(".rm", Boolean.TRUE);
        put(".rmvb", Boolean.TRUE);
        put(".avi", Boolean.TRUE);
        put(".wmv", Boolean.TRUE);
        put(".mov", Boolean.TRUE);
        put(".mkv", Boolean.TRUE);
        put(".mpeg", Boolean.TRUE);
        put(".vob", Boolean.TRUE);
        put(".hevc", Boolean.TRUE);
        put(".flv", Boolean.TRUE);
    }};

    private final static List<String> SYS_VIDEO_EXTENSION_COLLECTIONS = Arrays.asList(".mp4", ".3gp", ".avi"
            , ".wmv", ".mov", ".mkv", ".mpeg", ".hevc", ".flv");
    private final static Map<String, Boolean> SYS_VIDEO_EXTENSION_MAPS = new HashMap<String, Boolean>(){{
        put(".mp4", Boolean.TRUE);
        put(".3gp", Boolean.TRUE);
        put(".avi", Boolean.TRUE);
        put(".wmv", Boolean.TRUE);
        put(".mov", Boolean.TRUE);
        put(".mkv", Boolean.TRUE);
        put(".mpeg", Boolean.TRUE);
        put(".hevc", Boolean.TRUE);
        put(".flv", Boolean.TRUE);
    }};

    private final static List<String> AUDIO_EXTENSION_COLLECTIONS = Arrays.asList(".mp3", ".wav", ".wma");
    private final static Map<String, Boolean> AUDIO_EXTENSION_MAPS= new HashMap<String, Boolean>(){{
        put(".mp3", Boolean.TRUE);
        put(".wav", Boolean.TRUE);
        put(".wma", Boolean.TRUE);
    }};

    private static final String[] HEIF_HEADER_SUFFIXES = {
            "heic", "heix", "hevc", ".heif",".avci","hevx", "mif1", "msf1"
    };

    // 读取文件
    public static String readTextFile(File file) throws IOException {
        String text = null;
        InputStream is = null;
        try {
            is = new FileInputStream(file);
            text = readTextInputStream(is);
        } finally {
            if (is != null) {
                is.close();
            }
        }
        return text;
    }

    // 从流中读取文件
    public static String readTextInputStream(InputStream is) throws IOException {
        StringBuffer strbuffer = new StringBuffer();
        String line;
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            while ((line = reader.readLine()) != null) {
                strbuffer.append(line).append("\r\n");
            }
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
        return strbuffer.toString();
    }

    // 将文本内容写入文件
    public static void writeTextFile(File file, String str) throws IOException {
        DataOutputStream out = null;
        try {
            out = new DataOutputStream(new FileOutputStream(file));
            out.write(str.getBytes());
        } finally {
            if (out != null) {
                out.close();
            }
        }
    }

    /**
     * 根据URL获取文件名
     *
     * @param url
     * @param suffix
     * @return
     */
    public static String getFileNameFromUrl(String url, String suffix) {
        // 通过 ‘？’ 和 ‘/’ 判断文件名
        if (TextUtils.isEmpty(url)) {
            return "";
        }
        int index = url.lastIndexOf('?');
        String filename = null;
        try {
            int startLastxg = url.lastIndexOf('/') + 1;
            if (index > 1) {
                if (index < startLastxg) {
                    return getFileNameFromUrl(url.substring(0, index), suffix);
                }
                filename = url.substring(startLastxg, index);
            } else {
                filename = url.substring(startLastxg);
            }
        } catch (Exception e) {
        }
        if (filename == null || "".equals(filename.trim())) {// 如果获取不到文件名称
            filename = UUID.randomUUID() + suffix;// 默认取一个文件名
        }
        if (filename.indexOf(".") == -1) {
            filename = filename + suffix;
        }
        return filename;
    }

    // 复制文件
    public static void copyFile(File sourceFile, File targetFile)
            throws IOException {
        BufferedInputStream inBuff = null;
        BufferedOutputStream outBuff = null;
        try {
            inBuff = new BufferedInputStream(new FileInputStream(sourceFile));
            outBuff = new BufferedOutputStream(new FileOutputStream(targetFile));
            byte[] buffer = new byte[BUFFER];
            int length;
            while ((length = inBuff.read(buffer)) != -1) {
                outBuff.write(buffer, 0, length);
            }
            outBuff.flush();
        } finally {
            if (inBuff != null) {
                inBuff.close();
            }
            if (outBuff != null) {
                outBuff.close();
            }
        }
    }

    public static void copyFile(String oldPath, String newPath) {
        try {
            int bytesum = 0;
            int byteread = 0;
            File oldfile = new File(oldPath);
            if (oldfile.exists()) {                                    // 文件存在时
                InputStream inStream = new FileInputStream(oldPath);    // 读入原文件
                FileOutputStream fs = new FileOutputStream(newPath);
                byte[] buffer = new byte[1024];
                while ((byteread = inStream.read(buffer)) != -1) {
                    bytesum += byteread;                                // 字节数 文件大小
                    fs.write(buffer, 0, byteread);
                }
                inStream.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    /**
     * 只删除目录下的文件
     *
     * @param filePath
     * @return
     */
    public static boolean deleteDirectoryOnlyFile(String filePath) {
        return deleteDirectory(filePath, false, true);
    }


    /**
     * 删除目录下的文件
     *
     * @param filePath
     * @param delDir   是否删除目录
     * @return
     */
    public static boolean deleteDirectory(String filePath, boolean delDir) {
        return deleteDirectory(filePath, delDir, false);
    }

    public static boolean deleteDirectory(String filePath, boolean delDir, boolean onlyFile) {
        if (filePath == null) {
            return false;
        }
        // 如果sPath不以文件分隔符结尾，自动添加文件分隔符

        if (!filePath.endsWith(File.separator)) {
            filePath = filePath + File.separator;
        }
        File dirFile = new File(filePath);
        // 如果dir对应的文件不存在，或者不是一个目录，则退出
        if (!dirFile.exists() || !dirFile.isDirectory()) {
            return false;
        }
        boolean flag = true;
        // 删除文件夹下的所有文件(包括子目录)
        File[] files = dirFile.listFiles();
        for (int i = 0; i < files.length; i++) {
            // 删除子文件
            if (files[i].isFile()) {
                flag = deleteFile(files[i].getAbsolutePath());
                if (!flag) {
                    break;
                }
            } // 删除子目录
            else {
                if (!onlyFile) {
                    flag = deleteDirectory(files[i].getAbsolutePath());
                    if (!flag) {
                        break;
                    }
                }
            }
        }
        if (!flag) {
            return false;
        }

        if (delDir) {
            // 删除当前目录
            if (dirFile.delete()) {
                return true;
            } else {
                return false;
            }
        }
        return true;

    }

    /**
     * 删除目录（文件夹）以及目录下的文件
     *
     * @param filePath 被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
    public static boolean deleteDirectory(String filePath) {
        return deleteDirectory(filePath, true);
    }

    /**
     * 删除单个文件
     *
     * @param filePath 被删除文件的文件名
     * @return 文件删除成功返回true，否则返回false
     */
    public static boolean deleteFile(String filePath) {
        File file = new File(filePath);
        if (file.isFile() && file.exists()) {
            return file.delete();
        }
        return false;
    }

    /**
     * 删除文件夹以及目录下的文件
     *
     * @param filePath
     *            被删除目录的文件路径
     * @return 目录删除成功返回true，否则返回false
     */
	/*public static boolean deleteDirectory(String filePath) {
		boolean flag = false;
		// 如果filePath不以文件分隔符结尾，自动添加文件分隔符
		if (!filePath.endsWith(File.separator)) {
			filePath = filePath + File.separator;
		}
		File dirFile = new File(filePath);
		if (!dirFile.exists() || !dirFile.isDirectory()) {
			return false;
		}
		flag = true;
		File[] files = dirFile.listFiles();
		// 遍历删除文件夹下的所有文件(包括子目录)
		for (int i = 0; i < files.length; i++) {
			if (files[i].isFile()) {
				// 删除子文件
				flag = deleteFile(files[i].getAbsolutePath());
				if (!flag)
					break;
			} else {
				// 删除子目录
				flag = deleteDirectory(files[i].getAbsolutePath());
				if (!flag)
					break;
			}
		}
		if (!flag)
			return false;
		// 删除当前空目录
		return dirFile.delete();
	}*/

    /**
     * 根据路径删除指定的目录或文件，无论存在与否
     *
     * @param filePath 要删除的目录或文件
     * @return 删除成功返回 true，否则返回 false。
     */
    public static boolean DeleteFolder(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        } else {
            if (file.isFile()) {
                // 为文件时调用删除文件方法
                return deleteFile(filePath);
            } else {
                // 为目录时调用删除目录方法
                return deleteDirectory(filePath);
            }
        }
    }


    /**
     * 删除旧版APk包
     * @param appName
     */
    public static boolean deleteOldApkPackage(String appName){
        if(TextUtils.isEmpty(appName)){
            return false;
        }
        File oldApkFile = null;
        File publicDownloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        if (publicDownloads != null){
            oldApkFile = new File(publicDownloads.getPath(), appName+".apk");
        }
        if (oldApkFile != null && oldApkFile.exists() && oldApkFile.isFile() ) {
            return oldApkFile.delete();
        }
        return false;
    }

    /**
     * 判断是否是图片文件
     *
     * @param name
     * @return
     */
    public static boolean isImage(String name) {
        boolean flag = false;
        try {
            name = name.toLowerCase();
            for (String extension : IMAGE_EXTENSION_COLLECTIONS) {
                if (name.endsWith(extension)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    /**
     * 比isImage更快判断是否图片
     * @param name
     * @return
     */
    public static boolean isImageFast(String name) {
        if (TextUtils.isEmpty(name)){
            return false;
        }
        int index = name.lastIndexOf(".");
        if (index > 0){
            name = name.substring(index);
        }
        Boolean isAudio = IMAGE_EXTENSION_MAPS.get(name.toLowerCase());
        if (isAudio == null){
            isAudio = Boolean.FALSE;
        }
        return isAudio;
    }

    /**
     * 是否是heif格式图片
     * @param fileName
     * @return
     */
    public static boolean isHeifImage(String fileName){
        if (TextUtils.isEmpty(fileName)){
            return false;
        }
        boolean flag = false;
        try {
            fileName = fileName.toLowerCase();
            for (String extension : HEIF_HEADER_SUFFIXES) {
                if (fileName.endsWith(extension)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    public static boolean isVideoType(String name) {
        boolean flag = false;
        try {
            name = name.toLowerCase();
            for (String extension : VIDEO_EXTENSION_COLLECTIONS) {
                if (name.endsWith(extension)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    /**
     * 比isVideoType更快判断是否视频
     * @param name
     * @return
     */
    public static boolean isVideoTypeFast(String name) {
        if (TextUtils.isEmpty(name)){
            return false;
        }
        int index = name.lastIndexOf(".");
        if (index > 0){
            name = name.substring(index);
        }
        Boolean isAudio = VIDEO_EXTENSION_MAPS.get(name.toLowerCase());
        if (isAudio == null){
            isAudio = Boolean.FALSE;
        }
        return isAudio;
    }

    public static boolean isSysVideoType(String name) {
        boolean flag = false;
        try {
            name = name.toLowerCase();
            for (String extension : SYS_VIDEO_EXTENSION_COLLECTIONS) {
                if (name.endsWith(extension)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    public static boolean isSysVideoTypeFast(String name){
        if (TextUtils.isEmpty(name)){
            return false;
        }
        int index = name.lastIndexOf(".");
        if (index > 0){
            name = name.substring(index);
        }
        Boolean isAudio = SYS_VIDEO_EXTENSION_MAPS.get(name.toLowerCase());
        if (isAudio == null){
            isAudio = Boolean.FALSE;
        }
        return isAudio;
    }

    public static boolean isAudioType(String name) {
        boolean flag = false;
        try {
            name = name.toLowerCase();
            for (String extension : AUDIO_EXTENSION_COLLECTIONS) {
                if (name.endsWith(extension)) {
                    flag = true;
                    break;
                }
            }
        } catch (Exception e) {
        }
        return flag;
    }

    /**
     * 比isAudioType更快判断是否音频
     * @param name
     * @return
     */
    public static boolean isAudioTypeFast(String name) {
        if (TextUtils.isEmpty(name)){
            return false;
        }
        int index = name.lastIndexOf(".");
        if (index > 0){
            name = name.substring(index);
        }
        Boolean isAudio = AUDIO_EXTENSION_MAPS.get(name.toLowerCase());
        if (isAudio == null){
            isAudio = Boolean.FALSE;
        }
        return isAudio;
    }

    public static boolean isDocumentType(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".doc") || path.endsWith(".docm") || path.endsWith(".docx")
                || path.endsWith(".dotx") || path.endsWith(".odt") || path.endsWith(".ods")
                || path.endsWith(".xla") || path.endsWith(".xlam") || path.endsWith(".xlsb")
                || path.endsWith(".xlsx") || path.endsWith(".xltx") || path.endsWith(".xml")
                || path.endsWith(".pot") || path.endsWith(".potm") || path.endsWith(".potx")
                || path.endsWith(".pdf") || path.endsWith(".txt") || path.endsWith(".ppt");
    }

    public static boolean isWordTypeForDocument(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".doc") || path.endsWith(".docm") || path.endsWith(".docx")
                || path.endsWith(".dotx") || path.endsWith(".odt");
    }

    public static boolean isExcelTypeForDocument(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".ods") || path.endsWith(".xla") || path.endsWith(".xlam")
                || path.endsWith(".xlsb") || path.endsWith(".xlsx") || path.endsWith(".xltx");
    }

    public static boolean isPPTTypeForDocument(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".pot") || path.endsWith(".potm")
                || path.endsWith(".potx") || path.endsWith(".ppt");
    }

    public static boolean isTxtAndXmlTypeForDocument(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".xml") || path.endsWith(".txt");
    }

    public static boolean isPdfTypeForDocument(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".pdf");
    }

    public static boolean isOtherType(String path) {
        return !isImage(path) && !isVideoType(path)
                && !isDocumentType(path) && !isAudioType(path);
    }

    public static boolean isZipForOtherType(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".zip") || path.endsWith(".rar") || path.endsWith(".7z")|| path.endsWith(".tar.gz");
    }

    public static boolean isApkType(String path) {
        if (TextUtils.isEmpty(path)){
            return false;
        }
        path = path.toLowerCase();
        return path.endsWith(".apk");
    }

    public static boolean isImageNotContainGIF(String name) {
        boolean flag = false;
        try {
            name = name.toLowerCase();
            if (name.endsWith(".jpg") || name.endsWith(".png")
                    || name.endsWith(".jpeg") || name.endsWith(".bmp")) {
                flag = true;
            }
        } catch (Exception e) {
        }
        return flag;
    }

    /**
     * 过滤隐私空间路径
     * /remote.php/dav/secret/b9f5c90b8aac40b796327f6e6065f110/IMG_20210427_105247.jpg?key=920a9767cda4c286ea573e72650523ffc8891967
     *
     * @param path 移除隐私空间url带?key=xxx
     * @return
     */
    public static String filterPrivacySpace(String path) {
        if (!TextUtils.isEmpty(path)){
            if (path.contains("?key=")){
                int index = path.lastIndexOf("?key=");
                return path.substring(0, index);
            }
        }
        return path;
    }

    /**
     * 获取文件名
     *
     * @param path 文件路径
     * @return
     */
    public static String getFileName(String path) {
        String filename = null;
        if (!TextUtils.isEmpty(path)) {
            filename = path.substring(path.lastIndexOf("/") + 1, path.length());
        }
        return filename;
    }

    /**
     * 获取文件名，过滤后缀
     * @param fileName
     * @return
     */
    public static String getFileNameFilterExtension(String fileName){
        if (!TextUtils.isEmpty(fileName)) {
            int index = fileName.lastIndexOf(".");
            if (index > 0){
                fileName = fileName.substring(0,index);
            }
        }
        return fileName;
    }

    /**
     * @param extension 文件后缀(文件格式)
     * @return 文件格式对应的Mime类型(比如 ' jpg ' - > ' image / * ')
     */
    public static String getMimeTypeFromExtension(String extension) {
        String mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        return !TextUtils.isEmpty(mimeType) ? mimeType : "file/*";
    }

    /**
     * @param path
     * @return 文件后缀
     */
    public static String getFileExtensionFromPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            int index = path.lastIndexOf(".");
            if (index > 0){
                return path.substring(index+1);
            }
        }
        return path;
//        return path.substring(path.lastIndexOf(".") + 1);
    }

    public static int getContentResolverId(Context context, String path) {
        String[] proj = {MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATA,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Thumbnails.DATA};
        // 获取选中图片的路径  
        Cursor cursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, proj, MediaStore.Images.Media.DATA + "=? ", null, null);
        if (cursor == null) {
            return 0;
        }
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            cursor.close();
            return id;
        }
        cursor.close();
        return 0;
    }

    /**
     * Android 10获取图片
     */
    public static Bitmap getBitmapByQ(Context context, String id) {
        if (context == null) {
            return null;
        }
        try {
            //官方代码：
            Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, Long.parseLong(id));
            if (uri != null) {
                //通过流转化成bitmap对象
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                return BitmapFactory.decodeStream(inputStream);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] getByteArrayByQ(Context context, String id, String type) {
        if (context == null) {
            return null;
        }
        Uri contentUri = null;
        if (FILE_MINE_TYPE_IMAGE.equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_VIDEO.equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_AUDIO.equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        try {
            //官方代码：
            Uri uri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
            if (uri != null) {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                byte[] data = null;
                try {
                    if (inputStream.available() == 0) {//严谨起见,一定要加上这个判断,不要返回data[]长度为0的数组指针
                        return data;
                    }
                    data = new byte[inputStream.available()];
                    inputStream.read(data);
                    inputStream.close();
                    return data;
                } catch (IOException e) {
                    return data;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static InputStream getInputStreamByQ(Context context, String id, String type) {
        if (context == null) {
            return null;
        }
        Uri contentUri = null;
        if (FILE_MINE_TYPE_IMAGE.equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_VIDEO.equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_AUDIO.equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        try {
            //官方代码：
            Uri uri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
            if (uri != null) {
                InputStream inputStream = context.getContentResolver().openInputStream(uri);
                return inputStream;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static int getFileLength(String path) {
        File file = new File(path);
        return (int) file.length();
    }

    public static int getFileLengthByQ(Context context, String id, String type) {
        if (context == null) {
            return 0;
        }
        Uri contentUri = null;
        if (FILE_MINE_TYPE_IMAGE.equals(type)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_VIDEO.equals(type)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else if (FILE_MINE_TYPE_AUDIO.equals(type)) {
            contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        InputStream inputStream = null;
        try {
            //官方代码：
            Uri uri = ContentUris.withAppendedId(contentUri, Long.parseLong(id));
            if (uri != null) {
                inputStream = context.getContentResolver().openInputStream(uri);
                return inputStream.available();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    /**
     * Checks that the given {@code File} exists and is a directory.
     *
     * @param directory The {@code File} to check.
     * @throws IllegalArgumentException if the given {@code File} does not exist or is not a directory.
     */
    private static void checkDirectory(final File directory) {
        if (!directory.exists()) {
            throw new IllegalArgumentException(directory + " does not exist");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException(directory + " is not a directory");
        }
    }

    /**
     * Counts the size of a directory recursively (sum of the length of all files).
     * <p>
     * Note that overflow is not detected, and the return value may be negative if
     * overflow occurs. See {@link #(File)} for an alternative
     * method that does not overflow.
     * </p>
     *
     * @param directory directory to inspect, must not be {@code null}
     * @return size of directory in bytes, 0 if directory is security restricted, a negative number when the real total
     * is greater than {@link Long#MAX_VALUE}.
     * @throws NullPointerException if the directory is {@code null}
     */
    public static long sizeOfDirectory(final File directory) {
        checkDirectory(directory);
        return sizeOfDirectory0(directory);
    }

    public static long sizeOfDirPhotoVideo(final File directory ,final boolean isPhoto , final boolean isVideo) {
        checkDirectory(directory);
        return sizeOfPhotoOrVideo(directory,isPhoto,isVideo);
    }

    /**
     * the size of a director
     *
     * @param directory the directory to check
     * @return the size
     */
    private static long sizeOfDirectory0(final File directory) {
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        for (final File file : files) {
            if (file.exists()) {
                size += sizeOf0(file); // internal method
                if (size < 0) {
                    break;
                }
            }
        }

        return size;
    }

    private static long sizeOfPhotoOrVideo(final File directory ,final boolean isPhoto , final boolean isVideo) {
        final File[] files = directory.listFiles();
        if (files == null) {  // null if security restricted
            return 0L;
        }
        long size = 0;

        if(isPhoto && isVideo){
            for (final File file : files) {
                if (file.exists() && (isVideoType(file.getName()) || isImage(file.getName()))) {
                    size += sizeOf0(file); // internal method
                    if (size < 0) {
                        break;
                    }
                }
            }
        }else if(isPhoto){
            for (final File file : files) {
                if (file.exists() && isImage(file.getName())) {
                    size += sizeOf0(file); // internal method
                    if (size < 0) {
                        break;
                    }
                }
            }
        }else{
            for (final File file : files) {
                if (file.exists() && isVideoType(file.getName())) {
                    size += sizeOf0(file); // internal method
                    if (size < 0) {
                        break;
                    }
                }
            }
        }

        return size;
    }

    /**
     * the size of a file
     *
     * @param file the file to check
     * @return the size of the file
     */
    private static long sizeOf0(final File file) {
        if (file.isDirectory()) {
            return sizeOfDirectory0(file);
        }
        return file.length(); // will be 0 if file does not exist
    }

    /**
     * 刷新系统相册
     *
     * @param context
     * @param path
     * @param fileName
     */
    public static void refreshSystemAlbum(Context context, String path, String fileName) {
        if (context == null) {
            return;
        }
        // 把文件插入到系统图库
        File file = new File(path + fileName);
//        try {
//            MediaStore.Images.Media.insertImage(context.getContentResolver(),
//                    file.getAbsolutePath(), fileName, null);
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
        // 最后通知图库更新
        context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                Uri.fromFile(new File(file.getPath()))));
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }

                // TODO handle non-primary volumes
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[] {
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri The Uri to query.
     * @param selection (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    /**
     *是否过滤系统隐藏、不存在、不可读文件
     * @param file
     * @return
     */
    public static boolean isFilterFile(File file) {
        if (file.isHidden() || !file.exists() || !file.canRead()){//canRead() 5.18% exists() 4.86% isHidden() 1.17%
            return true;
        }
        //过滤正在写入的文件(如:录屏文件), 暂定为文件在2s内修改过则过滤.
        if(2000 > System.currentTimeMillis() - file.lastModified()){
            return true;
        }
        return false;
    }

    /**
     * 根据文件路径判断文件是否存在
     * @param filePath
     * @return
     */
    public static boolean fileExists(String filePath) {
        if(TextUtils.isEmpty(filePath)){
            return false;
        }
        File file = new File(filePath);
        if (file.exists() && file.isFile()) {
            return true;
        }
        return false;
    }
}
