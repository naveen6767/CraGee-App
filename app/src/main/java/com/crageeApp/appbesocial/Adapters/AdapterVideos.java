package com.crageeApp.appbesocial.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Models.ModelVideos;
import com.crageeApp.appbesocial.R;

import java.util.List;

public class AdapterVideos extends RecyclerView.Adapter<AdapterVideos.videoViewHolder> {

    List<ModelVideos> videosList;

    public AdapterVideos() {
    }

    public AdapterVideos(List<ModelVideos> videosList) {
        this.videosList = videosList;
    }

    @NonNull
    @Override
    public videoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View view=LayoutInflater.from(parent.getContext()).inflate(R.layout.videos_layout, parent, false);
        return new videoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull videoViewHolder holder, int position) {

        holder.videoView.loadData(videosList.get(position).getVideoUrl(),"text/html","utf-8");

    }

    @Override
    public int getItemCount() {
        return videosList.size();
    }

    public class videoViewHolder extends RecyclerView.ViewHolder{

            private WebView videoView;

            public videoViewHolder(@NonNull View itemView) {
                super(itemView);

                videoView=itemView.findViewById(R.id.videos_layout);
                videoView.getSettings().setJavaScriptEnabled(true);
                videoView.setWebChromeClient(new WebChromeClient());
            }
        }

}
