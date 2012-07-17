package com.frankandrobot.reminderer.Alarm;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.Database.DatabaseInterface;
import com.frankandrobot.reminderer.Database.DbColumns;
import com.frankandrobot.reminderer.Helpers.Logger;
import com.frankandrobot.reminderer.Parser.Task;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

//TODO finish pulling from AlarmAlert
//TODO implement save state for mKilled

public class AlarmAlertActivity extends FragmentActivity implements
	LoaderManager.LoaderCallbacks<Cursor> {
    private static String TAG = "R:AlarmALert";

    protected boolean mKilled = false;
    protected Button mDismiss, mSnooze;
    Task mTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.alarm_alert);

	mTask = getIntent().getParcelableExtra(
		AlarmConstants.TASK_INTENT_EXTRA);

	if (Logger.LOGV) {
	    Log.v(TAG, "AlarmAlert launched");
	    Log.v(TAG, mTask.toString());
	}

	// setup buttons
	mDismiss = (Button) findViewById(R.id.dismiss);
	mDismiss.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		if (!mKilled) { // if alarm is still playing stop it
		    AlarmAlertActivity.this.stopService(new Intent(
			    AlarmConstants.TASK_ALARM_ALERT));
		}
		finish();
	    }

	});
	mSnooze = (Button) findViewById(R.id.snooze);

	// setup receiver
	registerReceiver(mReceiver, new IntentFilter(
		AlarmConstants.TASK_ALARM_KILLED));

	// setup due tasks list view
	getSupportLoaderManager().initLoader(0, null, this);
	SimpleCursorAdapter sca = new SimpleCursorAdapter(this,
		R.layout.alarm_alert_row, cr,
		DbColumns.TASK_ALERT_LISTVIEW_NO_CP, new int[] {
			R.id.task_text, R.id.task_due_date });
	ListView lv = (ListView) findViewById(R.id.dueTasks);
	lv.setAdapter(sca);
    }

    @Override
    protected void onDestroy() {
	super.onDestroy();
	this.unregisterReceiver(mReceiver);
    }

    // Receives the TASK_ALARM_KILLED action from the AlarmRingerService.
    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
	@Override
	public void onReceive(Context context, Intent intent) {
	    mKilled = true;
	    // disable snooze button since can't snooze anymore
	    mSnooze.setEnabled(false);
	    // Alarm alarm =
	    // intent.getParcelableExtra(Alarms.ALARM_INTENT_EXTRA);
	    // if (mAlarm.id == alarm.id) {
	    // dismiss(true);
	    // }
	}
    };

    @Override
    public Loader<Cursor> onCreateLoader(int arg0, Bundle arg1) {
	    // This is called when a new Loader needs to be created.  This
	    // activity only has one Loader, so we don't care about the ID.
	    return DatabaseInterface.getDueAlarmsCursorLoader(this, mTask.getDateTimeForDb());
    }

    @Override
    public void onLoadFinished(Loader<Cursor> arg0, Cursor arg1) {
	// TODO Auto-generated method stub
	
    }

    @Override
    public void onLoaderReset(Loader<Cursor> arg0) {
	// TODO Auto-generated method stub
	
    }

}
