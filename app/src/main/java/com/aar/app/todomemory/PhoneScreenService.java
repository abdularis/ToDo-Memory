package com.aar.app.todomemory;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;

public class PhoneScreenService extends Service {

    private static boolean sIsRunning = false;

    private PhoneScreenEventReceiver mReceiver;

    public static boolean isRunning() {
        return sIsRunning;
    }

    public PhoneScreenService() {
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        mReceiver = new PhoneScreenEventReceiver();
        registerReceiver(mReceiver, intentFilter);

        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, i, 0);
        Notification notification = new Notification.Builder(this)
                .setContentText(getText(R.string.notif_content_text))
                .setContentTitle(getText(R.string.app_name))
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.mipmap.ic_launcher)
                .getNotification();

        startForeground(123, notification);
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        sIsRunning = true;

        if (intent.hasExtra(PhoneScreenEventReceiver.EXTRA_SCREEN_STATE)) {
            boolean screenOn = intent.getBooleanExtra(PhoneScreenEventReceiver.EXTRA_SCREEN_STATE, false);

            if (!screenOn) {
                runMainActivity();
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        sIsRunning = false;
        if (mReceiver != null) {
            unregisterReceiver(mReceiver);
        }
    }

    private void runMainActivity() {
        Intent i = new Intent(getApplicationContext(), MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP|Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
