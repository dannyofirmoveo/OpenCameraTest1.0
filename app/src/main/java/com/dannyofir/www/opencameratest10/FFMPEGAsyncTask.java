package com.dannyofir.www.opencameratest10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by dannyofir on 06/04/16.
 */
public class FFMPEGAsyncTask extends AsyncTask<String, Integer, Integer>
{

    ProgressDialog progressDialog;
    Activity activity;

    private String lastFilePath;
    private String convertText;
    private Bitmap convertedText;
    private File textToImageFile;

    String workFolder = null;
    String videoFolder = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;

    public FFMPEGAsyncTask(Activity activity, String lastFilePath, String convertText) {
        this.activity = activity;
        this.lastFilePath = lastFilePath;
        this.convertText = convertText;
    }

    @Override
    protected void onPreExecute() {

        progressDialog = new ProgressDialog(activity);
        progressDialog.setMessage("FFmpeg4Android Transcoding in progress...");
        progressDialog.show();

        Log.i(Prefs.TAG, activity.getString(R.string.app_name) + " version: " + GeneralUtils.getVersionName(activity));
        workFolder = activity.getFilesDir().getAbsolutePath() + "/";
        Log.i(Prefs.TAG, "workFolder (license and logs location) path: " + workFolder);
        vkLogPath = workFolder + "vk.log";
        Log.i(Prefs.TAG, "vk log (native log) path: " + vkLogPath);

        File file = new File(lastFilePath);
        videoFolder = file.getParent();

        GeneralUtils.copyLicenseFromAssetsToSDIfNeeded(activity, workFolder);

    }

    protected Integer doInBackground(String... paths) {
        Log.i(Prefs.TAG, "doInBackground started...");

        // delete previous log
        GeneralUtils.deleteFileUtil(videoFolder + "/vk.log");

        // Converts text to image and saves image to device
        convertedText = textAsBitmap(convertText, 100, Color.BLACK);
        storeImage(convertedText);

        PowerManager powerManager = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
        Log.d(Prefs.TAG, "Acquire wake lock");
        wakeLock.acquire();

        // Find a way to get the watermark from the app installation
        String[] commandStr = {"ffmpeg","-y" ,"-i", lastFilePath,"-strict","experimental", "-vf", "movie="+videoFolder+"/watermark.png [watermarkBase]; movie="+textToImageFile+" [scrollingText]; [in][watermarkBase] overlay=main_w-overlay_w-10:10 [tmp]; [tmp][scrollingText] overlay=x='if(gte(t,2), -w+(t-2)*200, NAN)':y=0 [out]","-s", "640x360","-r", "30", "-b", "150k", "-vcodec", "mpeg4","-ab", "48000", "-ac", "2", "-ar", "22050", videoFolder + "/newVideo.mp4"};

        // Good commandStr = String[] commandStr = {"ffmpeg","-y" ,"-i", lastFilePath,"-strict","experimental", "-vf", "movie="+videoFolder+"/watermark.png [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]","-s", "640x360","-r", "30", "-b", "150k", "-vcodec", "mpeg4","-ab", "48000", "-ac", "2", "-ar", "22050", videoFolder + "/newVideo.mp4"};
        String[] commandStr2 = {"ffmpeg","-i",lastFilePath,"-vf",videoFolder + "/newVideo.mp4"};

        ///////////// Set Command using code (overriding the UI EditText) /////
        String commandStr3 = "ffmpeg –i "+lastFilePath+" -vf \"movie="+videoFolder+"/watermark.png [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]\" "+videoFolder+"/newVideo.mp4";
        //String[] complexCommand = {"ffmpeg", "-y" ,"-i", "/sdcard/videokit/in.mp4","-strict","experimental","-s", "160x120","-r","25", "-vcodec", "mpeg4", "-b", "150k", "-ab","48000", "-ac", "2", "-ar", "22050", "/sdcard/videokit/out.mp4"};
        ///////////////////////////////////////////////////////////////////////


        LoadJNI vk = new LoadJNI();
        try {

            // complex command
            //vk.run(complexCommand, workFolder, getApplicationContext());

            vk.run(commandStr, workFolder, activity);

            // running without command validation
            //vk.run(complexCommand, workFolder, getApplicationContext(), false);

            // copying vk.log (internal native log) to the videokit folder
            GeneralUtils.copyFileToFolder(vkLogPath, videoFolder + "/");

//			} catch (CommandValidationException e) {
//					Log.e(Prefs.TAG, "vk run exeption.", e);
//					commandValidationFailedFlag = true;

        } catch (Throwable e) {
            Log.e(Prefs.TAG, "vk run exeption.", e);
        }
        finally {
            if (wakeLock.isHeld())
                wakeLock.release();
            else{
                Log.i(Prefs.TAG, "Wake lock is already released, doing nothing");
            }
        }
        Log.i(Prefs.TAG, "doInBackground finished");
        return Integer.valueOf(0);
    }

