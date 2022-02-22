package com.strod.apkinstaller.model;

import android.net.Uri;

import com.strod.apkinstaller.utils.DateUtils;
import com.strod.apkinstaller.utils.StringUtils;

/**
 * Created by laiying on 2022/2/22.
 */
public class LocalFile {
    private int id;
    private String name;
    private String path;
    private long time;
    private long size;
    private boolean isImage;
    private Uri imgPath;
    private boolean isDirectory;//是否是文件夹
    private boolean isBackRootDirectory;//是否返回根目录
    private boolean isBackVirtual;//是否是返回上一级按钮
    private String lastDirectoryName;//上一级名称

    private boolean isSelected;

    public LocalFile() {
    }

    public LocalFile(String name, String path, long time, long size) {
        this.name = name;
        this.path = path;
        this.time = time;
        this.size = size;
    }
    public LocalFile(String name, String path, Uri imgPath) {
        this.name = name;
        this.path = path;
        this.imgPath = imgPath;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public boolean isImage() {
        return isImage;
    }

    public void setImage(boolean image) {
        isImage = image;
    }

    public Uri getImgPath() {
        return imgPath;
    }

    public void setImgPath(Uri imgPath) {
        this.imgPath = imgPath;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectory(boolean directory) {
        isDirectory = directory;
    }

    public boolean isBackRootDirectory() {
        return isBackRootDirectory;
    }

    public void setBackRootDirectory(boolean backRootDirectory) {
        isBackRootDirectory = backRootDirectory;
    }

    public boolean isBackVirtual() {
        return isBackVirtual;
    }

    public void setBackVirtual(boolean backVirtual) {
        isBackVirtual = backVirtual;
    }

    public String getLastDirectoryName() {
        return lastDirectoryName;
    }

    public void setLastDirectoryName(String lastDirectoryName) {
        this.lastDirectoryName = lastDirectoryName;
    }

    public String getTimeAndSize() {
        if (time == 0 && size ==0){
            return "";
        }
        return DateUtils.formatTime(time, DateUtils.YYYY_MM_DD_HH_MM_SS)
                + " " + StringUtils.formatFileSize(size);
    }

}
