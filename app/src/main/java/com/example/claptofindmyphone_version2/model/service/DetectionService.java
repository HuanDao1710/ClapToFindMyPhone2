package com.example.claptofindmyphone_version2.model.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;
import com.example.claptofindmyphone_version2.model.constant.Constant;
import com.example.claptofindmyphone_version2.model.helper.DetectorThread;
import com.example.claptofindmyphone_version2.model.helper.DetectorType;
import com.example.claptofindmyphone_version2.model.helper.RecorderThread;
import com.example.claptofindmyphone_version2.model.utilities.SharedPreferenceUtils;

/**
 * Created by Filippo-TheAppExpert on 8/10/2015.
 */
public class DetectionService extends Service implements DetectorThread.OnSoundListener {

    private static final String TAG = DetectionService.class.getSimpleName();
    private DetectorThread mDetectorThread;
    private RecorderThread mRecorderThread;
    private static Context mContext;


    public static void startDetection(Context context) {
        mContext = context;
        Toast.makeText(context, "Detection Service Started!", Toast.LENGTH_SHORT).show();
        context.startService(new Intent(context, DetectionService.class));
    }

    public static void stopDetection(Context context) {
        Toast.makeText(context, "Detection Service Stopped!", Toast.LENGTH_SHORT).show();
        context.stopService(new Intent(context, DetectionService.class));
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        try {
            if (intent != null && intent.getExtras() != null) {

                if (intent.getExtras().containsKey("action")) {
                    Log.e("", "action : " + intent.getStringExtra("action"));

                    if (intent.getStringExtra("action").equals("start")) {
                        startDetection();
                    }

                    if (intent.getStringExtra("action").equals("stop")) {
                        stopDetection();
                        stopSelf();
                    }
                }
            } else {

                startDetection();
                Log.e("", "intent is null OR intent.getExtras() is null");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return super.onStartCommand(intent, flags, startId);
    }

    private void startDetection() {

        Log.d(TAG, "Start Service startDetection!");

        try {
            stopDetection();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mRecorderThread = new RecorderThread(mContext);
        mRecorderThread.startRecording();
//        String detectionType = SharedPreferenceUtils.getValue(getApplicationContext(), Constant.DETECTION_TYPE_PREFERENCE);
        String detectionType = DetectorType.CLAP.name();
        if (detectionType == null || detectionType.equals(DetectorType.WHISTLE.name())) {
            mDetectorThread = new DetectorThread(mRecorderThread, DetectorType.WHISTLE);
        } else {
            mDetectorThread = new DetectorThread(mRecorderThread, DetectorType.CLAP);
        }
        mDetectorThread.setOnSoundListener(this);
        mDetectorThread.start();
    }

    @Override
    public boolean stopService(Intent name) {
        stopDetection();
        return super.stopService(name);
    }

    private void stopDetection() {

        Log.d(TAG, "Stop Service stopDetection!");

        if (mDetectorThread != null) {
            mDetectorThread.stopDetection();
            mDetectorThread.setOnSoundListener(null);
            mDetectorThread = null;
        }

        if (mRecorderThread != null) {
            mRecorderThread.stopRecording();
            mRecorderThread = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onSound(DetectorType type) {

//        boolean status = Boolean.parseBoolean(SharedPreferenceUtils.getValue(getApplicationContext(), Constant.ENABLE_PREFERENCE));
        if (true) {
            startService(new Intent(DetectionService.this, SoundNotificationService.class));
        }
    }
}
