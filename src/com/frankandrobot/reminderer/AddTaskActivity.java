package com.frankandrobot.reminderer;

import android.app.Activity;
import android.content.ContentProviderResult;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frankandrobot.reminderer.database.TaskDAOService;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.parser.ContextFreeGrammar;

public class AddTaskActivity extends FragmentActivity
{
    Task mTask;
    Handler mHandler = new AddHandler();
    TaskDatabaseFacade mDatabse;
    LoaderManager.LoaderCallbacks<Boolean> mLoader = new LoaderCall();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        Button add = (Button) this.findViewById(R.id.submit);
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

        Button save = (Button) findViewById(R.id.save);
        save.setOnClickListener(new View.OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                if (mTask != null)
                    mDatabse.addTask(AddTaskActivity.this,
                                     mHandler,
                                     mTask);

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

        mDatabse = new TaskDatabaseFacade(getApplicationContext());

        getSupportLoaderManager().initLoader(TaskDatabaseFacade.ADD_TASK_LOADER_ID, null, mLoader);
    }

    class LoaderCall implements LoaderManager.LoaderCallbacks<Boolean>
    {
        @Override
        public Loader<Boolean> onCreateLoader(int i, Bundle bundle) {
            return mDatabse.getAddTaskLoader(mTask);
        }

        @Override
        public void onLoadFinished(Loader<Boolean> booleanLoader, Boolean aBoolean) {
            Toast.makeText(AddTaskActivity.this, "Task added",
                    Toast.LENGTH_SHORT).show();
        }

        @Override
        public void onLoaderReset(Loader<Boolean> booleanLoader) {

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
