package com.frankandrobot.reminderer.Database;

import java.util.Calendar;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
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
    static private String TAG = "R:DbInterface";

    public static void addTask(Context context, Task task) {
	if (Logger.LOGV) {
	    Log.v(TAG, "Saving task:\n" + task.toString());
	    Log.v(TAG, "task,time:"+task.getTaskForDb()+" "+task.getDateTimeForDb());
	}
	ContentResolver resolver = context.getContentResolver();
	// create content values from Task object
	ContentValues values = new ContentValues();
	values.put(DbColumns.TASK_DUE_DATE, task.getDateTimeForDb());
	values.put(DbColumns.TASK, task.getTaskForDb());
	// add content values to db
	Uri uri = resolver.insert(DbColumns.CONTENT_URI, values);
	// get id
        String segment = uri.getPathSegments().get(1);
        int newId = Integer.parseInt(segment);
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

	Intent intent = new Intent(AlarmConstants.TASK_ALARM_ALERT);

	Parcel out = Parcel.obtain();
	task.writeToParcel(out, 0);
	out.setDataPosition(0);
	intent.putExtra(AlarmConstants.TASK_RAW_DATA, out.marshall());

	PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
		PendingIntent.FLAG_CANCEL_CURRENT);

	am.set(AlarmManager.RTC_WAKEUP, task.getDateTime(), sender);

    }

    /**
     * Gets the tasks that are due a given time 
     * 
     * @param context
     * @param time
     * @return
     */
    public static Cursor getDueAlarms(Context context, Calendar time) {
	return getDueAlarms(context, time.getTimeInMillis());
    }

    public static Cursor getDueAlarms(Context context, long time) {
	//TODO use async cursor loader
	Cursor mResult = 
		context.getContentResolver().query(DbColumns.CONTENT_URI,
						   DbColumns.TASK_ALERT_LISTVIEW_CP, 
						   DbColumns.TASK_DUE_DATE+"<=?",
						   new String[]{Long.toString(time)}, 
						   DbColumns.DEFAULT_SORT);
	return mResult;
    }
}
