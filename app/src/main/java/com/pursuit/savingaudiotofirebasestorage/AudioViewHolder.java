package com.pursuit.savingaudiotofirebasestorage;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

class AudioViewHolder extends RecyclerView.ViewHolder{

    TextView mName;
    Button playDownload;

    public AudioViewHolder(@NonNull View itemView) {
        super(itemView);

        mName = itemView.findViewById(R.id.name);

        playDownload = itemView.findViewById(R.id.play_button);

    }
}

