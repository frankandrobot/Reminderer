package com.frankandrobot.reminderer.Database;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.os.Parcel;
import android.util.Log;

import com.frankandrobot.reminderer.Alarm.AlarmConstants;
import com.frankandrobot.reminderer.Helpers.Logger;
import com.frankandrobot.reminderer.Parser.Task;

/**
 * This is the app-specific interface to the database
 * 
 * @author uri
 * 
 */
public class DatabaseInterface {
    static private String TAG = "Reminderer DBinterface";

    public static void addTask(Context context, Task task) {
	// TODO add task to db - use the ContentProvider/ContentResolver;
	// ContentResolver resolver = context.getContentResolver();
	// resolver.update()
	if (Logger.LOGV) {
	    Log.v("addTask", "Saving task:\n" + task.toString());
	}
	// add task to alarm manager
	findNextAlarm(context, task);
    }

    public static void findNextAlarm(Context context, Task task) {
	// TODO query db to find the next task due
	// then pass this task to the AlarmManager
	// what it's doing now is passing the current task

	AlarmManager am = (AlarmManager) context
		.getSystemService(Context.ALARM_SERVICE);

	if (Logger.LOGV) {
	    android.util.Log.v(TAG, "** setAlert id " + task.getId()
		    + " atTime " + task.getLocaleTime());
	}

	Intent intent = new Intent(AlarmConstants.TASK_ALARM);

	Parcel out = Parcel.obtain();
	task.writeToParcel(out, 0);
	out.setDataPosition(0);
	intent.putExtra(AlarmConstants.TASK_RAW_DATA, out.marshall());

	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
		PendingIntent.FLAG_CANCEL_CURRENT);

	am.set(AlarmManager.RTC_WAKEUP, task.getDateTime(), sender);

    }
}
