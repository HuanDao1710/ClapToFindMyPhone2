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
        minFrequency = 600.0f;
        maxFrequency = 3200.0f;

        // get the decay part of a clap
        minIntensity = 10000.0f;
        maxIntensity = 100000.0f;

        minStandardDeviation = 0.0f;
        maxStandardDeviation = 0.5f;

        highPass = 100;
        lowPass = 10000;

        minNumZeroCross = 50;
        maxNumZeroCross = 300;

        numRobust = 3;
    }


    @Override
    protected boolean isPassedFrequency(double[] spectrum){
        // find the robust frequency
        ArrayRankDouble arrayRankDouble = new ArrayRankDouble();
        double robustFrequency = arrayRankDouble.getMaxValueIndex(spectrum) * unitFrequency;

        // frequency of the sound should not be too low or too high
        boolean result = (robustFrequency >= minFrequency && robustFrequency <= maxFrequency);
        //System.out.println("freq: " + robustFrequency + " " + result);
        return result;
    }
    //
    @Override
    protected boolean isPassedZeroCrossingRate(short[] amplitudes){
        ZeroCrossingRate zcr = new ZeroCrossingRate(amplitudes, 1);
        int numZeroCrosses = (int) zcr.evaluate();
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
                // run all checking for debug
                boolean isPassedChecking = true;
                // rule 1: check the intensity of this frame
                isPassedChecking &= isPassedIntensity(spectrum);
                isPassedChecking &= isPassedFrequency(rangedSpectrum);
                // rule 4: check the standard deviation of this frame with reference of previous frames
                isPassedChecking &= isPassedStandardDeviation(spectrogramData);
                System.out.println("Result: " + isPassedChecking + "\n");
                return isPassedChecking;
                // end run all checking for debug
            }
        }
        return false;
    }

}