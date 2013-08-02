package com.frankandrobot.reminderer.alarm;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.widget.Button;
import android.widget.SimpleCursorAdapter;

import com.frankandrobot.reminderer.datastructures.Task;

//TODO finish pulling from AlarmAlert
//TODO implement save state for mKilled

public class AlarmAlertActivity extends FragmentActivity
        implements LoaderManager.LoaderCallbacks<Cursor>
{
    private static String TAG = "R:AlarmALert";
    protected boolean mKilled = false;
    protected Button mDismiss, mSnooze;
    Task mTask;
    SimpleCursorAdapter mAdapter;
    // Receives the TASK_ALARM_KILLED action from the AlarmRingerService.
    private BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive(Context context, Intent intent)
        {
            mKilled = true;
            // disable snooze button since can't snooze anymore
            mSnooze.setEnabled(false);
            // alarm alarm =
            // intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
            // if (mAlarm.id == alarm.id) {
            // dismiss(true);
            // }
        }
    };

   /* @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.alarm_activity);

        mTask = getIntent().getParcelableExtra(AlarmConstants.TASK_INTENT_EXTRA);

        if (Logger.LOGV)
        {
            Log.v(TAG, "AlarmAlert launched");
            Log.v(TAG, mTask.toString());
        }

        // setup buttons
        mDismiss = (Button) findViewById(R.id.dismiss);
        mDismiss.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                if (!mKilled)
                { // if alarm is still playing stop it
                    AlarmAlertActivity.this.stopService(new Intent(AlarmConstants.TASK_ALARM_ALERT));
                }
                finish();
            }

        });
        mSnooze = (Button) findViewById(R.id.snooze);

        // setup receiver
        registerReceiver(mReceiver,
                         new IntentFilter(AlarmConstants.TASK_ALARM_KILLED));

        // setup due tasks list view
        mAdapter = new SimpleCursorAdapter(this,
                                           R.layout.main_screen_row,
                                           null,
                                           TaskTable.TASK_ALERT_LISTVIEW_NO_CP,
                                           new int[]{R.id.task_text, R.id.task_due_date});
        ListView lv = (ListView) findViewById(R.id.dueTasks);
        lv.setAdapter(mAdapter);
        getSupportLoaderManager().initLoader(0, null, this);
    }*/

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        this.unregisterReceiver(mReceiver);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1)
    {
        // This is called when a new Loader needs to be created. This
        // activity only has one Loader, so we don't care about the ID.
        /*return TaskDatabaseFacade.getDueAlarmsCursorLoader(this,
                                                           mTask.getTimeInMillis(),
                                                           TaskTable.LTE);
                                                           */
        return null;
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor data)
    {
        // Swap the new cursor in. (The framework will take care of closing the
        // old cursor once we return.)
        mAdapter.changeCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0)
    {
        // This is called when the last Cursor provided to onLoadFinished()
        // above is about to be closed. We need to make sure we are no
        // longer using it.
        mAdapter.changeCursor(null);
    }

}
