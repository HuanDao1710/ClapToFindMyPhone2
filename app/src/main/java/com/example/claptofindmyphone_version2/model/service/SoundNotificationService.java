package com.example.claptofindmyphone_version2.model.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.os.Build;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.Log;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;

import com.example.claptofindmyphone_version2.R;
import com.example.claptofindmyphone_version2.MainActivity;
import com.example.claptofindmyphone_version2.model.constant.Constant;

/**
 * Created by Filippo-TheAppExpert on 8/10/2015.
 */
public class SoundNotificationService extends Service implements MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnCompletionListener {

    private static final String TAG = SoundNotificationService.class.getSimpleName();
    private MediaPlayer mPlayer;
    private boolean mRunning;

    final int DELAY = 0, VIBRATE = 1000, SLEEP = 1000, START = -1;
    long[] vibratePattern = {DELAY, VIBRATE, SLEEP};
    //Rung
    VibratorManager vibratorManager;
    Vibrator vibrator;

    @Override
    public void onCreate() {
        super.onCreate();
        mRunning = false;
        mPlayer = MediaPlayer.create(this, R.raw.ring);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
        mPlayer.setOnPreparedListener(this);

        vibratorManager = (VibratorManager) this.getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
        vibrator = vibratorManager.getDefaultVibrator();
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {
        //Toast.makeText(getApplicationContext(), "onCompletion", Toast.LENGTH_SHORT).show();
        cancelNotification();
        stopSelf();
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int i, int i1) {
        Toast.makeText(getApplicationContext(), "onError", Toast.LENGTH_SHORT).show();
        cancelNotification();
        stopSelf();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {
        //Toast.makeText(getApplicationContext(), "onPrepared", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "Service is starting onStartCommand");

        if (!mRunning) {
            mRunning = true;
            createNotification();
            mPlayer.start();
            vibrate();
        }
        return super.onStartCommand(intent, flags, startId);
    }

    private void vibrate () {

        // this is the only type of the vibration which requires system version Oreo (API 26)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, START));
            return;
        }
        // noinspection deprecation
        vibrator.vibrate(vibratePattern, START);

    }

    public void onStop() {
        mPlayer.stop();
        mPlayer.release();
    }

    public void onPause() {
        mPlayer.stop();
        mPlayer.release();
    }

    @Override
    public void onDestroy() {
        mPlayer.stop();
        mPlayer.release();
        cancelNotification();
        super.onDestroy();
    }

    @Override
    public void onLowMemory() {
        cancelNotification();
        stopSelf();
    }

    public void createNotification() {
        // Prepare intent which is triggered if the
        // notification is selected
        Intent intent = new Intent(this, MainActivity.class);
        intent.putExtra("key", "Filippo");
        PendingIntent pIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE);


        // Build notification

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, Constant.CHANEL_NOTIFICATION_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setTicker("ፉጨት Phone Finder")
                .setContentTitle("My notification")
                .setAutoCancel(true)
                .setContentText("Notification Text Message!")
                .setContentIntent(pIntent); //Required on Gingerbread and below

        builder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.notify(0, builder.build());
        getSystemService(NOTIFICATION_SERVICE);
        // hide the notification after its selected
    }

    private void cancelNotification() {
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancel(0);
        getSystemService(NOTIFICATION_SERVICE);
    }
}
