package com.strod.apkinstaller;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.strod.apkinstaller.model.LocalFile;

import java.util.List;

/**
 * Created by laiying on 2022/2/22.
 */
public class MainAdapter extends RecyclerView.Adapter<MainViewHolder>{

    public interface OnItemClickListener{
        void onItemClick(LocalFile localFile);
    }

    private Context mContext;
    private List<LocalFile> mLists;
    private OnItemClickListener onItemClickListener;

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public MainAdapter (Context context, List<LocalFile> list){
        this.mContext = context;
        this.mLists = list;
    }


    @NonNull
    @Override
    public MainViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new MainViewHolder(LayoutInflater.from(mContext), viewGroup);
    }

    @Override
    public void onBindViewHolder(@NonNull MainViewHolder viewHolder, int i) {
        viewHolder.onBindData(mLists.get(i), i);
        viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (onItemClickListener != null){
                    onItemClickListener.onItemClick(mLists.get(i));
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return mLists == null ? 0: mLists.size();
    }
}
