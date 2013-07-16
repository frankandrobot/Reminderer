package com.frankandrobot.reminderer.alarm;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.helpers.Logger;

import static com.frankandrobot.reminderer.database.TaskTable.DEFAULT_SORT;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol;

/**
 * Convenience methods used by {@link TaskDatabaseFacade}
 */
public class AlarmHelper
{
    final private static String TAG = "R:AlarmHelper";

    /**
     * Finds the next task(s) due after the given task and enables them.
     *
     * Idea is that you get a Cursor containing all of the tasks due after
     * the current time. The problem is that there may be more than one task
     * due at the same time. So you get the task in the first row of the
     * Cursor. The time this task is due is the next task due time.
     *
     * @param context
     * @param dueTime
     */
    public void findAndEnableNextTasksDue(Context context, long dueTime)
    {
        // disable the old alarm if any
        disableAlert(context);

        // get all tasks due after curTime
        Cursor nextAlarms = getDueAlarmIds(context, dueTime, ">=");

        if (nextAlarms == null)
        {
            Log.e(TAG, "Error occurred. Couldn't query db for upcoming due tasks.");
        }
        else if (nextAlarms.getCount() == 0 && Logger.LOGV)
        {
            Log.v(TAG, "No upcoming tasks due");
        }
        else
        {
            // get the task in the first row (row pointer starts at -1)
            nextAlarms.moveToNext();
            int index = nextAlarms.getColumnIndex(TaskCol.TASK_DUE_DATE.toString());
            long nextDueTime = nextAlarms.getLong(index);

            if (Logger.LOGV)
            {
                Log.v(TAG, "dueTime:" + nextDueTime);
                Log.v(TAG, dumpCursor(nextAlarms));
            }

            AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

            Intent intent = new Intent(AlarmConstants.TASK_ALARM_ALERT);
            intent.putExtra(AlarmConstants.INTENT_NEXT_TASK_DUETIME, nextDueTime);
            PendingIntent sender = PendingIntent.getBroadcast(context,
                                                              0,
                                                              intent,
                                                              PendingIntent.FLAG_CANCEL_CURRENT);

            am.set(AlarmManager.RTC_WAKEUP, nextDueTime, sender);
        }
    }

    /**
     * Cancels any pending alarms
     *
     * @param context
     */
    private void disableAlert(Context context)
    {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                                                          0,
                                                          new Intent(AlarmConstants.TASK_ALARM_ALERT),
                                                          PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

    /**
     * Gets all alarms due after given time
     *
     * @param context
     * @param dueTime
     * @param op is one of =, <=, >=
     * @return
     */
    private Cursor getDueAlarmIds(Context context, long dueTime, String op)
    {
        Cursor mResult = context.getContentResolver().query(
                TaskProvider.CONTENT_URI,
                new String[]{TaskCol.TASK_ID.toString(), TaskCol.TASK_DUE_DATE.toString()},
                TaskCol.TASK_DUE_DATE + op + "?",
                new String[]{Long.toString(dueTime)},
                DEFAULT_SORT);
        return mResult;
    }

    /**
     * Convenience method to print out the alarm cursor
     *
     * @param cursor
     */
    private static String dumpCursor(Cursor cursor)
    {
        if (cursor != null)
        {
            // save row position
            int origPos = cursor.getPosition();
            int index = cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.toString());
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
        return "";
    }
}
