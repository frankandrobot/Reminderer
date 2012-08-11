package com.frankandrobot.reminderer;

import com.frankandrobot.reminderer.Database.DatabaseInterface;
import com.frankandrobot.reminderer.Database.DatabaseAsyncService.DatabaseHandler;
import com.frankandrobot.reminderer.Parser.GrammarParser;
import com.frankandrobot.reminderer.Parser.Task;

import android.app.Activity;
import android.content.ContentProviderResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.animation.*;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class AddTaskActivity extends Activity {
    Task mTask;
    Handler mHandler = new AddHandler();
    TextView test;
    EditText test2;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	setContentView(R.layout.add_task);

	Button add = (Button) this.findViewById(R.id.submit);
	add.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		GrammarParser parser = new GrammarParser();
		parser.setAndroidContext(AddTaskActivity.this);
		EditText text = (EditText) findViewById(R.id.add_task);
		String input = text.getText().toString();
		mTask = parser.parse(input);
		String output = mTask.toString();
		TextView log = (TextView) findViewById(R.id.log_cat);
		log.setText(output);
	    }

	});

	Button save = (Button) findViewById(R.id.save);
	save.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		if (mTask != null)
		    DatabaseInterface.addTask(AddTaskActivity.this, mHandler,
			    mTask);

	    }

	});

	Button cancel = (Button) findViewById(R.id.cancel);
	cancel.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		finish();

	    }

	});

	test = (TextView) findViewById(R.id.test);
	test2 = (EditText) findViewById(R.id.test2);
	test.setOnClickListener(new OnClickListener() {
	    
	    @Override
	    public void onClick(View arg0) {
		hideText();
	    }
	});
    }

    private void hideText() {
	// start animationvisibility
	Animation animation = AnimationUtils.loadAnimation(AddTaskActivity.this,
                R.anim.fade_out);
	final LinearLayout parentView = (LinearLayout) findViewById(R.id.addtask_root);
	animation.setAnimationListener(new Animation.AnimationListener() {
	    
	    @Override
	    public void onAnimationEnd(Animation animation) {
		parentView.post(new Runnable() {
	            public void run() {
	                // it works without the runOnUiThread, but all UI updates must 
	                // be done on the UI thread
	                //activity.runOnUiThread(new Runnable() {
	                //    public void run() {
	        	//Animation animation = AnimationUtils.loadAnimation(AddTaskActivity.this, R.anim.quick_hide);
	                //test.startAnimation(animation);
	        	//parentView.removeView(test);
	                //    }
	                }
		});
	    }

	    @Override
	    public void onAnimationRepeat(Animation animation) {
		// TODO Auto-generated method stub
		
	    }

	    @Override
	    public void onAnimationStart(Animation animation) {
		// TODO Auto-generated method stub
		
	    }
	    
	});
	test.startAnimation(animation);
//	try { 
//	    Thread.sleep(100);
//	}
//	catch(Exception e) {}
//	animation = AnimationUtils.loadAnimation(AddTaskActivity.this,
//                R.anim.fade_in);
//	test.startAnimation(animation);
//	test2.setVisibility(View.VISIBLE);

    }
    
    class AddHandler extends DatabaseHandler {

	@Override
	protected void onInsertComplete(int token, Object cookie,
		ContentProviderResult[] result) {
	    Toast.makeText(AddTaskActivity.this, "Task added",
		    Toast.LENGTH_SHORT).show();
	}

    }
}
