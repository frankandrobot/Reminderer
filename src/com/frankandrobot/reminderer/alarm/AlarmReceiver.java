package com.frankandrobot.reminderer.alarm;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.frankandrobot.reminderer.helpers.Logger;

import java.text.SimpleDateFormat;

public class AlarmReceiver extends BroadcastReceiver
{
    private static String TAG = "R:AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (Logger.LOGV)
        {
            Log.v(TAG, "task rec'd");
        }

        // Intentionally verbose: always log the alarm time to provide useful
        // information in bug reports.
        long now = System.currentTimeMillis();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS aaa");
        Log.v(TAG,
              String.format("alarm rec'd at %s", sdf.format(now)));

        // Grab the due time from the intent
        long dueTime = intent.getLongExtra(AlarmConstants.TASK_DUETIME, 0);
        if (dueTime != 0)
        {
            // Close dialogs and window shade
            Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
            context.sendBroadcast(closeDialogs);

            // Decide which activity to start based on the state of the keyguard.
            Class c = AlarmActivity.class;
            KeyguardManager km = (KeyguardManager) context.getSystemService(Context.KEYGUARD_SERVICE);
            if (km.inKeyguardRestrictedInputMode())
            {
                Log.v(TAG, "wuz in keyguard mode");
                // Use the full screen activity for security.
                //    c = AlarmAlertFullScreen.class;
            }

            // launch UI, explicitly stating that this is not due to user action
            // so that the current app's notification management is not disturbed
            Intent alarmAlert = new Intent(context, c);
            alarmAlert.putExtra(AlarmConstants.TASK_DUETIME, dueTime);
            alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
            context.startActivity(alarmAlert);

            // Play the alarm alert and vibrate the device.
            Intent playAlarm = new Intent(AlarmConstants.TASK_ALARM_ALERT);
            context.startService(playAlarm);

            //grab a wake lock so the service can play the alarm
            AlarmAlertWakeLock.getInstance().acquireCpuWakeLock(context);

            //get the next alarm
            new AlarmManager().findAndEnableNextTasksDue(context, dueTime + (long) 1);
        }
    }

}
