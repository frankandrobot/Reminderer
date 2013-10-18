package com.frankandrobot.reminderer.alarm;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
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

import org.joda.time.DateTime;

import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.*;
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
     * Finds and enables the next task(s) due after dueTime
     *
     * Usually, dueTime ~ NOW. However, the database op to query
     * tasks may take too long or this method may be called with
     * dueTime in the past (i.e., dueTime < NOW).
     *
     * This method handles that case by querying the next 10 distinct due times.
     * If any of these due times is in the past, it schedules them immediately.
     * This ensures that we never miss any alarms.
     *
     * @param context the context
     * @param dueTime find tasks after dueTime
     * @param compareOp one of >, or >=
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
                nextAlarms.moveToFirst();
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
                    nextAlarms.moveToNext();
                    while(!nextAlarms.isAfterLast())
                    {
                        long secondDueTime = nextAlarms.getLong(index);

                        if (secondDueTime <= System.currentTimeMillis())
                        {
                            if (Logger.LOGV) Log.v(TAG, "dueTime:" + secondDueTime);

                            scheduleAlarm(context, secondDueTime, (int)secondDueTime);
                        }
                        nextAlarms.moveToNext();
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
            Intent newIntent = new Intent(AlarmConstants.GET_NEXT_ALARM_SERVICE);
            newIntent.putExtra("dueTime", System.currentTimeMillis());
            newIntent.putExtra("isPhoneBoot", true);

            context.startService(newIntent);
        }
    }

    /**
     * Gets the next due alarm.
     *
     * This also:
     * - Calculates the next due times for repeating alarms
     * - adds the next alarm to the alarm manager.
     *
     * @note on phone boot, it updates all expired repeating tasks.
     * Otherwise, it just updates repeating tasks that just fired.
     *
     * Intent takes the following params:
     * - dueTime
     * - isPhoneBoot
     *
     */
    public static class GetNextAlarm extends IntentService
    {
        final private long FUDGE_TIME = 1000*60*10; //10 minutes

        public GetNextAlarm()
        {
            super("GetNextAlarm");
        }

        private interface GetRepeatingAlarms
        {
            public Cursor query(long dueTime);
        }

        /**
         * Gets repeating tasks due at dueTime
         */
        GetRepeatingAlarms querySpecificDueTime = new GetRepeatingAlarms() {
            @Override
            public Cursor query(long dueTime) {
                return getContentResolver().query(TaskProvider.TASK_JOIN_REPEAT_URI,
                                                  new TaskTable().getColumns(TASK_ID,
                                                                             TASK_REPEAT_TYPE,
                                                                             REPEAT_NEXT_DUE_DATE),
                                                  REPEAT_NEXT_DUE_DATE+"="+dueTime,
                                                  null,
                                                  null);
            }
        };

        /**
         * Gets expired repeating tasks with FUDGE_TIME to account for
         * this service taking extra long time
         */
        GetRepeatingAlarms queryExpiredRepeatingAlarms = new GetRepeatingAlarms() {
            @Override
            public Cursor query(long dueTime) {
                return getContentResolver().query(TaskProvider.TASK_JOIN_REPEAT_URI,
                                                  new TaskTable().getColumns(TASK_ID,
                                                                             TASK_REPEAT_TYPE,
                                                                             REPEAT_NEXT_DUE_DATE),
                                                  REPEAT_NEXT_DUE_DATE+"<="+dueTime+FUDGE_TIME,
                                                  null,
                                                  null);
            }
        };

        @Override
        protected void onHandleIntent(Intent intent)
        {
            final long dueTime = intent.getLongExtra("dueTime", 0);
            final boolean isPhoneBoot = intent.getBooleanExtra("isPhoneBoot", false);

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
                                getNextAlarm(dueTime, isPhoneBoot);
                            }
                            finally
                            {
                                if (!isPhoneBoot) AlarmAlertWakeLock.getInstance().releaseCpuLock(1);
                            }
                        }
                    }
                }).start();
            }

        }

        protected void getNextAlarm(long dueTime, boolean isPhoneBoot)
        {
            if (Logger.LOGD)
            {
                Log.d(TAG, "dueTime: "+new DateTime(dueTime));
            }
            //first calculate the next alarm due so we don't fall behind
            long nextAlarmDate = new AlarmManager().findAndEnableNextTasksDue(getApplicationContext(),
                                                                              dueTime,
                                                                              CompareOp.AFTER);

            //get repeating tasks that are either expired or due at dueTime
            GetRepeatingAlarms getRepeatingAlarms = isPhoneBoot
                                                    ? queryExpiredRepeatingAlarms : querySpecificDueTime;
            Cursor cursor = getRepeatingAlarms.query(dueTime);

            boolean isRecalculateNextAlarm = false;

            //for each repeating task, find the next due date and save it to db
            //if any of these are before current alarm, recalculate the next alarm due
            if (cursor != null && cursor.getCount() > 0)
            {
                cursor.moveToFirst();
                while(!cursor.isAfterLast())
                {
                    long taskId = cursor.getLong(0);
                    Type repeatType = Type.toType(cursor.getInt(1));
                    long dueDate = cursor.getLong(2);
                    long nextDueDate = Task.calculateNextDueDate(repeatType, dueDate);
                    //recall that #findAndEnableNextTasksDue returns 0 when there
                    //are no due tasks, so automatically recalculate next alarm
                    //when this happens
                    isRecalculateNextAlarm = isRecalculateNextAlarm
                                             || nextAlarmDate == 0
                                             || nextDueDate < nextAlarmDate;
                    //does this time exist in the db already?
                    Cursor dupCursor = getContentResolver().query(TaskProvider.REPEAT_URI,
                                                                  new String[]{REPEAT_ID.colname()},
                                                                  REPEAT_TASK_ID_FK+"="+taskId+
                                                                  " AND "+REPEAT_NEXT_DUE_DATE+"="+nextDueDate,
                                                                  null,
                                                                  null);
                    if (dupCursor != null && dupCursor.getCount() == 0)
                    {
                        ContentValues values = new ContentValues();
                        values.put(REPEAT_TASK_ID_FK.colname(), taskId);
                        values.put(REPEAT_NEXT_DUE_DATE.colname(), nextDueDate);
                        getContentResolver().insert(TaskProvider.REPEAT_URI, values);
                    }
                    cursor.moveToNext();

                    if (Logger.LOGD)
                    {
                        Log.d(TAG, "next due date: "+new DateTime(nextDueDate));
                    }
                }
            }

            if (isRecalculateNextAlarm)
                new AlarmManager().findAndEnableNextTasksDue(getApplicationContext(),
                                                             dueTime,
                                                             isPhoneBoot ? CompareOp.ON_OR_AFTER : CompareOp.AFTER);

        }
    }
}
