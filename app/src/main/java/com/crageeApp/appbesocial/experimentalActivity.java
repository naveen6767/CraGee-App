package com.crageeApp.appbesocial;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.crageeApp.appbesocial.Adapters.AdapterVideos;
import com.crageeApp.appbesocial.Models.ModelVideos;

import java.util.Vector;

public class experimentalActivity extends AppCompatActivity {


     RecyclerView recyclerView;
     Vector<ModelVideos> videos=new Vector<ModelVideos>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_experimental);

           recyclerView=findViewById(R.id.recyclerview_videos);
           recyclerView.setHasFixedSize(true);
           recyclerView.setLayoutManager(new LinearLayoutManager(this));
           videos.add(new ModelVideos());
           videos.add(new ModelVideos("https://youtu.be/A4JjrRhLX7Y"));
           videos.add(new ModelVideos("https://youtu.be/BOF_peuhQ60"));
           videos.add(new ModelVideos("https://youtu.be/BAt8jzYaNxg"));
           videos.add(new ModelVideos("https://youtu.be/BAt8jzYaNxg"));
        AdapterVideos adapterVideos=new AdapterVideos(videos);
        recyclerView.setAdapter(adapterVideos);


    }
}