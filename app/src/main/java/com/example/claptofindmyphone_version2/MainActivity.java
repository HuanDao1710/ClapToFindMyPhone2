package com.example.claptofindmyphone_version2;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import com.example.claptofindmyphone_version2.controller.Controller;

public class MainActivity extends AppCompatActivity implements Controller.ControllerListener {

    private Controller mController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mController = new Controller(MainActivity.this);
        mController.startDetection();
    }

    @Override
    public MainActivity getMainControllerContext() {
        return MainActivity.this;
    }

    @Override
    public void start(Intent intent) {
        startActivity(intent);
    }

    @Override
    public void help() {
        showMessage();
    }

    private void showMessage() {
        Toast.makeText(getApplicationContext(), "This functionality will be implemented on the next version of the app!", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void selectNotificationSound() {
        showMessage();
    }
}