    protected void onProgressUpdate(Integer... progress) {
        progressDialog.setProgress(progress.length);
    }

    @Override
    protected void onCancelled() {
        Log.i(Prefs.TAG, "onCancelled");
        //progressDialog.dismiss();
        super.onCancelled();
    }


    @Override
    protected void onPostExecute(Integer result) {
        Log.i(Prefs.TAG, "onPostExecute");
        progressDialog.dismiss();
        super.onPostExecute(result);

        // finished Toast
        String rc = null;
        if (commandValidationFailedFlag) {
            rc = "Command Validation Failed";
        }
        else {
            rc = GeneralUtils.getReturnCodeFromLog(vkLogPath);
        }
        final String status = rc;

        // Maybe this needs to change, check if there are further problems

        activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, status, Toast.LENGTH_LONG).show();
                if (status.equals("Transcoding Status: Failed")) {
                    Toast.makeText(activity, "Check: " + vkLogPath + " for more information.", Toast.LENGTH_LONG).show();
                }
            }
        });

//        MediaController mediaController = new MediaController(MainActivity.this);
//        mediaController.setAnchorView(videoViewMain);
//
//        videoViewMain = (VideoView) findViewById(R.id.videoViewMain);
//        videoViewMain.setVideoPath(videoFolder + "/newVideo.mp4");
//        videoViewMain.setMediaController(mediaController);
//        videoViewMain.start();

    }

    public Bitmap textAsBitmap(String text, float textSize, int textColor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(textColor);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent(); // ascent() is negative
        int width = (int) (paint.measureText(text) + 0.5f); // round
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawText(text, 0, baseline, paint);
        return image;
    }

    private  File getOutputMediaFile(){
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.
        File mediaStorageDir = new File(Environment.getExternalStorageDirectory()
                + "/Android/data/"
                + activity.getPackageName()
                + "/Files");

        // This location works best if you want the created images to be shared
        // between applications and persist after your app has been uninstalled.

        // Create the storage directory if it does not exist
        if (! mediaStorageDir.exists()){
            if (! mediaStorageDir.mkdirs()){
                return null;
            }
        }
        // Create a media file name
        String timeStamp = new SimpleDateFormat("ddMMyyyy_HHmm").format(new Date());
        File mediaFile;
        String mImageName="MI_"+ timeStamp +".jpg";
        // TODO changed this so it has the same filename each time
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "samefile");
        return mediaFile;
    }

    private void storeImage(Bitmap image) {
        textToImageFile = getOutputMediaFile();
        if (textToImageFile == null) {
            Log.d("Check Permissions",
                    "Error creating media file, check storage permissions: ");// e.getMessage());
            return;
        }
        try {
            FileOutputStream fos = new FileOutputStream(textToImageFile);
            image.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.close();
        } catch (FileNotFoundException e) {
            Log.d("FNF", "File not found: " + e.getMessage());
        } catch (IOException e) {
            Log.d("EAF", "Error accessing file: " + e.getMessage());
        }
    }



}
