package com.frankandrobot.reminderer.Alarm;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.Helpers.Logger;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

//TODO finish pulling from AlarmAlert
//TODO implement save state for mKilled

public class AlarmAlertActivity extends Activity {
    private static String TAG = "Reminderer AlarmALertActivity";

    protected boolean mKilled = false;
    protected Button mDismiss, mSnooze;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.alarm_alert);
	if (Logger.LOGV)
	    Log.v(TAG, "AlarmAlert launched");
	mDismiss = (Button) findViewById(R.id.dismiss);
	mDismiss.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		if (!mKilled) { //if alarm is still playing stop it
		    AlarmAlertActivity.this.stopService(new Intent(
			    AlarmConstants.TASK_ALARM_ALERT));
		}
		finish();
	    }

	});
	mSnooze = (Button) findViewById(R.id.snooze);
	registerReceiver(mReceiver, new IntentFilter(
		AlarmConstants.TASK_ALARM_KILLED));
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

}
