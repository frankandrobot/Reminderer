package com.frankandrobot.reminderer.database;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Handler;
import android.os.Parcel;
import android.support.v4.content.Loader;
import android.util.Log;

import com.frankandrobot.reminderer.alarm.AlarmConstants;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.Calendar;

/**
 * The app-specific interface to the database
 *
 */
public class TaskDatabaseFacade
{
    static private String TAG = "R:DbInterface";

    private static ContentValues addToContentValues(Task task)
    {
        ContentValues values = new ContentValues();
        values.put(DbColumns.TASK_DUE_DATE, task.getTimeInMillis());
        values.put(DbColumns.TASK, task.getTaskDesc());
        return values;
    }

    public static void addTask(final Context context,
                               Handler handler,
                               final Task task)
    {
        if (Logger.LOGV)
        {
            Log.v(TAG, "Saving task:\n" + task.toString());
            Log.v(TAG,
                  "task,time:" + task.getTaskDesc() + " "
                          + task.getTimeInMillis());
        }
        // create content values from Task object
        ContentValues values = addToContentValues(task);
        // add content values to db
        Runnable postOp = new Runnable()
        {

            @Override
            public void run()
            {
                findNextAlarm(context, task);
            }

        };
        DatabaseAsyncService.startInsert(context, handler, values, postOp);
    }

    public static void findNextAlarm(Context context, Task task)
    {
        // Idea is that you get a Cursor containing all of the tasks due after
        // the current time. The problem is that there may be more than one task
        // due at the same time. So you get the task in the first row of the
        // Cursor. Then you get all tasks due at the same time as that task
        // TODO rewrite using Cursor groups
        // TODO make this asynchronous

        // disable the old alarm if any
        disableAlert(context);
        // get all tasks due after curTime
        long curTime = System.currentTimeMillis();
        Cursor nextAlarms = getDueAlarms(context, curTime, DbColumns.GTE);
        if (nextAlarms == null)
        {
            Log.d(TAG, "Error occurred. No upcoming tasks due");
            return;
        }
        else if (nextAlarms.getCount() == 0)
        {
            if (Logger.LOGV)
                Log.v(TAG, "No upcoming tasks due");
            return;
        }
        // get the task in the first row
        nextAlarms.moveToNext(); // row pointer starts at -1
        int index = nextAlarms.getColumnIndex(DbColumns.TASK_DUE_DATE);
        long dueTime = nextAlarms.getLong(index);
        if (Logger.LOGV)
        {
            Log.v(TAG, "dueTime:" + dueTime);
            Log.v(TAG, dumpCursor(nextAlarms));
        }
        // get all tasks due at dueTime
        nextAlarms.close(); // close previous
        nextAlarms = getDueAlarms(context, dueTime, DbColumns.EQ);
        if (nextAlarms == null)
        {
            Log.d(TAG, "Something went wrong. nextAlarms should not be null");
            return;
        }
        // get ids of these tasks
        long ids[] = new long[nextAlarms.getCount()];
        index = nextAlarms.getColumnIndex(DbColumns.TASK_ID);
        int len = 0;
        while (nextAlarms.moveToNext())
        {
            ids[len++] = nextAlarms.getLong(index);
        }
        nextAlarms.close();

        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        if (Logger.LOGV)
        {
            android.util.Log.v(TAG, "** setAlert id " + task.getId()
                    + " atTime " + task.get(Task.Task_Calendar.class).getLocaleTime());
        }
        Intent intent = new Intent(AlarmConstants.TASK_ALARM_ALERT);
        Parcel out = Parcel.obtain();
        task.writeToParcel(out, 0);
        out.setDataPosition(0);
        // TODO remove task from intent
        intent.putExtra(AlarmConstants.TASK_RAW_DATA, out.marshall());
        intent.putExtra(AlarmConstants.TASK_ID_DATA, ids);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0, intent,
                                                          PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(AlarmManager.RTC_WAKEUP, dueTime, sender);

    }

    /**
     * DEPRECATED - use the Loader<Cursor> interface for asynchronous calls
     * <p/>
     * Gets the tasks that are due a given time
     *
     * @param context
     * @param time
     * @param op
     * @return
     */
    public static Cursor getDueAlarms(Context context, Calendar time, String op)
    {
        return getDueAlarms(context, time.getTimeInMillis(), op);
    }

    /**
     * DEPRECATED - use the Loader<Cursor> interface for asynchronous calls
     *
     * @param context
     * @param time
     * @param op
     * @return
     */
    public static Cursor getDueAlarms(Context context, long time, String op)
    {
        Cursor mResult = context.getContentResolver().query(
                DbColumns.CONTENT_URI, DbColumns.TASK_ALERT_LISTVIEW_CP,
                DbColumns.TASK_DUE_DATE + op + "?",
                new String[]{Long.toString(time)}, DbColumns.DEFAULT_SORT);
        return mResult;
    }

    /**
     * Get a cursors containing all of the tasks with due_date OP time where OP
     * is one of <=, =, or >=
     *
     * @param context
     * @param time
     * @param OP      - one of <=, =, or >=
     * @return
     */
    public static Loader<Cursor> getDueAlarmsCursorLoader(Context context,
                                                          long time, String OP)
    {
        return new android.support.v4.content.CursorLoader(context,
                                                           DbColumns.CONTENT_URI,
                                                           DbColumns.TASK_ALERT_LISTVIEW_CP,
                                                           DbColumns.TASK_DUE_DATE + OP + "?",
                                                           new String[]{Long.toString(
                                                                   time)},
                                                           DbColumns.DEFAULT_SORT);
    }

    /**
     * Convenience method to print out the alarm cursor
     *
     * @param cursor
     */
    private static String dumpCursor(Cursor cursor)
    {
        if (cursor == null)
            return "";
        // save row position
        int origPos = cursor.getPosition();
        int index = cursor.getColumnIndex(DbColumns.TASK_DUE_DATE);
        cursor.moveToFirst();
        String row = "*****Cursor*****\n";
        while (cursor.moveToNext())
        {
            row += cursor.getPosition() + ":" + cursor.getLong(index);
        }
        // restore cursor
        cursor.moveToPosition(origPos);
        return row;
    }

    /**
     * Cancels any pending alarms
     *
     * @param context
     */
    private static void disableAlert(Context context)
    {
        AlarmManager am = (AlarmManager) context
                .getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context, 0,
                                                          new Intent(
                                                                  AlarmConstants.TASK_ALARM_ALERT),
                                                          PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

}
