package com.example.yun.sred;

import android.content.Intent;
import android.graphics.Color;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.ColorInt;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.beardedhen.androidbootstrap.BootstrapButton;
import com.beardedhen.androidbootstrap.TypefaceProvider;

import java.util.ArrayList;
import java.util.Locale;

public class MainActivity extends AppCompatActivity {

    private Intent i;
    private TextView tv;
    private TextToSpeech tts;
    private BootstrapButton stt_butt, aiSpeaker_butt;
    private boolean aispeaker=false;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TypefaceProvider.registerDefaultIconSets();

        setContentView(R.layout.activity_main);

        tv = findViewById(R.id.STT_text);
        stt_butt = findViewById(R.id.STT_button);
        aiSpeaker_butt = findViewById(R.id.aiSpeaker);
        i = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        i.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        i.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        aiSpeaker_butt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(aispeaker == false) {
                    aispeaker = true;
                    aiSpeaker_butt.setText("ON");

                }
                else if(aispeaker==true) {
                    aispeaker = false;
                    aiSpeaker_butt.setText("OFF");
                }
            }
        });

        tts = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                tts.setLanguage(Locale.KOREAN);
            }
        });
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

                tts.setPitch(1.0f);
                if(aispeaker == true)
                tts.speak("클로바 "+tv.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
                else
                    tts.speak(tv.getText().toString(),TextToSpeech.QUEUE_FLUSH,null);
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
