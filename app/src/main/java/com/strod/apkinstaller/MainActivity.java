package com.strod.apkinstaller;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import com.strod.apkinstaller.manager.FileManager;
import com.strod.apkinstaller.model.LocalFile;
import com.strod.apkinstaller.utils.CmdUtils;
import com.strod.apkinstaller.utils.FileUtils;
import com.strod.apkinstaller.utils.UriUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {


    private RecyclerView mRecyclerView;
    private MainAdapter mAdapter;
    private List<LocalFile> mLocalFiles = new ArrayList<>();

    private List<LocalFile> rootList = new ArrayList<>();

    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initView();
        requestPermission();
    }

    private void initView(){
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setCanceledOnTouchOutside(false);
        mRecyclerView = findViewById(R.id.recycler_view);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAdapter = new MainAdapter(this, mLocalFiles);
        mAdapter.setOnItemClickListener(new MainAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(LocalFile localFile) {
                String path = localFile.getPath();
                if (TextUtils.isEmpty(path)){
                    return;
                }
                File file = new File(path);
                if (!file.exists()){
                    return;
                }
                if (localFile.isDirectory()){
                    if (localFile.isBackRootDirectory()){
                        setListData();
                    }else {
                        reloadListData(file);
                    }
                }else {
                    //
                    if (path.endsWith(".apk")){
                        Log.d("apk", "path:"+localFile.getPath());
                        progressDialog.show();

                        Observable.create((ObservableOnSubscribe<CmdUtils.CommandResult>) emitter -> {
                            String installCommand = "";
                            CmdUtils.CommandResult cpResult = null;
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
//                            installCommand = " cat " + localFile.getPath() + " | pm install -S "+ localFile.getSize();
                                String tmpPath = "/data/local/tmp/";
                                String fileName = FileUtils.getFileName(localFile.getPath());
                                //拷贝文件
                                String cp= "cp "+localFile.getPath()+" "+(tmpPath + fileName);
                                cpResult = CmdUtils.execCommand(cp, true);
                                if (cpResult.result != 0){
                                    emitter.onNext(cpResult);
                                }else{
                                    installCommand = "pm install " + (tmpPath + fileName);
                                    CmdUtils.CommandResult result = CmdUtils.execCommand(installCommand, true);
                                    emitter.onNext(result);
                                }
                            }else{
                                installCommand = "pm install "+ localFile.getPath();
                                CmdUtils.CommandResult result = CmdUtils.execCommand(installCommand, true);
                                emitter.onNext(result);
                            }
                        }).subscribeOn(Schedulers.io())
                                .unsubscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(result -> {
                                    progressDialog.dismiss();
                                    if (result.result == 0){
                                        //安装成功
                                        Toast.makeText(MainActivity.this, "安装成功", Toast.LENGTH_LONG).show();
                                    }else{
                                        //安装失败
                                        Toast.makeText(MainActivity.this, "安装失败", Toast.LENGTH_LONG).show();
                                    }
                                });
                    }else{
                        Toast.makeText(MainActivity.this, "请选择apk文件", Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }


    protected static final int PERMISSIONS_REQUEST_CODE = 42;
    protected static final int PERMISSIONS_SDCARD_REQUEST_CODE = 43;

    private void requestPermission(){
        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            if (Environment.isExternalStorageManager()){
                //看是否需要继续6.0请求
                requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
            }else {
                final Context context = MainActivity.this;
                AlertDialog.Builder builder = new AlertDialog.Builder(context)
                        .setCancelable(false)
                        .setMessage("需要读取文件权限")
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
//                            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                Intent intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                                intent.setData(getPackageNameUri(context));
                                if (hasIntent(context, intent)){
                                    startActivityForResult(intent, PERMISSIONS_SDCARD_REQUEST_CODE);
                                }else {
                                    intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
                                    if (hasIntent(context, intent)){
                                        startActivityForResult(intent, PERMISSIONS_SDCARD_REQUEST_CODE);
                                    }else {
                                        try {
                                            intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                                            intent.setData(getPackageNameUri(context));
                                            startActivityForResult(intent, PERMISSIONS_SDCARD_REQUEST_CODE);
                                        } catch (ActivityNotFoundException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        });
                builder.show();
            }
        }else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            requestPermissions(permissions, PERMISSIONS_REQUEST_CODE);
        }else{
            setListData();
        }
    }

    private boolean hasIntent(Context context, Intent intent){
        PackageManager pm = context.getPackageManager();

        List<ResolveInfo> list = pm.queryIntentActivities(intent, 0);
        final int size = (list == null) ? 0 : list.size();
        return size > 0;
    }

    /**
     * 获取包名 Uri 对象
     */
    private static Uri getPackageNameUri(Context context) {
        return Uri.parse("package:" + context.getPackageName());
    }

    @SuppressLint("CheckResult")
    public void setListData() {
        progressDialog.show();
        Observable.create((ObservableOnSubscribe<List<LocalFile>>) emitter -> {
            List<LocalFile> list = FileManager.getInstance().getStorageDirectories(MainActivity.this);
            rootList.clear();
            rootList.addAll(list);
            emitter.onNext(list);
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mLocalFiles.clear();
                    mLocalFiles.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                });
    }

    public void reloadListData(File file){
        progressDialog.show();
        Observable.create(new ObservableOnSubscribe<List<LocalFile>>() {
            @Override
            public void subscribe(ObservableEmitter<List<LocalFile>> emitter) throws Exception {
                List<LocalFile> list = FileManager.getInstance().getFiles(file);
                emitter.onNext(list);
            }
        }).subscribeOn(Schedulers.io())
                .unsubscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(list -> {
                    mLocalFiles.clear();
                    LocalFile backFileBean = createBackLastDirectory(file, isRootDirectory(file));
                    mLocalFiles.add(backFileBean);
                    mLocalFiles.addAll(list);
                    mAdapter.notifyDataSetChanged();
                    progressDialog.dismiss();
                });
    }

    private boolean isRootDirectory(File file){
        if (rootList != null){
            for (LocalFile f : rootList){
                if (f.getPath().equals(file.getPath())){
                    return true;
                }
            }
        }
        return false;
    }

    private String getRootDirectoryName(File file){
        if (rootList != null){
            for (LocalFile f : rootList){
                if (f.getPath().equals(file.getPath())){
                    return f.getName();
                }
            }
        }
        return "";
    }

    private LocalFile createBackLastDirectory(File file, boolean isRoot){
        LocalFile localFileBean = new LocalFile();
        localFileBean.setPath(file.getParent());
        localFileBean.setName("返回上一级");
        localFileBean.setImgPath(UriUtil.getUriForResourceId(R.mipmap.ic_file_dirs));
        localFileBean.setDirectory(true);
        localFileBean.setBackVirtual(true);
        localFileBean.setLastDirectoryName(isRootDirectory(file.getParentFile()) ?  getRootDirectoryName(file.getParentFile()) : file.getParentFile().getName());
        if (isRoot){
            localFileBean.setBackRootDirectory(true);
        }
        return localFileBean;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {

        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            boolean allGranted = false;
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //已授权
                    allGranted = true;
                } else {
                    //未授权
                    allGranted = false;
                }
            }
            if (allGranted){
                setListData();
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (PERMISSIONS_SDCARD_REQUEST_CODE == requestCode){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
                if (Environment.isExternalStorageManager()){
                    //看是否需要继续6.0请求
                    requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSIONS_REQUEST_CODE);
                }
            }

        }
    }
}