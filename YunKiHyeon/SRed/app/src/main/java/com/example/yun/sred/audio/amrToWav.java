package com.example.yun.sred.audio;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class amrToWav implements Runnable  {

     private final int RECORDER_AUDIO_ENCODING = AudioFormat.ENCODING_PCM_16BIT;
        private final int RECORDER_CHANNELS = AudioFormat.CHANNEL_CONFIGURATION_MONO;  //안드로이드 녹음시 채널 상수값
        private final int WAVE_CHANNEL_MONO = 1;                                           //wav 파일 헤더 생성시 채널 상수값
        private final int HEADER_SIZE = 0x2c;
        private final int RECORDER_BPP = 16;
        private final int RECORDER_SAMPLERATE = 0xac44;
        private final int BUFFER_SIZE;
        private final String TEMP_FILE_NAME = "test_temp.bak";

        private AudioRecord mAudioRecord;
        private boolean mIsRecording;
        private String mFileName;
        private BufferedInputStream mBIStream;
        private BufferedOutputStream mBOStream;
        private int mAudioLen = 0;

        public amrToWav(String path, String fileName) {
            super();
            //mPath = path;
            mFileName = fileName;
            BUFFER_SIZE = AudioRecord.getMinBufferSize(RECORDER_SAMPLERATE, RECORDER_CHANNELS, RECORDER_AUDIO_ENCODING);
            mIsRecording = false;
        }

        @Override
        public void run() {
            mAudioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC, RECORDER_SAMPLERATE, RECORDER_CHANNELS,
                    RECORDER_AUDIO_ENCODING, BUFFER_SIZE);
            mAudioRecord.startRecording();
            mIsRecording = true;
            writeAudioDataToFile();
        }

        private void writeAudioDataToFile() {
            byte[] buffer = new byte[BUFFER_SIZE];
            byte[] data = new byte[BUFFER_SIZE];
            File waveFile = new File(Environment.getExternalStorageDirectory()+"/"+mFileName);
            File tempFile = new File(Environment.getExternalStorageDirectory()+"/"+TEMP_FILE_NAME);

            try {
                mBOStream = new BufferedOutputStream(new FileOutputStream(tempFile));
            } catch (FileNotFoundException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }

            int read = 0;
            int len = 0;
            if (null != mBOStream) {
                try {
                    while (mIsRecording) {
                        read = mAudioRecord.read(data, 0, BUFFER_SIZE);
                        if (AudioRecord.ERROR_INVALID_OPERATION != read) {
                            mBOStream.write(data);
                        }
                    }

                    mBOStream.flush();
                    mAudioLen = (int)tempFile.length();
                    mBIStream = new BufferedInputStream(new FileInputStream(tempFile));
                    mBOStream.close();
                    mBOStream = new BufferedOutputStream(new FileOutputStream(waveFile));
                    mBOStream.write(getFileHeader());
                    len = HEADER_SIZE;
                    while ((read = mBIStream.read(buffer)) != -1) {
                        mBOStream.write(buffer);
                    }
                    mBOStream.flush();
                    mBIStream.close();
                    mBOStream.close();

                } catch (IOException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
            }
        }

        private byte[] getFileHeader() {
            byte[] header = new byte[HEADER_SIZE];
            int totalDataLen = mAudioLen + 40;
            long byteRate = RECORDER_BPP * RECORDER_SAMPLERATE * WAVE_CHANNEL_MONO/8;
            header[0] = 'R';  // RIFF/WAVE header
            header[1] = 'I';
            header[2] = 'F';
            header[3] = 'F';
            header[4] = (byte) (totalDataLen & 0xff);
            header[5] = (byte) ((totalDataLen >> 8) & 0xff);
            header[6] = (byte) ((totalDataLen >> 16) & 0xff);
            header[7] = (byte) ((totalDataLen >> 24) & 0xff);
            header[8] = 'W';
            header[9] = 'A';
            header[10] = 'V';
            header[11] = 'E';
            header[12] = 'f';  // 'fmt ' chunk
            header[13] = 'm';
            header[14] = 't';
            header[15] = ' ';
            header[16] = 16;  // 4 bytes: size of 'fmt ' chunk
            header[17] = 0;
            header[18] = 0;
            header[19] = 0;
            header[20] = (byte)1;  // format = 1 (PCM방식)
            header[21] = 0;
            header[22] =  WAVE_CHANNEL_MONO;
            header[23] = 0;
            header[24] = (byte) (RECORDER_SAMPLERATE & 0xff);
            header[25] = (byte) ((RECORDER_SAMPLERATE >> 8) & 0xff);
            header[26] = (byte) ((RECORDER_SAMPLERATE >> 16) & 0xff);
            header[27] = (byte) ((RECORDER_SAMPLERATE >> 24) & 0xff);
            header[28] = (byte) (byteRate & 0xff);
            header[29] = (byte) ((byteRate >> 8) & 0xff);
            header[30] = (byte) ((byteRate >> 16) & 0xff);
            header[31] = (byte) ((byteRate >> 24) & 0xff);
            header[32] = (byte) RECORDER_BPP * WAVE_CHANNEL_MONO/8;  // block align
            header[33] = 0;
            header[34] = RECORDER_BPP;  // bits per sample
            header[35] = 0;
            header[36] = 'd';
            header[37] = 'a';
            header[38] = 't';
            header[39] = 'a';
            header[40] = (byte)(mAudioLen & 0xff);
            header[41] = (byte)((mAudioLen >> 8) & 0xff);
            header[42] = (byte)((mAudioLen >> 16) & 0xff);
            header[43] = (byte)((mAudioLen >> 24) & 0xff);
            return header;
        }

        public void stopRecording() {
            if (null != mAudioRecord) {
                mIsRecording = false;
                mAudioRecord.stop();
                mAudioRecord.release();
                mAudioRecord = null;
            }
        }
    }
