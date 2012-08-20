package com.frankandrobot.reminderer;

import java.util.Calendar;
import java.util.GregorianCalendar;

import com.frankandrobot.reminderer.Database.DatabaseInterface;
import com.frankandrobot.reminderer.Database.DatabaseAsyncService.DatabaseHandler;
import com.frankandrobot.reminderer.Parser.GrammarParser;
import com.frankandrobot.reminderer.Parser.Task;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentProviderResult;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.animation.*;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

//TODO remove title

public class AddTaskActivity extends Activity {
    private static String TAG = "AddTask";
    private Task mTask;
    private Handler mHandler = new AddHandler();
    private final static int DATE_PICKER = 0;
    private final static int TIME_PICKER = 1;

    // variables to save when pausing or stopping activity
    boolean isAddTaskBoxEmpty = true; // technicall the add task box is never
				      // empty. This helps keep track of when
				      // the user actually entered something

    @Override
    public void onCreate(Bundle savedInstanceState) {
	super.onCreate(savedInstanceState);
	requestWindowFeature(Window.FEATURE_NO_TITLE);
	setContentView(R.layout.add_task);

	EditText addTaskBox = (EditText) findViewById(R.id.add_task);
	addTaskBox.setOnFocusChangeListener(new OnFocusChangeListener() {

	    @Override
	    public void onFocusChange(View text, boolean focus) {
		EditText textBox = (EditText) text;
		// if gaining focus and here for the first time then clear text
		// box
		if (focus && isAddTaskBoxEmpty) {
		    textBox.setText("");
		    // TODO this doesn't work with Android 4.0 theme
		    // textBox.setTextColor(getResources().getColor(R.color.dark));
		}
		// else if lost focus and still empty then add back string
		else if (!focus && isTextBoxEmpty(R.id.add_task)) {
		    isAddTaskBoxEmpty = true;
		    textBox.setText(R.string.add_task);
		    // textBox.setTextColor(getResources().getColor(R.color.selected));
		}
	    }
	});

	addTaskBox.setOnEditorActionListener(new OnEditorActionListener() {

	    @Override
	    public boolean onEditorAction(TextView textBox, int actionId,
		    KeyEvent arg2) {
		// if enter key pressed then try to add task
		if (actionId == EditorInfo.IME_NULL)
		    isAddTaskBoxEmpty = addTask((EditText) textBox);
		return true;
	    }
	});

	ImageButton add = (ImageButton) this.findViewById(R.id.submit);
	add.setOnClickListener(new OnClickListener() {

	    @Override
	    public void onClick(View arg0) {
		// steal focus from text box
		arg0.requestFocus();
		// try to add task
		isAddTaskBoxEmpty = addTask((EditText) findViewById(R.id.add_task));
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

    }

    private void hideText() {
	// start animationvisibility
	Animation animation = AnimationUtils.loadAnimation(
		AddTaskActivity.this, R.anim.fade_out);
	final LinearLayout parentView = (LinearLayout) findViewById(R.id.addtask_root);
	animation.setAnimationListener(new Animation.AnimationListener() {

	    @Override
	    public void onAnimationEnd(Animation animation) {
		parentView.post(new Runnable() {
		    public void run() {
			// it works without the runOnUiThread, but all UI
			// updates must
			// be done on the UI thread
			// activity.runOnUiThread(new Runnable() {
			// public void run() {
			// Animation animation =
			// AnimationUtils.loadAnimation(AddTaskActivity.this,
			// R.anim.quick_hide);
			// test.startAnimation(animation);
			// parentView.removeView(test);
			// }
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
	// test.startAnimation(animation);
	// try {
	// Thread.sleep(100);
	// }
	// catch(Exception e) {}
	// animation = AnimationUtils.loadAnimation(AddTaskActivity.this,
	// R.anim.fade_in);
	// test.startAnimation(animation);
	// test2.setVisibility(View.VISIBLE);

    }

    /**
     * Tries to parse the text from text box then save to db
     * 
     * If text box is empty, returns true
     * 
     * @param text
     * @return
     */
    private boolean addTask(EditText text) {
	if (isTextBoxEmpty(text.getId()))
	    return true;
	// setup parser
	GrammarParser parser = new GrammarParser();
	parser.setAndroidContext(AddTaskActivity.this);
	// parse string
	mTask = parser.parse(text.getText().toString());
	// prepare output
	hide(R.id.sample_tasks_heading);
	hide(R.id.sample_tasks);
	prepareViewsForTask(mTask);
	return false;
    }

    /**
     * Prepares the views in the UI to display the task
     * 
     * @param task
     */
    private void prepareViewsForTask(Task task) {
	// setup heading
	TextView heading = (TextView) findViewById(R.id.add_task_heading);
	heading.setText(task.getTask());
	heading.setText(toUpperCase(R.id.add_task_heading));
	heading.setOnClickListener(new TaskClickListener(R.id.add_task_heading));
	heading.setFocusable(true);
	heading.setClickable(true);
	// setup date
	addContentToRow(R.id.add_row_date, R.drawable.add_row_calendar,
		task.getFullLocaleDate());
	// setup time
	addContentToRow(R.id.add_row_time, R.drawable.add_row_clock,
		task.getLocaleTime());
    }

    /*
     * //////////////////////////////////////////////////////////////////////////
     * convenience functions
     */

    private boolean isTextBoxEmpty(int id) {
	EditText textBox = (EditText) findViewById(id);
	String input = textBox.getText().toString();
	return input.trim().equals("");
    }

    private void hide(int id) {
	View view = findViewById(id);
	view.setVisibility(View.GONE);
    }

    private String toUpperCase(int id) {
	TextView text = (TextView) findViewById(id);
	return text.getText().toString().toUpperCase();
    }

    private View getChildView(int parentId, int childId) {
	ViewGroup parent = (ViewGroup) findViewById(parentId);
	return parent.findViewById(childId);
    }

    /**
     * Set the icons and the text in the table that displays the task in the UI.
     * 
     * Setup the onclicklisteners
     * 
     * @param rowId
     * @param iconId
     *            - id of icon to use
     * @param text
     *            - text to use
     */
    private void addContentToRow(int rowId, int iconId, String text) {
	ViewGroup parent = (ViewGroup) findViewById(rowId);
	if (iconId >= 0) {
	    ImageView icon = (ImageView) parent.findViewById(R.id.add_row_icon);
	    icon.setImageResource(iconId);
	}
	if (text != null) {
	    TextView textBox = (TextView) parent
		    .findViewById(R.id.add_row_text);
	    textBox.setText(text);
	}
	// setup OnClickListener
	setupClickListenerForRow(rowId);
    }

    /**
     * Make the text view clickable and add the onclicklistener
     * 
     * @param id
     */
    private void setupClickListenerForRow(int rowId) {
	View text = getChildView(rowId, R.id.add_row_text);
	text.setClickable(true);
	text.setFocusable(true);
	text.setOnClickListener(new TaskClickListener(rowId));
    }

    private class TaskClickListener implements OnClickListener {
	int id;
	
	public TaskClickListener(int id) {
	    this.id = id;
	}
	
	@Override
	public void onClick(View v) {
	    switch (id) {
	    case R.id.add_task_heading:
		break;
	    case R.id.add_row_date:
		showDialog(DATE_PICKER);
		break;
	    case R.id.add_row_time:
		break;
	    }

	}

    }

    @Override
    protected Dialog onCreateDialog(int id) {
	switch (id) {
	case DATE_PICKER:
	    if (mTask == null) return null;
	    Calendar date = new GregorianCalendar();
	    date.setTime(mTask.getDateObj());
	    // set date picker as task's date
	    return new DatePickerDialog(this, null, date.get(Calendar.YEAR),
		    date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH));
	}
	return null;
    }

    private class AddHandler extends DatabaseHandler {

	@Override
	protected void onInsertComplete(int token, Object cookie,
		ContentProviderResult[] result) {
	    Toast.makeText(AddTaskActivity.this, "Task added",
		    Toast.LENGTH_SHORT).show();
	}

    }
}
