package com.frankandrobot.reminderer;

import android.app.ActionBar;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.frankandrobot.reminderer.R.id;

public class RemindererActivity extends ActionBarActivity
{
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_HOME
                                            | ActionBar.DISPLAY_SHOW_TITLE | ActionBar.DISPLAY_SHOW_CUSTOM);

        Button addNew = (Button) findViewById(id.add_new_button);
        addNew.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(RemindererActivity.this,
                                           AddTaskActivity.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.main_activity_actionbar, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        switch (item.getItemId())
        {
            case R.id.action_addtask:
                addTask();
                return true;
            case R.id.action_addtask_mic:
                return true;
            default:
                return true;
        }
    }

    private void addTask()
    {
        Intent intent = new Intent(RemindererActivity.this,
                                   AddTaskActivity.class);
        startActivity(intent);
    }
}