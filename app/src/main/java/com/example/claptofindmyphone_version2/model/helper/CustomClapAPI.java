package com.example.claptofindmyphone_version2.model.helper;

import android.util.Log;

import com.musicg.api.DetectionApi;
import com.musicg.math.rank.ArrayRankDouble;
import com.musicg.math.statistics.ZeroCrossingRate;
import com.musicg.wave.Wave;
import com.musicg.wave.WaveHeader;
import com.musicg.wave.extension.Spectrogram;

public class CustomClapAPI extends DetectionApi {
    public CustomClapAPI(WaveHeader waveHeader) {
        super(waveHeader);
    }

    public boolean isClap(byte[] audioBytes){
        return isSpecificSound(audioBytes);
    }
    protected void init(){
        // settings for detecting a clap
        minFrequency = 700.0f;
        maxFrequency = 2000.0f;

        // get the decay part of a clap
        minIntensity = 10000.0f;
        maxIntensity = 100000.0f;

        minStandardDeviation = 0.0f;
        maxStandardDeviation = 0.1f;

        highPass = 100;
        lowPass = 10000;

        minNumZeroCross = 60;
        maxNumZeroCross = 300;
        numRobust = 4;
    }


    void setMinIntensity(double intensity) {
        this.minIntensity = intensity;
    }
    @Override
    protected boolean isPassedZeroCrossingRate(short[] amplitudes){
        ZeroCrossingRate zcr = new ZeroCrossingRate(amplitudes, 1);
        int numZeroCrosses = (int) zcr.evaluate();
//        try{
//            if(numZeroCrosses > 50) {
//                System.out.println("CrossingRate: " + numZeroCrosses) ;
//                Thread.sleep(2000);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        // different sound has different range of zero crossing value
        // when lengthInSecond=1, zero crossing rate is the num
        // of zero crosses
        boolean result = (numZeroCrosses >= minNumZeroCross && numZeroCrosses <= maxNumZeroCross);
        //System.out.println("zcr: " + numZeroCrosses + " " +result);

        return result;
    }


    @Override
    public boolean isSpecificSound(byte[] audioBytes) {

        int bytesPerSample = waveHeader.getBitsPerSample() / 8;
        int numSamples = audioBytes.length / bytesPerSample;
        // numSamples required to be a power of 2
        if (numSamples > 0 && Integer.bitCount(numSamples) == 1) {
            fftSampleSize = numSamples;
            numFrequencyUnit = fftSampleSize / 2;
            // frequency could be caught within the half of nSamples according to Nyquist theory
            unitFrequency = (double) waveHeader.getSampleRate() / 2 / numFrequencyUnit;
            // set boundary
            lowerBoundary = (int) (highPass / unitFrequency);
            upperBoundary = (int) (lowPass / unitFrequency);
            // end set boundary
            Wave wave = new Wave(waveHeader, audioBytes);	// audio bytes of this frame
            short[] amplitudes = wave.getSampleAmplitudes();
            // spectrum for the clip
            Spectrogram spectrogram = wave.getSpectrogram(fftSampleSize, 0);
            double[][] spectrogramData = spectrogram.getAbsoluteSpectrogramData();
            // since fftSampleSize==numSamples, there're only one spectrum which is thisFrameSpectrogramData[0]
            double[] spectrum = spectrogramData[0];
            int frequencyUnitRange = upperBoundary - lowerBoundary + 1;
            double[] rangedSpectrum = new double[frequencyUnitRange];
            System.arraycopy(spectrum, lowerBoundary, rangedSpectrum, 0, rangedSpectrum.length);
            if (frequencyUnitRange <= spectrum.length) {
                try {
                    // run all checking for debug
                    boolean isPassedChecking = true;
                    isPassedChecking &= isPassedIntensity(spectrum);
//                    if (isPassedChecking) {
//                        Log.d("TAG", "isSpecificSound: isPassedIntensity");
//                        Thread.sleep(2000);
//                    }
                    isPassedChecking &= isPassedFrequency(rangedSpectrum);
//                    if (isPassedChecking) {
//                        Log.d("TAG", "isSpecificSound: " + "isPassedFrequency");
//                        Thread.sleep(2000);
//                    }
                    isPassedChecking &= isPassedStandardDeviation(spectrogramData);
//                    if (isPassedChecking) {
//                        Log.d("TAG", "isSpecificSound: isPassedStandardDeviation");
//                        Thread.sleep(2000);
//                    }
                    isPassedChecking &= isPassedZeroCrossingRate(amplitudes);
//                    if (isPassedChecking) {
//                        Log.d("TAG", "isSpecificSound: isPassedZeroCrossingRate");
//                        Thread.sleep(2000);
//                    }
                    return isPassedChecking;
                } catch (Exception e) {
                    e.printStackTrace();
                }
                // end run all checking for debug
            }
        }
        return false;
    }

}