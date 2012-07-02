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

import com.frankandrobot.reminderer.Helpers.Logger;
import com.frankandrobot.reminderer.Parser.Task;

/**
 * This is the app-specific interface to the database
 * 
 * @author uri
 * 
 */
public class DatabaseInterface {

    // This action triggers the AlarmReceiver as well as the AlarmRinger. It
    // is a public action used in the manifest for receiving Alarm broadcasts
    // from the alarm manager.
    public static final String TASK_ALARM = "com.frankandrobot.remimderer.TASK_ALARM";

    // This extra is the raw Alarm object data. It is used in the
    // AlarmManagerService to avoid a ClassNotFoundException when filling in
    // the Intent extras.
    public static final String TASK_RAW_DATA = "intent.extra.task_raw";
    
    public static void addTask(Context context, Task task) {
	// TODO add task to db - use the ContentProvider/ContentResolver;
	// ContentResolver resolver = context.getContentResolver();
	// resolver.update()
	
	// add task to alarm manager
	findNextAlarm(context, task);
    }
    
    public static void findNextAlarm(Context context, Task task) {
	//TODO query db to find the next task due
	//then pass this task to the AlarmManager
	//what it's doing now is passing the current task
	
	AlarmManager am = (AlarmManager)
                context.getSystemService(Context.ALARM_SERVICE);

        if (Logger.LOGV) {
          //  android.util.Log.v("** setAlert id " + alarm.id + " atTime " + atTimeInMillis);
        }

        Intent intent = new Intent(TASK_ALARM);

        Parcel out = Parcel.obtain();
        task.writeToParcel(out, 0);
        out.setDataPosition(0);
        intent.putExtra(TASK_RAW_DATA, out.marshall());

        PendingIntent sender = PendingIntent.getBroadcast(
                context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, task.getDateForDb(), sender);


    }
}
