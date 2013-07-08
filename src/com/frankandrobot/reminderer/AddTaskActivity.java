package com.frankandrobot.reminderer;

import android.app.Activity;
import android.content.ContentProviderResult;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.frankandrobot.reminderer.database.TaskDAOAsyncService.DatabaseHandler;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.parser.ContextFreeGrammar;

public class AddTaskActivity extends Activity
{
    Task mTask;
    Handler mHandler = new AddHandler();

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_task);

        Button add = (Button) this.findViewById(R.id.submit);
        add.setOnClickListener(new OnClickListener()
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
        save.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                if (mTask != null)
                    TaskDatabaseFacade.addTask(AddTaskActivity.this, mHandler,
                                               mTask);

            }

        });

        Button cancel = (Button) findViewById(R.id.cancel);
        cancel.setOnClickListener(new OnClickListener()
        {

            @Override
            public void onClick(View arg0)
            {
                finish();

            }

        });

    }

    class AddHandler extends DatabaseHandler
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
