package com.example.claptofindmyphone_version2.model.helper;

import java.util.LinkedList;
import java.util.Vector;

import com.musicg.api.DetectionApi;
import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;
import android.media.AudioFormat;
import android.media.AudioRecord;
import android.util.Log;

public class DetectorThread extends Thread {

    private DetectorType mType;
    private RecorderThread recorder;
    private WaveHeader waveHeader;
    private DetectionApi mDetectionApi;
    private Thread _thread;
    private LinkedList<Boolean> whistleResultList = new LinkedList<>();
    private int numWhistles;
    private int totalWhistlesDetected = 0;
    private int whistleCheckLength = 2;
    private int whistlePassScore = 2;
    int minDistance = 200;
    int maxDistance = 1000;
    private OnSoundListener onSoundListener;
    private int requiredClapCount = 3;
    private Vector<Long> claps = new Vector<>();
    private boolean isSound;

    public DetectorThread(RecorderThread recorder, DetectorType type) {
        mType = type;
        this.recorder = recorder;
        AudioRecord audioRecord = recorder.getAudioRecord();
        int bitsPerSample = 0;
        if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) {
            bitsPerSample = 16;
        } else if (audioRecord.getAudioFormat() == AudioFormat.ENCODING_PCM_8BIT) {
            bitsPerSample = 8;
        }
        int channel = 0;
        if (audioRecord.getChannelConfiguration() == AudioFormat.CHANNEL_IN_MONO) {
            channel = 1;
        }
        waveHeader = new WaveHeader();
        waveHeader.setChannels(channel);
        waveHeader.setBitsPerSample(bitsPerSample);
        waveHeader.setSampleRate(audioRecord.getSampleRate());

        switch (type) {
            case CLAP:
                mDetectionApi = new CustomClapAPI(waveHeader);
                break;
            case WHISTLE:
                mDetectionApi = new WhistleApi(waveHeader);
                break;
        }
    }

    private void initBufferWhists() {
        numWhistles = 0;
        whistleResultList.clear();
        // init the first frames
        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
        // end init the first frames
    }

    private void initBufferClaps() {
        numWhistles = 0;
        whistleResultList.clear();
        // init the first frames
        for (int i = 0; i < whistleCheckLength; i++) {
            whistleResultList.add(false);
        }
        // end init the first frames
    }

    public void start() {
        _thread = new Thread(this);
        _thread.start();
    }

    public void stopDetection() {
        _thread = null;
    }

    @Override
    public void run() {
        Log.e("", "DetectorThread started...");
        switch (mType) {
            case CLAP:
                listenApplause();
                break;
            case WHISTLE:
                listenWhistles();
                break;
        }
    }

    private void listenWhistles() {
        initBufferWhists();
        byte[] buffer;
        Thread thisThread = Thread.currentThread();
        while (_thread == thisThread) {
            // detect sound
            buffer = recorder.getFrameBytes();
            // audio analyst
            if (buffer != null) {
                try {
                    boolean isWhistle = ((WhistleApi) mDetectionApi).isWhistle(buffer);
                    isSound = isWhistle;
                    if (whistleResultList.getFirst()) {
                        numWhistles--;
                    }
                    whistleResultList.removeFirst();
                    whistleResultList.add(isSound);
                    if (isSound) {
                        numWhistles++;
                    }
                    Log.e("", "numWhistles : " + numWhistles);
                    if (numWhistles >= whistlePassScore) {
                        // clear buffer
                        initBufferWhists();
                        totalWhistlesDetected++;
                        Log.d("TAG","Whistles Detected!");
                        Thread.sleep(2000);
                        Log.e("", "totalWhistlesDetected : "
                                + totalWhistlesDetected);
                        if (onSoundListener != null) {
                            onSoundListener.onSound(mType);
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Log.w("", "error " + e.getMessage()+" Cause :: " + e.getCause());
                }
                // end whistle detection
            } else {
                // Debug.e("", "no sound detected");
                // no sound detected
                if (whistleResultList.getFirst()) {
                    numWhistles--;
                }
                whistleResultList.removeFirst();
                whistleResultList.add(false);
                // MainActivity.whistleValue = numWhistles;
            }
        }
        Log.e("", "Terminating detector thread...");
    }

    private void listenApplause() {
        byte[] buffer;
        Thread thisThread = Thread.currentThread();
        while (_thread == thisThread) {
            // detect sound
            buffer = recorder.getFrameBytes();
            // audio analyst
            if(buffer == null)  continue;
            try {
                boolean isClap = ((CustomClapAPI) mDetectionApi).isClap(buffer);
                if(!isClap) continue;
                long currentTime = System.currentTimeMillis();
                if(claps.size() > 0 ) {
                    long distance = currentTime - claps.get(claps.size() - 1);
                    if(distance > maxDistance) {
                        claps.clear();
                    }
                    if(distance < minDistance) {
                        continue;
                    }
                }
                claps.add(currentTime);
                Log.i("Mitch","claps = " + claps.size());
                if(claps.size() >= requiredClapCount){
                    claps.clear();
                    Log.d("TAG","Claps Detected!");
                    Thread.sleep(2000);
                    if (onSoundListener != null) {
                        onSoundListener.onSound(mType);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                Log.w("", "error " + e.getMessage()+" Cause :: " + e.getCause());
            }
                // end whistle detection
        }
        Log.e("", "Terminating detector thread...");
    }


    public void setOnSoundListener(OnSoundListener onSoundListener) {
        this.onSoundListener = onSoundListener;
    }

    public interface OnSoundListener {
        void onSound(DetectorType type);
    }

}