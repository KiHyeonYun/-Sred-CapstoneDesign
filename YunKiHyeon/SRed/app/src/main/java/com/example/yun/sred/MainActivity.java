package com.example.yun.sred;

import android.content.Intent;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private Intent i;
    private TextView tv;
    private BootstrapButton stt_butt;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();

        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.STT_text);
        stt_butt = findViewById(R.id.STT_button);

        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");



        RecognitionListener listener = new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {

            }

            @Override
            public void onBeginningOfSpeech() {

            }

            @Override
            public void onRmsChanged(float rmsdB) {

            }

            @Override
            public void onBufferReceived(byte[] buffer) {

            }

            @Override
            public void onEndOfSpeech() {

            }

            @Override
            public void onError(int error) {

            }

            @Override
            public void onResults(Bundle results) {
                String key = "";
                key = SpeechRecognizer.RESULTS_RECOGNITION;
                ArrayList<String> mResult = results.getStringArrayList(key);
                String[] rs = new String[mResult.size()];
                mResult.toArray(rs); tv.setText(""+rs[0]);

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

        stt_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecognizer.startListening(i);
            }
        });
    }
}
