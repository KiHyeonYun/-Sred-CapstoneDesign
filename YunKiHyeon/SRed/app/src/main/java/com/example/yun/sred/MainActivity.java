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
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;
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

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "VoiceChangerSample";

    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private MicRecordTask recordTask;
    private AlertDialog saveDialog;
    private WaveDisplayView displayView;
    private ProgressBar progressBar;

    private static final int VOICE = 1;
    private Intent i;
    private TextView tv;
    private TextToSpeech tts;
    private BootstrapButton stop_butt, record_butt, stt_butt, aiSpeaker_butt;
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

        tv = findViewById(R.id.STT_text);
        //  stt_butt = findViewById(R.id.STT_button);
        record_butt = findViewById(R.id.Record_button);
        stop_butt = findViewById(R.id.stop_butt);

        progressBar = (ProgressBar) findViewById(R.id.progressBarMain);
        displayView = new WaveDisplayView(getBaseContext());

        //   aiSpeaker_butt = findViewById(R.id.aiSpeaker);
      //  stt_butt.setVisibility(View.INVISIBLE);

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");
        i.putExtra("android.speech.extra.GET_AUDIO_FORMAT", "audio/AMR");
        i.putExtra("android.speech.extra.GET_AUDIO", true);

//        aiSpeaker_butt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (aispeaker == false) {
//                    aispeaker = true;
//                    aiSpeaker_butt.setText("ON");
//
//                } else if (aispeaker == true) {
//                    aispeaker = false;
//                    aiSpeaker_butt.setText("OFF");
//                }
//            }
//        });

//        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
//            @Override
//            public void onInit(int status) {
//                tts.setLanguage(Locale.KOREAN);
//            }
//        });
        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                tv.setText("\"듣고있습니다\"");
                stt_butt.setVisibility(View.VISIBLE);
            }

            @Override
            public void onBeginningOfSpeech() {
                stt_butt.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

                stt_butt.setVisibility(View.VISIBLE);
                FirebaseDatabase.getInstance().getReference().child("users").child(user.getUid()).child("result")
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                result = dataSnapshot.getValue();
                                mdatabase.child("users").child(user.getUid()).child("result").setValue("NULL");
                                tv.setText(result.toString());

                                tts.setPitch(1.0f);
                                if (aispeaker == true)
                                    //tts.speak("클로바 "+tv.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                                    tts.speak("클로바 " + result.toString(), TextToSpeech.QUEUE_FLUSH, null);
                                else
                                    tts.speak(tv.getText().toString(), TextToSpeech.QUEUE_FLUSH, null);
                                tts.speak(result.toString(), TextToSpeech.QUEUE_FLUSH, null);

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                            }
                        });

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                startActivityForResult(i, VOICE);
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = results.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                //mResult.toArray(rs); tv.setText(""+rs[0]);

            }

            @Override
            public void onPartialResults(Bundle partialResults) {

            }

            @Override
            public void onEvent(int eventType, Bundle params) {

            }
        };

        final SpeechRecognizer mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        configureEvnetListener();
        setInitializeState();
    }
//        stt_butt.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                mRecognizer.startListening(i);
//            }
//        });
//    }
//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//
//        ArrayList<String> speechList = data.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
//        String result = speechList.get(0);
//        Toast.makeText(MainActivity.this, result, Toast.LENGTH_LONG).show();
//
//        Uri audioUri = data.getData();
//
//        ContentResolver contentResolver = getContentResolver();
//        InputStream inputStream = null;
//        OutputStream outputStream = null;
//
//        try {
//            inputStream = contentResolver.openInputStream(audioUri);
//            outputStream = null;
//
//            File targetFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath()+"/test");
//            if(!targetFile.exists()){
//                targetFile.mkdirs();
//            }
//            String fileName = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date()) + ".amr";
//            outputStream = new FileOutputStream(targetFile + "/" + fileName);
//
//            int read = 0;
//            byte[] bytes = new byte[1024];
//
//            while ((read = inputStream.read(bytes)) != -1) {
//                outputStream.write(bytes, 0, read);
//            }
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } finally {
//            if (inputStream != null) {
//                try {
//                    inputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            if (outputStream != null) {
//                try {
//                    // outputStream.flush();
//                    outputStream.close();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//        }
//    }


        /////////////////////////
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
            if (stop_butt.isEnabled()) {
                stopAll();
            }
            super.onPause();
        }
        private void configureEvnetListener () {
            record_butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startRecording();
                }
            });

            stop_butt.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    stopAll();
                }
            });

        }
        private void setInitializeState () {
            record_butt.setEnabled(true);
            stop_butt.setEnabled(false);
            //      saveButton.setEnabled(true);
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
                recordTask.setMax(10 * getDataBytesPerSecond(SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING));
            } catch (IllegalArgumentException ex) {
                Log.w(TAG, "Fail to create MicRecordTask.", ex);
            }
            recordTask.start();
            waitEndTask(recordTask);
        }

        private void stopRecording () {
            stopTask(recordTask);

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
            record_butt.setEnabled(!b);
            stop_butt.setEnabled(b);
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
                            setButtonEnable(false);
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
