package com.example.claptofindmyphone_version2.model.helper;

import com.musicg.api.DetectionApi;
import com.musicg.api.WhistleApi;
import com.musicg.wave.WaveHeader;

public class CustomWhistAPI extends DetectionApi {
    public CustomWhistAPI(WaveHeader waveHeader) {
        super(waveHeader);
    }

    @Override
    protected void init() {
        // settings for detecting a whistle
        minFrequency = 600.0f;
        maxFrequency = Double.MAX_VALUE;

        minIntensity = 100.0f;
        maxIntensity = 100000.0f;

        minStandardDeviation = 0.1f;
        maxStandardDeviation = 1.0f;

        highPass = 100;
        lowPass = 10000;

        minNumZeroCross = 50;
        maxNumZeroCross = 200;

        numRobust = 10;
    }

    public boolean isWhistle(byte[] audioBytes){
        return isSpecificSound(audioBytes);
    }
    void setMinIntensity(double intensity) {
        this.minIntensity = intensity;
    }

}
