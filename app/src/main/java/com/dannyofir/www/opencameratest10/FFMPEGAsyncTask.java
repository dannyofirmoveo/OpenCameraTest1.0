package com.dannyofir.www.opencameratest10;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.PowerManager;
import android.util.Log;
import android.widget.MediaController;
import android.widget.Toast;
import android.widget.VideoView;

import com.dannyofir.www.opencameratest10.R;
import com.netcompss.ffmpeg4android.GeneralUtils;
import com.netcompss.ffmpeg4android.Prefs;
import com.netcompss.loader.LoadJNI;

import java.io.File;

/**
 * Created by dannyofir on 06/04/16.
 */
public class FFMPEGAsyncTask extends AsyncTask<String, Integer, Integer>
{

    ProgressDialog progressDialog;
    Activity activity;

    private String lastFilePath;

    String workFolder = null;
    String videoFolder = null;
    String vkLogPath = null;
    private boolean commandValidationFailedFlag = false;

    public FFMPEGAsyncTask(Activity activity, String lastFilePath) {
        this.activity = activity;
        this.lastFilePath = lastFilePath;
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

        PowerManager powerManager = (PowerManager) activity.getSystemService(Activity.POWER_SERVICE);
        PowerManager.WakeLock wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "VK_LOCK");
        Log.d(Prefs.TAG, "Acquire wake lock");
        wakeLock.acquire();

        // Find a way to get the watermark from the app installation
        String[] commandStr = {"ffmpeg","-y" ,"-i", lastFilePath,"-strict","experimental", "-vf", "movie=" + videoFolder + "/watermark.gif [watermark]; [in][watermark] overlay=main_w-overlay_w-10:10 [out]","-s", "320x240","-r", "30", "-b", "15496k", "-vcodec", "mpeg4","-ab", "48000", "-ac", "2", "-ar", "22050", videoFolder + "/newVideo.mp4"};

        ///////////// Set Command using code (overriding the UI EditText) /////
        //String commandStr = "ffmpeg -y -i /sdcard/videokit/in.mp4 -strict experimental -s 320x240 -r 30 -aspect 3:4 -ab 48000 -ac 2 -ar 22050 -vcodec mpeg4 -b 2097152 /sdcard/videokit/out.mp4";
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
            GeneralUtils.copyFileToFolder(vkLogPath, videoFolder);

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
            rc = "Command Vaidation Failed";
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

}
