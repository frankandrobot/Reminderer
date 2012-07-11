package com.frankandrobot.reminderer.Alarm;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.Helpers.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class AlarmAlertActivity extends Activity {
    private static String TAG="Reminderer AlarmALertActivity";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.alarm_alert);
	if (Logger.LOGV)
	    Log.v(TAG,"AlarmAlert launched");
    }

}
