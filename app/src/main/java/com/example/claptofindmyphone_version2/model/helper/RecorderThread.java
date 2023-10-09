package com.example.claptofindmyphone_version2.model.helper;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.util.Log;
import androidx.core.app.ActivityCompat;

import com.example.claptofindmyphone_version2.MainActivity;
import com.example.claptofindmyphone_version2.model.constant.Constant;
public class RecorderThread extends Thread {

    private AudioRecord audioRecord;
    private int channelConfiguration;
    private int audioEncoding;
    private int sampleRate;
    private int overLapByteSize;
    private int byteStepSize;
    private int frameByteSize; // for 1024 fft size (16bit sample size)
    byte[] buffer;

    private byte[] overlapBuffer;

    public RecorderThread(Context context) {

        sampleRate = 44100;
        frameByteSize = 1024 * 2;

        channelConfiguration = AudioFormat.CHANNEL_IN_MONO;
        audioEncoding = AudioFormat.ENCODING_PCM_16BIT;
//
        int recBufSize = AudioRecord.getMinBufferSize(sampleRate,
                channelConfiguration, audioEncoding); // need to be larger than size of a frame

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((MainActivity) context,
                    new String[]{android.Manifest.permission.RECORD_AUDIO},
                    Constant.PERMISSION_REQUEST_CODE);
        }
        audioRecord = new AudioRecord(MediaRecorder.AudioSource.MIC,
                sampleRate, channelConfiguration, audioEncoding, recBufSize);
        buffer = new byte[frameByteSize];
        overlapBuffer = new byte[overLapByteSize];
    }



    public AudioRecord getAudioRecord() {
        return audioRecord;
    }

    public boolean isRecording() {
        if (audioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            return true;
        }

        return false;
    }

    public void startRecording() {
        try {
            audioRecord.startRecording();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public void stopRecording() {
        try {
            audioRecord.stop();
            audioRecord.release();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getMaxAmplitude() {
        int amplitude = (buffer[0] & 0xff) << 8 | buffer[1];
        return Math.abs(amplitude);
    }

    private void initOverlapAndStepSize() {
        assert overLapByteSize < frameByteSize;

        byteStepSize = frameByteSize - overLapByteSize;
    }

    public byte[] getNextBlock() {
        int bytesRead = audioRecord.read(buffer, 0, frameByteSize);
        if (bytesRead <= 0) {
            return null;
        }

        byte[] frame = new byte[frameByteSize];
        System.arraycopy(buffer, 0, frame, 0, frameByteSize);

        // Lưu trữ phần overlap từ frame hiện tại
        System.arraycopy(buffer, byteStepSize, overlapBuffer, 0, overLapByteSize);

        // Dịch chuyển buffer theo byteStepSize
        System.arraycopy(buffer, byteStepSize, buffer, 0, overLapByteSize);

        // Sao chép phần overlap vào cuối buffer
        System.arraycopy(overlapBuffer, 0, buffer, overLapByteSize, overLapByteSize);

        // Phân tích âm thanh
        int totalAbsValue = 0;
        short sample = 0;
        float averageAbsValue = 0.0f;

        for (int i = 0; i < frameByteSize; i += 2) {
            sample = (short) ((frame[i]) | frame[i + 1] << 8);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;

        Log.e("", "averageAbsValue : " + averageAbsValue);

        // Không có đầu vào
        if (averageAbsValue < 30) {
            Log.e("NO INPUT", "avg: " + averageAbsValue);
            return null;
        }

        return frame;
    }





    public byte[] getFrameBytes() {
        audioRecord.read(buffer, 0, frameByteSize);

        // analyze sound
        int totalAbsValue = 0;
        short sample = 0;
        float averageAbsValue = 0.0f;

        for (int i = 0; i < frameByteSize; i += 2) {
            sample = (short) ((buffer[i]) | buffer[i + 1] << 8);
            totalAbsValue += Math.abs(sample);
        }
        averageAbsValue = totalAbsValue / frameByteSize / 2;

        Log.e("", "averageAbsValue : " + averageAbsValue);

        // no input
        if (averageAbsValue < 30) {
            Log.e("NO INPUT", "avg: " + averageAbsValue);
            return null;
        }

        return buffer;
    }

}