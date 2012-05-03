package com.frankandrobot.reminderer;

import com.frankandrobot.reminderer.Parser.MyDateFormat;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;

public class RemindererActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.main);
	stringTester("June 2, 2003<--this should work");
	stringTester("6/2<--this should work");
	stringTester("Jun 2, 2003<---this should work");
	stringTester("Jun 2<---this should work");
	stringTester("hello June 2<---this should fail");
    }
    
    private void stringTester(final String string) {
	MyDateFormat form = new MyDateFormat(this);
	String[] date = form.parse(string);
	if (date == null)
	    Log.d("R", "date is null");
	else
	    Log.d("R", date[0] + " " + date[1]);
    }
}