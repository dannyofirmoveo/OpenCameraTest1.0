package com.dannyofir.www.opencameratest10;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.RemoteViews;

public class MyWidgetProvider extends AppWidgetProvider {
	private static final String TAG = "MyWidgetProvider";
	
	// from http://developer.android.com/guide/topics/appwidgets/index.html
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
    	if( MyDebug.LOG )
    		Log.d(TAG, "onUpdate");
        final int N = appWidgetIds.length;
    	if( MyDebug.LOG )
    		Log.d(TAG, "N = " + N);

        // Perform this loop procedure for each App Widget that belongs to this provider
        for (int i=0; i<N; i++) {
            int appWidgetId = appWidgetIds[i];
        	if( MyDebug.LOG )
        		Log.d(TAG, "appWidgetId: " + appWidgetId);

            PendingIntent pendingIntent = null;
            // for now, always put up the keyguard if the device is PIN locked etc
			/*SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
			if( sharedPreferences.getBoolean(CameraActivity.getShowWhenLockedPreferenceKey(), true) ) {
		    	if( MyDebug.LOG )
		    		Log.d(TAG, "do show above lock screen");
	            Intent intent = new Intent(context, MyWidgetProvider.class);
	            intent.setAction("com.dannyofir.www.opencameratest10.LAUNCH_OPEN_CAMERA");
	            pendingIntent = PendingIntent.getBroadcast(context, 0, intent, 0);
			}
			else*/ {
		    	/*if( MyDebug.LOG )
		    		Log.d(TAG, "don't show above lock screen");*/
	            Intent intent = new Intent(context, CameraActivity.class);
	            pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);
			}

            // Get the layout for the App Widget and attach an on-click listener
            // to the button
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_layout);
            views.setOnClickPendingIntent(R.id.widget_launch_open_camera, pendingIntent);
			/*if( sharedPreferences.getBoolean(CameraActivity.getShowWhenLockedPreferenceKey(), true) ) {
				views.setTextViewText(R.id.launch_open_camera, "Open Camera (unlocked)");
			}
			else {
				views.setTextViewText(R.id.launch_open_camera, "Open Camera (locked)");
			}*/

            // Tell the AppWidgetManager to perform an update on the current app widget
            appWidgetManager.updateAppWidget(appWidgetId, views);
        }
    }

    /*@Override
	public void onReceive(Context context, Intent intent) {
    	if( MyDebug.LOG ) {
    		Log.d(TAG, "onReceive " + intent);
    	}
	    if (intent.getAction().equals("com.dannyofir.www.opencameratest10.LAUNCH_OPEN_CAMERA")) {
	    	if( MyDebug.LOG )
	    		Log.d(TAG, "Launching CameraActivity");
	        final Intent activity = new Intent(context, CameraActivity.class);
	        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	        context.startActivity(activity);
	    	if( MyDebug.LOG )
	    		Log.d(TAG, "done");
	    }
	    super.onReceive(context, intent);
	}*/
}
