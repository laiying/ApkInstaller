package com.strod.apkinstaller;

import android.content.res.Resources;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.strod.apkinstaller.model.LocalFile;
import com.strod.apkinstaller.utils.UriUtil;

/**
 * Created by laiying on 2022/2/22.
 */
public class MainViewHolder extends RecyclerView.ViewHolder {

    private ImageView icon;
    private TextView tvFileName;
    private TextView tvTimeSize;
    public MainViewHolder(LayoutInflater inflater, ViewGroup viewGroup) {
        super(inflater.inflate(R.layout.item_local_file, viewGroup, false));
        icon = itemView.findViewById(R.id.img_photo);
        tvFileName = itemView.findViewById(R.id.tv_file_name);
        tvTimeSize = itemView.findViewById(R.id.tv_time_and_size);
    }

    public void onBindData(LocalFile file, int position) {
        if (file.getImgPath().getScheme().endsWith(UriUtil.LOCAL_RESOURCE_SCHEME)){
            icon.setImageResource(Integer.parseInt(file.getImgPath().getPath().replace("/","")));
        }else {
            icon.setImageURI(file.getImgPath());
        }

        tvFileName.setText(file.getName());
        tvTimeSize.setText(file.getTimeAndSize());
    }
}
