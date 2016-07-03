package com.dannyofir.www.opencameratest10;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class AppMainActivity extends AppCompatActivity {

    private String lastFilePath;
    private EditText convertText;
    private Bitmap picture;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_main);

        convertText = (EditText) findViewById(R.id.editTextWatermark);

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

                new FFMPEGAsyncTask(AppMainActivity.this, lastFilePath, convertText.getText().toString()).execute();

            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);

        if(resultCode == RESULT_OK){

            lastFilePath = getRealPathFromUri(this, data.getData());

        }

    }

    public static String getRealPathFromUri(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

}
