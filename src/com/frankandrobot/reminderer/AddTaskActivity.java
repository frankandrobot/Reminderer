package com.frankandrobot.reminderer;

import android.content.ContentProviderResult;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frankandrobot.reminderer.database.TaskDAOService;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.parser.ContextFreeGrammar;

public class AddTaskActivity extends FragmentActivity
{
    final static private String TAG = "R:AddTaskActivity";

    Task mTask;
    TaskDatabaseFacade taskDatabase;
    LoaderManager.LoaderCallbacks<Void> taskSaver = new TaskSaver();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        Button add = (Button) this.findViewById(R.id.add_new_button);
        add.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                ContextFreeGrammar parser = new ContextFreeGrammar(AddTaskActivity.this);
                EditText text = (EditText) findViewById(R.id.add_task);
                String input = text.getText().toString();
                mTask = parser.parse(input);
                String output = mTask.toString();
                TextView log = (TextView) findViewById(R.id.log_cat);
                log.setText(output);
            }

        });

        Button save = (Button) findViewById(R.id.save_button);
        save.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                if (mTask != null)
                {
                    if (Logger.LOGV)
                        Log.v(TAG, "clicked() ");

                    getSupportLoaderManager().restartLoader(TaskDatabaseFacade.ADD_TASK_LOADER_ID,
                                                            null,
                                                            taskSaver).forceLoad();
                }
            }

        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                finish();

            }

        });

        taskDatabase = new TaskDatabaseFacade(this);

        getSupportLoaderManager().initLoader(TaskDatabaseFacade.ADD_TASK_LOADER_ID,
                                             null,
                                             taskSaver);
    }

    class TaskSaver implements LoaderManager.LoaderCallbacks<Void>
    {
        @Override
        public Loader<Void> onCreateLoader(int i, Bundle bundle) {
            return taskDatabase.getAddTaskLoader(mTask);
        }

        @Override
        public void onLoadFinished(Loader<Void> booleanLoader, Void aBoolean) {
            Toast.makeText(AddTaskActivity.this, "Task added",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoaderReset(Loader<Void> booleanLoader) {

        }

    }

    class AddHandler extends TaskDAOService.DatabaseHandler
    {

        @Override
        protected void onInsertComplete(int token, Object cookie,
                                        ContentProviderResult[] result)
        {
            Toast.makeText(AddTaskActivity.this, "Task added",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
