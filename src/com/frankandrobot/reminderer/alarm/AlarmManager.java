package com.frankandrobot.reminderer.alarm;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.helpers.Logger;

import static com.frankandrobot.reminderer.database.TaskTable.TaskCol;

/**
 * Convenience methods used by {@link TaskDatabaseFacade}
 */
public class AlarmManager
{
    final private static String TAG = "R:AlarmManager";

    public enum CompareOp
    {
        AFTER(">")
        ,ON_OR_AFTER(">=");

        CompareOp(String val) { this.val = val; }
        private String val;

        @Override
        public String toString() { return val; }
    }

    /**
     * Finds the next task(s) due after the given task and enables them.
     *
     * Idea is that you get a Cursor containing all of the tasks due after
     * the current time. The problem is that there may be more than one task
     * due at the same time. So you get the task in the first row of the
     * Cursor. The time this task is due is the next task due time.
     *
     * __Do NOT call this method directly.__
     *
     * @param context the context
     * @param dueTime find tasks after dueTime
     * @param compareOp one of =, >, or >=
     */
    public long findAndEnableNextTasksDue(Context context,
                                          long dueTime,
                                          CompareOp compareOp)
    {
        // disable the old alarm if any
        disableAlert(context);

        // get all tasks due after curTime
        final long oneMinute = 1000*60;
        Cursor nextAlarms = getDueAlarmIds(context, dueTime, compareOp, dueTime + oneMinute);

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
            int index = nextAlarms.getColumnIndex(TaskCol.TASK_DUE_DATE.colname());
            long nextDueTime = nextAlarms.getLong(index);

            if (Logger.LOGV)
            {
                Log.v(TAG, "dueTime:" + nextDueTime);
                Log.v(TAG, dumpCursor(nextAlarms));
            }

            scheduleAlarm(context, nextDueTime);

            if (nextDueTime < System.currentTimeMillis())
            {
                //we're assuming in the worst case scenario, the db op
                //took a really long time (one minute)
                //we may have to reschedule the next due task as well
                nextAlarms.moveToLast();
                long secondDueTime = nextAlarms.getLong(index);

                if (secondDueTime > nextDueTime)
                {
                    if (Logger.LOGV) Log.v(TAG, "dueTime:" + secondDueTime);

                    scheduleAlarm(context, secondDueTime);
                }

            }
            return nextDueTime;
        }
        return 0;
    }

    private void scheduleAlarm(Context context, long nextDueTime)
    {
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmConstants.TASK_ALARM_ALERT);
        intent.putExtra(AlarmConstants.TASK_DUETIME, nextDueTime);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                                                          0,
                                                          intent,
                                                          PendingIntent.FLAG_CANCEL_CURRENT);

        am.set(android.app.AlarmManager.RTC_WAKEUP, nextDueTime, sender);
    }

    /**
     * Cancels any pending alarms
     *
     * @param context da context
     */
    private void disableAlert(Context context)
    {
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                                                          0,
                                                          new Intent(AlarmConstants.TASK_ALARM_ALERT),
                                                          PendingIntent.FLAG_CANCEL_CURRENT);
        am.cancel(sender);
    }

    /**
     * Gets all alarms due after, before, between dueTime and endTime
     *
     * @param context da contex
     * @param dueTime the time to compare
     * @param compareOp is one of >, >=
     * @param endTime dueTime <= endTime
     * @return the cursor containing the due alarms
     */
    private Cursor getDueAlarmIds(Context context, long dueTime, CompareOp compareOp, long endTime)
    {
        return context.getContentResolver().query(
                TaskProvider.LOAD_DUE_TASKS_URI,
                null,
                compareOp.toString(),
                new String[]{Long.toString(dueTime), Long.toString(endTime)},
                TaskCol.TASK_DUE_DATE.colname());
    }

    /**
     * Convenience method to print out the alarm cursor
     *
     * @param cursor the cursor to dump
     */
    private static String dumpCursor(Cursor cursor)
    {
        if (cursor != null)
        {
            // save row position
            int origPos = cursor.getPosition();
            int index = cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.colname());
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

    public static class PhoneBoot extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            new AlarmManager().findAndEnableNextTasksDue(context,
                                                         System.currentTimeMillis(),
                                                         CompareOp.ON_OR_AFTER);
        }
    }
}
