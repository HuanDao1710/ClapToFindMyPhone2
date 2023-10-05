package com.example.claptofindmyphone_version2.model.receiver;

import android.content.Context;
import android.util.Log;
import com.example.claptofindmyphone_version2.model.service.DetectionService;
import java.util.Date;


public class CallReceiver extends PhoneCallReceiver {
    private static final String TAG = CallReceiver.class.getSimpleName();
    @Override
    protected void onIncomingCallStarted(Context ctx, String number, Date start) {
        Log.d(TAG, "PhoneCallReceiver detected onIncomingCallStarted");
        DetectionService.stopDetection(ctx);
    }
    @Override
    protected void onOutgoingCallStarted(Context ctx, String number, Date start) {
        Log.d(TAG, "PhoneCallReceiver detected onOutgoingCallStarted");
        DetectionService.stopDetection(ctx);
    }
    @Override
    protected void onIncomingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d(TAG, "PhoneCallReceiver detected onIncomingCallEnded");
        DetectionService.startDetection(ctx);
    }
    @Override
    protected void onOutgoingCallEnded(Context ctx, String number, Date start, Date end) {
        Log.d(TAG, "PhoneCallReceiver detected onOutgoingCallEnded");
        DetectionService.startDetection(ctx);
    }
    @Override
    protected void onMissedCall(Context ctx, String number, Date start) {
        Log.d(TAG, "PhoneCallReceiver detected onMissedCall");
        DetectionService.startDetection(ctx);
    }

}
