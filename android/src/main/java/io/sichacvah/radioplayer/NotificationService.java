package io.sichacvah.radioplayer;

import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;

import android.os.Bundle;
import android.os.IBinder;
import android.app.Service;
import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import javax.annotation.Nullable;

import java.util.ArrayList;

import com.facebook.react.jstasks.HeadlessJsTaskEventListener;
import com.facebook.react.jstasks.HeadlessJsTaskConfig;
import com.facebook.react.jstasks.HeadlessJsTaskContext;
import com.facebook.react.HeadlessJsTaskService;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReactApplicationContext;

public class NotificationService extends HeadlessJsTaskService {

    public static Context context;
    Notification status;
    boolean isPause = true;
    private static final String MAIN_ACTION = "radioplayer.ACTION.MAIN_ACTION";
    private static final String PLAY_ACTION = "PLAY_ACTION";
    private static final String PAUSE_ACTION = "PAUSE_ACTION";
    private static final String STARTFOREGROUND_ACTION = "STARTFOREGROUND_ACTION";
    private static final String STOPFOREGROUND_ACTION = "STOPFOREGROUND_ACTION";  
    private static final int FOREGROUND_SERVICE = 101;
    private static final String TASK_KEY = "radioPlayerTask";

    public Class getMainActivityClass() {
        String packageName = context.getPackageName();
        Intent launchIntent = context.getPackageManager().getLaunchIntentForPackage(packageName);
        String className = launchIntent.getComponent().getClassName();
        try {
            return Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void showNotification(int pos) {
        RemoteViews views = new RemoteViews(getPackageName(),
            R.layout.status_bar);

        Intent notificationIntent = new Intent(this, getMainActivityClass());
        
        notificationIntent.setAction(MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK 
            | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0,
            notificationIntent, 0);
        
        Intent playIntent = new Intent(this, NotificationService.class);
        playIntent.setAction(PLAY_ACTION);
        PendingIntent pplayIntent = PendingIntent.getService(this, 0,
            playIntent, 0);
        
        Intent closeIntent = new Intent(this, NotificationService.class);
        closeIntent.setAction(STOPFOREGROUND_ACTION);
        PendingIntent pcloseIntent = PendingIntent.getService(this, 0,
            closeIntent, 0);
        
        views.setOnClickPendingIntent(R.id.status_bar_play, pplayIntent);

        views.setOnClickPendingIntent(R.id.status_bar_collapse, pcloseIntent);

        if (pos == 0)
        {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.pause_ntf);
        }

        if(pos == 1) {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.pause_ntf);           
        }
        if(pos == 2)
        {
            views.setImageViewResource(R.id.status_bar_play,
                    R.drawable.play_ntf);            
        }

        status = new Notification.Builder(this).build();
        status.contentView = views;
        status.flags = Notification.FLAG_ONGOING_EVENT;
        status.icon = R.drawable.radio;
        status.contentIntent = pendingIntent;
        startForeground(FOREGROUND_SERVICE, status);
    }

    @Override
    protected @Nullable HeadlessJsTaskConfig getTaskConfig(Intent intent) {
        if (intent == null) return null;

        String radioPath = intent.getStringExtra("RADIO_PATH");
        WritableMap params = Arguments.createMap();
        params.putString("radioPath", radioPath);
        String action = intent.getAction();
        if (intent.getAction().equals(PLAY_ACTION)) {
            if (!isPause) {
                action = "PAUSE";
            } else {
                action = "PLAY";
            }
        }
        params.putString("action", action);
        return new HeadlessJsTaskConfig(
            TASK_KEY,
            params,
            0,
            true
        );
    }

    @Override
    public void onHeadlessJsTaskFinish(int taskId) { }
    

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        context = getApplicationContext();
        HeadlessJsTaskConfig taskConfig = getTaskConfig(intent);
        if (taskConfig != null) {
            if (intent.getAction().equals(STARTFOREGROUND_ACTION)) {
                showNotification(0);
                isPause = false;
            } else if (intent.getAction().equals(PLAY_ACTION)) {
                if (!isPause) {
                    showNotification(2);
                    isPause = true;
                } else {
                    showNotification(1);
                    isPause = false;
                }
            } else if (intent.getAction().equals(STOPFOREGROUND_ACTION)) {
                stopForeground(true);
                stopSelf();
            }
            startTask(taskConfig);
            return START_STICKY;
        }
        return START_NOT_STICKY;
    }
}