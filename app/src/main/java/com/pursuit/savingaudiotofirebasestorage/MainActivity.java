package com.pursuit.savingaudiotofirebasestorage;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button recordButton, playbtn;
    private TextView recordTextView;
    private MediaRecorder recorder;
    private String fileName = null;
    private static final String LOG_TAG = "Record_Log";
    private StorageReference mStorageRef;
    private ProgressDialog progressDialog;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recordButton = findViewById(R.id.record_button);
        recordTextView = findViewById(R.id.record_text_view);
        playbtn = findViewById(R.id.play_button);

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadAudio();
            }
        });


        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/recorded_audio.3gp";
        //fileName += "/recorded_audio.mp3";
        logInToFireBase();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        progressDialog = new ProgressDialog(this);

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    startRecording();
                    recordTextView.setText("Recording Started ...");

                } else if (event.getAction() == MotionEvent.ACTION_UP) {
                    stopRecording();
                    recordTextView.setText("Recording Stopped ...");

                }
                return false;
            }
        });
    }

    private void logInToFireBase() {
        String email = "linda.pokorny@gmail.com";
        String password = "linda151";
        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            Log.d("pokorny", "onComplete: FireBase Auth Was Successful");
                        } else {
                            Log.d("pokorny", "onComplete: FireBase Auth failed.");
                        }
                    }
                });
    }

    private void startRecording() {
        recorder = new MediaRecorder();
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //.DEFAULT
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            recorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        recorder.start();
    }

    private void stopRecording() {
        recorder.stop();
        recorder.release();
        recorder = null;

        uploadAudio();
    }

    private void uploadAudio() {
        progressDialog.setMessage("Uploading Audio...");
        progressDialog.show();
        StorageReference filepath = mStorageRef.child("Audio").child("new_audio.3gp");
        Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                progressDialog.dismiss();
                recordTextView.setText("Uploading Finished");
            }
        });
    }

    private void downloadAudio(){
        final StorageReference filepath = mStorageRef.child("Audio").child("new_audio.3gp");
        try {
            filepath.getFile(Uri.fromFile(File.createTempFile("audio",".3gp"))).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    taskSnapshot.getBytesTransferred();
                    MediaPlayer player = new MediaPlayer();
                    try {
                        player.setDataSource();
                        player.prepare();
                    } catch (IOException e){
                        e.printStackTrace();
                    }
                    player.start();
                }
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
