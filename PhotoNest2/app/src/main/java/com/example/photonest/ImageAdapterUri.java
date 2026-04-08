package com.example.photonest;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.documentfile.provider.DocumentFile;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ImageAdapterUri extends RecyclerView.Adapter<ImageAdapterUri.VH> {

    private List<DocumentFile> files;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onClick(DocumentFile file);
    }

    public ImageAdapterUri(List<DocumentFile> files, OnItemClickListener listener) {
        this.files = files;
        this.listener = listener;
    }

    public static class VH extends RecyclerView.ViewHolder {
        ImageView iv;

        public VH(View itemView) {
            super(itemView);
            iv = itemView.findViewById(R.id.ivThumb);
        }
    }

    @Override
    public VH onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_image, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(VH holder, int position) {

        DocumentFile file = files.get(position);

        Glide.with(holder.iv.getContext())
                .load(file.getUri())
                .centerCrop()
                .into(holder.iv);

        holder.itemView.setOnClickListener(v -> listener.onClick(file));
    }

    @Override
    public int getItemCount() {
        return files.size();
    }
}