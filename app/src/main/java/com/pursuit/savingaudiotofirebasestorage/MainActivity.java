package com.pursuit.savingaudiotofirebasestorage;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StreamDownloadTask;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button recordButton, playbtn;
    private TextView recordTextView;
    private MediaRecorder recorder;
    private String fileName = null;
    private static final String LOG_TAG = "Record_Log";
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef;
    private MediaPlayer mMediaplayer;
    private ProgressDialog progressDialog;
    private MainActivity mainActivity;
    FirebaseStorage storage = FirebaseStorage.getInstance("gs://voicetest-7fe76.appspot.com");
    // Storage Permissions
    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainActivity = this;
        setContentView(R.layout.activity_main);
        mMediaplayer = new MediaPlayer();
        mMediaplayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
        recordButton = findViewById(R.id.record_button);
        recordTextView = findViewById(R.id.record_text_view);
        playbtn = findViewById(R.id.play_button);

        playbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                fetchAudioUrlFromFirebase();
            }
        });


        fileName = Environment.getExternalStorageDirectory().getAbsolutePath();
        fileName += "/recorded_audio.3gp";
        //fileName += "/recorded_audio.mp3";

        logInToFireBase();

        mStorageRef = storage.getReference();
        progressDialog = new ProgressDialog(this);

        recordButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {

                        ActivityCompat.requestPermissions(mainActivity, new String[]{Manifest.permission.RECORD_AUDIO},
                                2);

                    } else {
                        startRecording();
                    }
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
        recorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
            @Override
            public void onError(MediaRecorder mediaRecorder, int i, int i1) {
                Log.d("pokorny", String.valueOf(i));
            }
        });
        recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        recorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP); //.DEFAULT
        recorder.setOutputFile(fileName);
        recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        int permission = ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        } else {
            try {
                recorder.prepare();
                recorder.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }
        }


    }

    private void stopRecording() {

        try {
            recorder.stop();
            recorder.release();
            recorder = null;

            uploadAudio();
        } catch (RuntimeException stopException) {
            Log.e(LOG_TAG, "stop failed");
        }

    }

    private void uploadAudio() {
        progressDialog.setMessage("Uploading Audio...");
        progressDialog.show();
        StorageReference filepath = mStorageRef.child("Audio").child("new_audio.3gp");
        final Uri uri = Uri.fromFile(new File(fileName));
        filepath.putFile(uri).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                progressDialog.dismiss();
                recordTextView.setText("Uploading Finished");

            }
        });
        filepath.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {


            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                AudioModel audioModel = new AudioModel(uri.toString());
                db.collection("fribel").document("uploads").collection("audiolist")
                        .add(audioModel)
                        .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                            @Override
                            public void onSuccess(DocumentReference documentReference) {
                                Log.d("test", "DocumentSnapshot added with ID: " + documentReference.getId());
                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.w("test", "Error adding document", e);
                            }
                        });
                ;


                progressDialog.dismiss();
                recordTextView.setText("Uploading Finished");

            }
        });
    }


    private void fetchAudioUrlFromFirebase() {
        final FirebaseStorage storage = FirebaseStorage.getInstance();
        // Create a storage reference from our app
        StorageReference storageRef = mStorageRef.child("Audio").child("new_audio.3gp");
        storageRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                try {
                    // Download url of file
                    final String url = uri.toString();
                    mMediaplayer.setVolume(1, 1);
                    mMediaplayer.setDataSource(getApplicationContext(), uri);
                    // wait for media player to get prepare
                    mMediaplayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mediaPlayer) {
                            mediaPlayer.start();
                        }
                    });
                    mMediaplayer.prepareAsync();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.i("TAG", e.getMessage());
                    }
                });

    }

}