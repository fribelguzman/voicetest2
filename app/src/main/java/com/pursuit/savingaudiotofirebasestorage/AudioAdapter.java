package com.pursuit.savingaudiotofirebasestorage;

import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;

import static android.os.Environment.DIRECTORY_DOWNLOADS;

public class AudioAdapter /*extends RecyclerView.Adapter<AudioViewHolder>*/ {

/*    ArrayList<AudioModel> audioList;

    public AudioAdapter(ArrayList<AudioModel> audioList) {
        this.audioList = audioList;
    }

    @NonNull
    @Override
    public AudioViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item_view, viewGroup, false);
        return new AudioViewHolder(view);

    }

    @Override
    public void onBindViewHolder(@NonNull AudioViewHolder audioViewHolder, int i) {

        AudioViewHolder.mName.setText(downModels.get(i).getName());
        AudioViewHolder.mLink.setText(downModels.get(i).getAudioFile());

        audioViewHolder.playDownload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadFile(audioViewHolder.mName.getContext(), audioModel.get(i).getName(), ".3gp", DIRECTORY_DOWNLOADS, audioModel.get(i).getLink());
            }
        });


    }

    public void downloadFile(Context context, String fileName, String fileExtension, String destinationDirectory, String url) {

        DownloadManager downloadmanager = (DownloadManager) context.
                getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(url);
        DownloadManager.Request request = new DownloadManager.Request(uri);

        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setDestinationInExternalFilesDir(context, destinationDirectory, fileName + fileExtension);

        downloadmanager.enqueue(request);
    }


    @Override
    public int getItemCount() {
        return 0;
    }*/
}