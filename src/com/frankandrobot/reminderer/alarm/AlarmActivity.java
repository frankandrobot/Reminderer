package com.frankandrobot.reminderer.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.helpers.Logger;

/**
 * Alarm Clock alarm alert: pops visible indicator and plays alarm
 * tone. This activity is the full screen version which shows over the lock
 * screen with the wallpaper as the background.
 */
public class AlarmActivity extends FragmentActivity
{
    private static final String TAG = "R:AlarmActivity";

    protected static final String SCREEN_OFF = "screen_off";
    // These defaults must match the values in res/xml/settings.xml
    private static final String DEFAULT_SNOOZE = "10";
    private static final String DEFAULT_VOLUME_BEHAVIOR = "2";

    private int mVolumeBehavior;

    // Receives the ALARM_KILLED action from the AlarmKlaxon.
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            /*Alarm alarm = intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            if (alarm != null && mAlarm.id == alarm.id)
            {
                dismiss(true);
            }*/
        }
    };

    @Override
    protected void onCreate(Bundle icicle)
    {
        super.onCreate(icicle);

        requestWindowFeature(android.view.Window.FEATURE_NO_TITLE);

        final Window win = getWindow();
        win.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                     | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                     | WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                     | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        updateLayout(getIntent());

        Button dismiss = (Button) findViewById(id.dimiss_button);
        dismiss.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                dismiss(false);
            }
        });
        // Register to get the alarm killed intent.
        //registerReceiver(mReceiver, new IntentFilter(Alarms.ALARM_KILLED));
    }

    private void updateLayout(Intent intent)
    {
        setContentView(R.layout.alarm_activity);

        long dueTime = intent.getLongExtra(AlarmConstants.TASK_DUETIME, 0);

        setDueTime(dueTime);
    }

    private void setDueTime(long dueTime)
    {
        AlarmDueListFragment fragment = (AlarmDueListFragment) getSupportFragmentManager()
                                                             .findFragmentById(R.id.alarm_duelist);
        fragment.setDueTime(dueTime);

    }

 /*   // Attempt to snooze this alert.
    private void snooze()
    {
        final String snooze = PreferenceManager.getDefaultSharedPreferences(this).getString(SettingsActivity.KEY_ALARM_SNOOZE,
                                                                                            DEFAULT_SNOOZE);
        int snoozeMinutes = Integer.parseInt(snooze);

        final long snoozeTime = System.currentTimeMillis() + (1000 * 60 * snoozeMinutes);
        Alarms.saveSnoozeAlert(AlarmAlertFullScreen.this,
                               mAlarm.id,
                               snoozeTime);

        // Get the display time for the snooze and update the notification.
        final Calendar c = Calendar.getInstance();
        c.setTimeInMillis(snoozeTime);

        // Append (snoozed) to the label.
        String label = mAlarm.getLabelOrDefault(this);
        label = getString(R.string.alarm_notify_snooze_label, label);

        // Notify the user that the alarm has been snoozed.
        Intent cancelSnooze = new Intent(this, AlarmReceiver.class);
        cancelSnooze.setAction(Alarms.CANCEL_SNOOZE);
        cancelSnooze.putExtra(Alarms.ALARM_ID, mAlarm.id);
        PendingIntent broadcast = PendingIntent.getBroadcast(this,
                                                             mAlarm.id,
                                                             cancelSnooze,
                                                             0);
        NotificationManager nm = getNotificationManager();
        Notification n = new Notification(R.drawable.stat_notify_alarm,
                                          label,
                                          0);
        n.setLatestEventInfo(this,
                             label,
                             getString(R.string.alarm_notify_snooze_text,
                                       Alarms.formatTime(this, c)),
                             broadcast);
        n.flags |= Notification.FLAG_AUTO_CANCEL | Notification.FLAG_ONGOING_EVENT;
        nm.notify(mAlarm.id, n);

        String displayTime = getString(R.string.alarm_alert_snooze_set,
                                       snoozeMinutes);
        // Intentionally log the snooze time for debugging.
        Log.v(displayTime);

        // Display the snooze minutes in a toast.
        Toast.makeText(AlarmAlertFullScreen.this,
                       displayTime,
                       Toast.LENGTH_LONG).show();
        stopService(new Intent(Alarms.ALARM_ALERT_ACTION));
        finish();
    }

    private NotificationManager getNotificationManager()
    {
        return (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
    }
*/
    // Dismiss the alarm.
    private void dismiss(boolean killed)
    {
        // The service told us that the alarm has been killed, do not modify
        // the notification or stop the service.
        if (!killed)
        {
/*
            // Cancel the notification and stop playing the alarm
            NotificationManager nm = getNotificationManager();
            nm.cancel(mAlarm.id);
*/
            stopService(new Intent(AlarmConstants.TASK_ALARM_ALERT));
        }
        finish();
    }

    /**
     * this is called when a second alarm is triggered while a
     * previous alert window is still active.
     */
    @Override
    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);

        if (Logger.LOGV) Log.v(TAG, "onNewIntent()");

        long dueTime = intent.getLongExtra(AlarmConstants.TASK_DUETIME, 0);

        setDueTime(dueTime);
    }

    /**
    @Override
    protected void onStop()
    {
        super.onStop();
        if (!isFinishing())
        {
            // Don't hang around.
            finish();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        if (Log.LOGV) Log.v("AlarmAlert.onDestroy()");
        // No longer care about the alarm being killed.
        unregisterReceiver(mReceiver);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event)
    {
        // Do this on key down to handle a few of the system keys.
        boolean up = event.getAction() == KeyEvent.ACTION_UP;
        switch (event.getKeyCode())
        {
            // Volume keys and camera keys dismiss the alarm
            case KeyEvent.KEYCODE_VOLUME_UP:
            case KeyEvent.KEYCODE_VOLUME_DOWN:
            case KeyEvent.KEYCODE_CAMERA:
            case KeyEvent.KEYCODE_FOCUS:
                if (up)
                {
                    switch (mVolumeBehavior)
                    {
                        case 1:
                            snooze();
                            break;

                        case 2:
                            dismiss(false);
                            break;

                        default:
                            break;
                    }
                }
                return true;
            default:
                break;
        }
        return super.dispatchKeyEvent(event);
    }

    @Override
    public void onBackPressed()
    {
        // Don't allow back to dismiss. This method is overriden by AlarmAlert
        // so that the dialog is dismissed.
        return;
    }*/
}