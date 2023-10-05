package com.example.claptofindmyphone_version2.controller;

import android.content.Intent;
import android.net.Uri;
import com.example.claptofindmyphone_version2.model.constant.Constant;
import com.example.claptofindmyphone_version2.model.service.DetectionService;
import com.example.claptofindmyphone_version2.model.utilities.SharedPreferenceUtils;
import com.example.claptofindmyphone_version2.MainActivity;

/**
 * Created by Filippo-TheAppExpert on 8/10/2015.
 */
public class Controller {

    private ControllerListener mListener;

    public Controller(ControllerListener listener) {
        mListener = listener;
    }


    public void browseCode() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(Constant.GITHUB_LINK));
        mListener.start(intent);
    }

    public synchronized void startDetection() {
        boolean status = Boolean.parseBoolean(SharedPreferenceUtils.getValue(mListener.getMainControllerContext(), Constant.ENABLE_PREFERENCE));
        if (status) {
            DetectionService.startDetection(mListener.getMainControllerContext());;
        }
    }

    public void stopDetection() {
        DetectionService.stopDetection(mListener.getMainControllerContext());
    }

    public void selectNotificationSound() {
        mListener.selectNotificationSound();
    }

    public void help() {
        mListener.help();
    }

    public interface ControllerListener {

        MainActivity getMainControllerContext();

        void start(Intent intent);

        void help();

        void selectNotificationSound();
    }
}