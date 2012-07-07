package com.frankandrobot.reminderer.Alarm;

import com.frankandrobot.reminderer.Helpers.Logger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context arg0, Intent arg1) {
	if (Logger.LOGV) {
	    Log.v("AlarmReceiver","task rec'd");
	}
	Toast.makeText(arg0, "Task due", Toast.LENGTH_LONG).show();

    }

}
