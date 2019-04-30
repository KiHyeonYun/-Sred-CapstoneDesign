package com.example.yun.sred;

import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Color;
import android.media.AudioFormat;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
import com.bumptech.glide.Glide;
import com.example.yun.sred.audio.AudioPlayTask;
import com.example.yun.sred.audio.MicRecordTask;
import com.example.yun.sred.audio.NormalizeWaveData;
import com.example.yun.sred.audio.StopableTask;
import com.example.yun.sred.audio.WaveDisplayView;
import com.example.yun.sred.audio.WaveFileHeaderCreator;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class MainActivity extends BaseDialog {
    private static final String TAG = "VoiceChangerSample";

    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private boolean toggleView =true;
    private MicRecordTask recordTask;
    private AlertDialog saveDialog;
    private WaveDisplayView displayView;
    private ProgressBar progressBar;

    private static final int VOICE = 1;
    private Intent i;
    private TextToSpeech tts;
    private TextView ttsText;
    private ImageView recordImageView;
    private boolean aispeaker = false;
    private com.google.firebase.auth.FirebaseAuth FirebaseAuth;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();
    private Object result;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();

        setContentView(R.layout.activity_main);

        recordImageView = findViewById(R.id.recordImageView);
        progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
        displayView = new WaveDisplayView(getBaseContext());

        recordImageView.setImageResource(R.drawable.record_main);


        ttsText = findViewById(R.id.STT_text);
        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });

        FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("result")
                .addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        result = dataSnapshot.getValue();
                        tts.setPitch(1.0f);

                        if (!result.toString().equals(" ")) {
                            ttsText.setText(result.toString());
                            tts.speak(result.toString(), TextToSpeech.QUEUE_FLUSH, null);
                            recordImageView.setImageResource(R.drawable.record_main);
                        }
                    }


                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                    }
                });



        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        i.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        i.putExtra("android.speech.extra.GET_AUDIO", true);

        recordImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(toggleView == true) {
                    Glide.with(MainActivity.this).load(R.raw.listening).into(recordImageView);
                    startRecording();
                }
                else {
                    Glide.with(MainActivity.this).load(R.raw.error).into(recordImageView);
                    stopRecording();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean ret = super.onCreateOptionsMenu(menu);
        int index = Menu.FIRST;
        menu.add(Menu.NONE, index++, Menu.NONE, "1");
        menu.add(Menu.NONE, index++, Menu.NONE, "2");
        menu.add(Menu.NONE, index++, Menu.NONE, "3");
        menu.add(Menu.NONE, index++, Menu.NONE, "4");
        return ret;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        final int size = 8000;
        final int freq = 440;
        switch (item.getItemId()) {
            case Menu.FIRST:
                displayView.clearWaveData();
                break;
            case Menu.FIRST + 1:
                displayView.addWaveData(NormalizeWaveData.createNoiseData(size));
                break;
            case Menu.FIRST + 2:
                displayView.addWaveData(NormalizeWaveData.createSineData(size, freq));
                break;
            case Menu.FIRST + 3:
                displayView.addWaveData(NormalizeWaveData.createSquareData(size, freq));
                break;
        }
        return true;
    }
    protected void onPause () {
        if (toggleView==true) {
            stopAll();
        }
        super.onPause();
    }

    private boolean saveSoundFile (File savefile,boolean isWavFile){

        Uri file;
        StorageReference wavRef;
        UploadTask uploadTask;
        byte[] data = displayView.getAllWaveData();
        if (data.length == 0) {
            Log.w(TAG, "save data is not found.");
            return false;
        }

        try {
            Glide.with(MainActivity.this).load(R.raw.processing).into(recordImageView);
            savefile.createNewFile();
            FileOutputStream targetStream = new FileOutputStream(savefile);
            try {
                if (isWavFile) {
                    WaveFileHeaderCreator.pushWaveHeader(targetStream, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING, data.length);
                }
                targetStream.write(data);


            } finally {
                if (targetStream != null) {
                    targetStream.close();
                }
            }
            file = Uri.fromFile(new File(getSavePath() + "/using" + ".wav"));
            wavRef = storageRef.child(user.getUid() + "/using/" + file.getLastPathSegment());
            uploadTask = wavRef.putFile(file);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    FirebaseDatabase.getInstance()
                            .getReference()
                            .child("users")
                            .child(user.getUid())
                            .child("using").setValue("true");
                    mdatabase.child("users").child(user.getUid()).child("result").setValue(" ");
                    Toast.makeText(MainActivity.this, "FileUpload Success", Toast.LENGTH_SHORT).show();
                }
            });
            return true;
        } catch (IOException ex) {
            Log.w(TAG, "Fail to save sound file.", ex);
            return false;
        }

    }


    private void startRecording () {
        Log.i(TAG, "start recording.");
        setButtonEnable(true);
        try {
            recordTask = new MicRecordTask(progressBar, displayView, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
            recordTask.setMax(3 * getDataBytesPerSecond(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING));
        } catch (IllegalArgumentException ex) {
            Glide.with(MainActivity.this).load(R.raw.error).into(recordImageView);
            Log.w(TAG, "Fail to create MicRecordTask.", ex);
        }
        recordTask.start();
        waitEndTask(recordTask);
    }
    private void stopRecording () {
        Glide.with(MainActivity.this).load(R.raw.processing).into(recordImageView);
        stopTask(recordTask);
        ttsText.setText(" ");

        final File file = new File(getSavePath(), "using" + ".wav");
        saveSoundFile(file, true);

        Log.i(TAG, "stop recording.");
    }
    private void stopTask (StopableTask task){
        if (task.stopTask()) {
            try {
                task.join(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted recoring thread stopping.");
            }
        }
        setButtonEnable(false);
    }


    private void stopAll () {
        if (recordTask != null && recordTask.isRunning()) {
            stopRecording();
        }
    }

    private void setButtonEnable ( boolean b){
        recordImageView.setEnabled(!b);
//            stop_butt.setEnabled(b);
    }


    private void waitEndTask ( final Thread t){
        final Handler handler = new Handler();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    t.join();
                } catch (InterruptedException e) {
                }

                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        //     Thread.sleep(3000);
                        setButtonEnable(false);
                        stopRecording();

                    }
                });
            }
        }).start();
    }
    private File getCacheFile () {
        return new File(getSavePath(), "cache.raw");
    }
    private File getSavePath () {
        if (hasSDCard()) {

            File path = new File(Environment.getExternalStorageDirectory(), "download/VoiceChanger/");
            //File path = new File(Environment.getExternalStorageDirectory(), "download/VoiceChanger/");
            path.mkdirs();
            return path;
        } else {
            Log.i(TAG, "SDCard is unuseable: " + Environment.getExternalStorageState());
            return getFilesDir();
        }
    }
    private boolean hasSDCard () {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }
    private int getDataBytesPerSecond ( int sampleRate, int channelConfig, int audioEncoding){
        boolean is8bit = audioEncoding == AudioFormat.ENCODING_PCM_8BIT;
        boolean isMonoChannel = channelConfig != AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        return sampleRate * (isMonoChannel ? 1 : 2) * (is8bit ? 1 : 2);
    }
}