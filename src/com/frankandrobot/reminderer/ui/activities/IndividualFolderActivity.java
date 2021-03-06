package com.frankandrobot.reminderer.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.ui.fragments.IndividualFolderListFragment;

/**
 * Use this activity to view individual folders
 * Shows the folder's open tasks
 */
public class IndividualFolderActivity extends ActionBarActivity
{
    private static final String TAG = IndividualFolderActivity.class.getSimpleName();

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.individual_folder);

        android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        IndividualFolderListFragment fragment = (IndividualFolderListFragment)
                                                        getSupportFragmentManager()
                                                        .findFragmentById(R.id.individual_folder_list_fragment);
        long folderId = getIntent().getLongExtra("folderId", -1);
        fragment.setFolderId(folderId);
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
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
            case id.action_addtask:
                addTask();
                return true;
            case id.action_addtask_mic:
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void addTask()
    {
        Intent intent = new Intent(IndividualFolderActivity.this,
                                   AddTaskActivity.class);
        startActivity(intent);
    }
}