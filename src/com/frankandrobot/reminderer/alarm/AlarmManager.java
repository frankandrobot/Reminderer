package com.frankandrobot.reminderer.alarm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentProvider;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.parser.GrammarRule;

import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.*;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.*;
import static com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.*;

/**
 * Convenience methods used by {@link TaskDatabaseFacade}
 */
public class AlarmManager
{
    final private static String TAG = "R:AlarmManager";
    //a real lock object isn't needed
    //we just need to lock across all instances calling the method
    final private static Object lock = new Object();

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
        synchronized (lock)
        {
            // disable the old alarm if any
            disableAlert(context);

            // get all tasks due after curTime
            // this is potentially expensive
            Cursor nextAlarms = getDueAlarmIds(context, dueTime, compareOp);
            // might be NOW + 1 minute later

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
                int index = nextAlarms.getColumnIndex(TASK_DUE_DATE.colname());
                long nextDueTime = nextAlarms.getLong(index);

                if (Logger.LOGV)
                {
                    Log.v(TAG, "dueTime:" + nextDueTime);
                    Log.v(TAG, dumpCursor(nextAlarms));
                }

                //if the db took a really long time, this may fire immediately
                scheduleAlarm(context, nextDueTime, (int)nextDueTime);

                if (nextDueTime <= System.currentTimeMillis())
                {
                    //we're assuming in the worst case scenario, the db op
                    //took a really long time (one minute)
                    //we may have to reschedule the next due task as well
                    nextAlarms.moveToLast();
                    long secondDueTime = nextAlarms.getLong(index);

                    if (secondDueTime <= System.currentTimeMillis())
                    {
                        if (Logger.LOGV) Log.v(TAG, "dueTime:" + secondDueTime);

                        scheduleAlarm(context, secondDueTime, (int)secondDueTime);
                    }

                }
                return nextDueTime;
            }
            return 0;
        }
    }

    /**
     * Schedule the alarm using the AlarmManager
     *
     * @param context
     * @param nextDueTime
     * @param pendingIntentId use different IDs to register multiple intents
     */
    private void scheduleAlarm(Context context, long nextDueTime, int pendingIntentId)
    {
        android.app.AlarmManager am = (android.app.AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent intent = new Intent(AlarmConstants.TASK_ALARM_ALERT);
        intent.putExtra(AlarmConstants.TASK_DUETIME, nextDueTime);
        PendingIntent sender = PendingIntent.getBroadcast(context,
                                                          pendingIntentId,
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
     * Gets all alarms due after, before dueTime
     *
     * @param context da contex
     * @param dueTime the time to compare
     * @param compareOp is one of >, >=
     * @return the cursor containing the due alarms
     */
    private Cursor getDueAlarmIds(Context context, long dueTime, CompareOp compareOp)
    {
        return context.getContentResolver().query(
                TaskProvider.LOAD_DUE_TIMES_URI,
                null,
                compareOp.toString(),
                new String[]{Long.toString(dueTime)},
                TASK_DUE_DATE.colname());
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
            int index = cursor.getColumnIndex(TASK_DUE_DATE.colname());
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

    public static class GetNextAlarm extends IntentService
    {
        public GetNextAlarm(String name)
        {
            super(name);
        }

        @Override
        protected void onHandleIntent(Intent intent)
        {
            final long dueTime = intent.getLongExtra("dueTime", 0);
            if (dueTime > 0)
            {
                new Thread(new Runnable() {
                    @Override
                    public void run()
                    {
                        //ensure this code is called only one at a time
                        synchronized (lock)
                        {
                            try
                            {
                                getNextAlarm(dueTime);
                            }
                            finally
                            {
                                AlarmAlertWakeLock.getInstance().releaseCpuLock(1);
                            }
                        }
                    }
                }).start();
            }

        }

        private void getNextAlarm(long dueTime)
        {
            //first calculate the next alarm due so we don't fall behind
            long nextAlarmDate = new AlarmManager().findAndEnableNextTasksDue(getApplicationContext(),
                                                                              dueTime,
                                                                              CompareOp.AFTER);

            //get repeating tasks (if any) with this due time and calculate next repeating time
            ContentResolver resolver = getContentResolver();
            Cursor cursor = resolver.query(TaskProvider.TASK_JOIN_REPEAT_URI,
                    new TaskTable().getColumns(TASK_ID,
                                               TASK_REPEAT_TYPE,
                                               REPEAT_NEXT_DUE_DATE),
                    REPEAT_NEXT_DUE_DATE+"="+dueTime,
                    null,
                    null);

            boolean isRecalculateNextAlarm = false;
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                while(!cursor.isAfterLast())
                {
                    long taskId = cursor.getLong(0);
                    Type repeatType = Type.toType(cursor.getInt(1));
                    long dueDate = cursor.getLong(2);
                    long nextDueDate = Task.calculateNextDueDate(repeatType, dueDate);
                    isRecalculateNextAlarm = isRecalculateNextAlarm || nextDueDate < nextAlarmDate;
                    ContentValues values = new ContentValues();
                    values.put(REPEAT_TASK_ID_FK.colname(), taskId);
                    values.put(REPEAT_NEXT_DUE_DATE.colname(), nextDueDate);
                    resolver.insert(TaskProvider.REPEAT_URI, values);
                }
            }

            if (isRecalculateNextAlarm)
                new AlarmManager().findAndEnableNextTasksDue(getApplicationContext(),
                                                             dueTime,
                                                             CompareOp.AFTER);

        }
    }
}
