package com.frankandrobot.reminderer.Alarm;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.frankandrobot.reminderer.Database.DatabaseInterface;
import com.frankandrobot.reminderer.Helpers.Logger;
import com.frankandrobot.reminderer.Parser.Task;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {
    private static String TAG = "AlarmReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
	if (Logger.LOGV) {
	    Log.v(TAG, "task rec'd");
	}
	
	// Grab the task from the intent.
	Task task = null;
	final byte[] data = intent
		.getByteArrayExtra(DatabaseInterface.TASK_RAW_DATA);
	if (data != null) {
	    Parcel in = Parcel.obtain();
	    in.unmarshall(data, 0, data.length);
	    in.setDataPosition(0);
	    task = Task.CREATOR.createFromParcel(in);
	}
	if (task == null) {
	    Log.v(TAG, "AlarmReceiver failed to parse the taskfrom the intent");
	    return;
	}
	
	// Intentionally verbose: always log the alarm time to provide useful
	// information in bug reports.
	long now = System.currentTimeMillis();
	SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss.SSS aaa");
	Log.v(TAG, "AlarmReceiver.onReceive() id " + task.getId() + " setFor "
		+ format.format(task.getDateObj()));
	
	// Ignore false alarms caused by timezone changes
	if (now > task.getDateTime() + AlarmConstants.STALE_WINDOW * 1000) {
	    if (Logger.LOGV) {
		Log.v(TAG, "AlarmReceiver ignoring stale alarm");
	    }
	    return;
	}

	// Maintain a cpu wake lock until the AlarmAlert and AlarmKlaxon can
	// pick it up.
	//TODO enable wake lock
	//AlarmAlertWakeLock.acquireCpuWakeLock(context);

	// Close dialogs and window shade
	Intent closeDialogs = new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS);
	context.sendBroadcast(closeDialogs);

	// Decide which activity to start based on the state of the keyguard.
	Class c = AlarmAlertActivity.class;
	KeyguardManager km = (KeyguardManager) context
		.getSystemService(Context.KEYGUARD_SERVICE);
	if (km.inKeyguardRestrictedInputMode()) {
	    // Use the full screen activity for security.
	//    c = AlarmAlertFullScreen.class;
	}

        // launch UI, explicitly stating that this is not due to user action
        // so that the current app's notification management is not disturbed
        Intent alarmAlert = new Intent(context, c);
        alarmAlert.putExtra(AlarmConstants.TASK_INTENT_EXTRA, task);
        alarmAlert.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_NO_USER_ACTION);
        context.startActivity(alarmAlert);

    }

}
