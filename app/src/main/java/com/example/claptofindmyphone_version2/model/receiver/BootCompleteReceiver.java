package com.example.claptofindmyphone_version2.model.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.example.claptofindmyphone_version2.model.constant.Constant;
import com.example.claptofindmyphone_version2.model.service.DetectionService;
import com.example.claptofindmyphone_version2.model.utilities.SharedPreferenceUtils;

/**
 * Starts the Service at boot if it's specified in the preferences.
 *
 * @author Filippo Engidashet
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent i) {

        boolean status = Boolean.parseBoolean(SharedPreferenceUtils.getValue(context, Constant.ENABLE_PREFERENCE));
        if (status) {
            DetectionService.startDetection(context);
        }
    }
}
