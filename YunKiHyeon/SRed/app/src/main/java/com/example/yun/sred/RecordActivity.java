package com.example.yun.sred;

import android.Manifest;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioFormat;
import android.os.Environment;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.Toast;

import com.example.yun.sred.audio.AudioPlayTask;
import com.example.yun.sred.audio.MicRecordTask;
import com.example.yun.sred.audio.NormalizeWaveData;
import com.example.yun.sred.audio.StopableTask;
import com.example.yun.sred.audio.WaveDisplayView;
import com.example.yun.sred.audio.WaveFileHeaderCreator;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class RecordActivity extends AppCompatActivity {


    String[] PERMISSION = {Manifest.permission.READ_SMS, Manifest.permission.RECEIVE_SMS};
    private static final String TAG = "VoiceChangerSample";

    private static final int SAMPLE_RATE = 8000;
    private static final int CHANNEL_CONFIG = AudioFormat.CHANNEL_CONFIGURATION_MONO;
    private static final int AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;

    private MicRecordTask recordTask;
    private AudioPlayTask playTask;
    private AlertDialog saveDialog;

    private WaveDisplayView displayView;
    private ProgressBar progressBar;
    private Button recordButton;
    private Button playButton;
    private Button stopButton;
    private Button saveButton;

    private FirebaseAuth FirebaseAuth;
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private DatabaseReference mdatabase = FirebaseDatabase.getInstance().getReference();
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        Log.d(TAG, "Start.");
        LinearLayout displayLayout = (LinearLayout) findViewById(R.id.displayView);
        displayView = new WaveDisplayView(getBaseContext());
        displayLayout.addView(displayView);

        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        recordButton = (Button) findViewById(R.id.Record);
        playButton = (Button) findViewById(R.id.Play);
        stopButton = (Button) findViewById(R.id.Stop);
        saveButton = (Button) findViewById(R.id.Save);

        configureEvnetListener();
        setInitializeState();
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

    @Override
    protected void onPause() {
        if (stopButton.isEnabled()) {
            stopAll();
        }
        super.onPause();
    }
    private void configureEvnetListener() {
        recordButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startRecording();
            }
        });

        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startPlaying();
            }
        });

        stopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopAll();
            }
        });

        saveDialog = createSaveDialog();
        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveDialog.show();
            }
        });
    }
    private void setInitializeState() {
        recordButton.setEnabled(true);
        playButton.setEnabled(true);
        stopButton.setEnabled(false);
        saveButton.setEnabled(true);
    }

    private AlertDialog createSaveDialog() {
        final Handler handler = new Handler();
        final View view = LayoutInflater.from(this).inflate(R.layout.save_dialog, null);
        return new AlertDialog.Builder(this)
                .setTitle(R.string.dialog_save_title)
                .setView(view)
                .setPositiveButton(R.string.dialog_save_button_save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        EditText filename = (EditText) view.findViewById(R.id.filenameEditText);
                        RadioButton wavRadio = (RadioButton) view.findViewById(R.id.wavRadio);

                        boolean isWavFile = wavRadio.isChecked();
                        final File file = new File(getSavePath(), filename.getText() + (isWavFile ? ".wav" : ".raw"));
                        saveSoundFile(file, isWavFile);

                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(RecordActivity.this, "Save completed: " + file.getAbsolutePath(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.dialog_save_button_cancel, null)
                .create();
    }

    private boolean saveSoundFile(File savefile, boolean isWavFile) {

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
            return true;
        } catch (IOException ex) {
            Log.w(TAG, "Fail to save sound file.", ex);
            return false;
        }
    }


    private void startRecording() {
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

    private void stopRecording() {
        stopTask(recordTask);
        FirebaseDatabase.getInstance()
                .getReference()
                .child("users")
                .child(user.getUid())
                .child("recordNumber")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Object recordNumber = dataSnapshot.getValue();

                        Toast.makeText(RecordActivity.this,recordNumber.toString(),Toast.LENGTH_LONG).show();
                        final File file = new File(getSavePath(), "0"+ recordNumber.toString()+ ".wav");
                            saveSoundFile(file, true);

                        recordNumber = Integer.parseInt(recordNumber.toString())+1;
                        mdatabase.child("users").child(user.getUid()).child("recordNumber").setValue(recordNumber);

                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e(TAG, "ERROR DataBase");
                    }
                });

        Log.i(TAG, "stop recording.");
    }

    private void startPlaying() {
        Log.i(TAG, "start playing.");

        setButtonEnable(true);
        try {
            playTask = new AudioPlayTask(progressBar, displayView, SAMPLE_RATE, CHANNEL_CONFIG, AUDIO_ENCODING);
        } catch (IllegalArgumentException ex) {
            Log.w(TAG, "Fail to create MicRecordTask.", ex);
        }
        playTask.start();
        waitEndTask(playTask);
    }

    private void stopPlaying() {
        stopTask(playTask);
        Log.i(TAG, "stop playing.");
    }

    private void stopTask(StopableTask task) {
        if (task.stopTask()) {
            try {
                task.join(1000);
            } catch (InterruptedException e) {
                Log.w(TAG, "Interrupted recoring thread stopping.");
            }
        }
        setButtonEnable(false);
    }


    private void stopAll() {
        if (recordTask != null && recordTask.isRunning()) {
            stopRecording();
        }
        if (playTask != null && playTask.isRunning()) {
            stopPlaying();
        }
    }

    private void setButtonEnable(boolean b) {
        recordButton.setEnabled(!b);
        playButton.setEnabled(!b);
        stopButton.setEnabled(b);
        saveButton.setEnabled(!b && hasSDCard());
    }


    private void waitEndTask(final Thread t) {
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


    private File getCacheFile() {
        return new File(getSavePath(), "cache.raw");
    }

    private File getSavePath() {
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

    private boolean hasSDCard() {
        String state = Environment.getExternalStorageState();
        return state.equals(Environment.MEDIA_MOUNTED);
    }


    private int getDataBytesPerSecond(int sampleRate, int channelConfig, int audioEncoding) {
        boolean is8bit = audioEncoding == AudioFormat.ENCODING_PCM_8BIT;
        boolean isMonoChannel = channelConfig != AudioFormat.CHANNEL_CONFIGURATION_STEREO;
        return sampleRate * (isMonoChannel ? 1: 2) * (is8bit ? 1: 2);
    }
}

