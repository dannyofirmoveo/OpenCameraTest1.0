package com.dannyofir.www.opencameratest10;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.dannyofir.www.opencameratest10.Preview.ApplicationInterface;

import java.net.URI;
import java.net.URISyntaxException;

public class AppMainActivity extends AppCompatActivity {

    StorageUtils storageUtils = new StorageUtils(this);
    String fileName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main);

        Button openCameraButton = (Button) findViewById(R.id.openCameraButton);
        openCameraButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(AppMainActivity.this, CameraActivity.class);

                // We set the intent action to the VIDEO_CAPTURE action, opening the video camera instead of the stills camera.
                // NOT VIDEO_CAMERA, this doesn't exit the camera once we are done taking our video, only VIDEO_CAPTURE
                intent.setAction(MediaStore.ACTION_VIDEO_CAPTURE);

                startActivityForResult(intent, 27);

            }
        });

        Button sendVideoForEditing = (Button) findViewById(R.id.sendVideoForEditing);
        sendVideoForEditing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            fileName = data.getStringExtra("filename");


        }

    }
}
