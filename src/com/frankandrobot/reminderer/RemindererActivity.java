package com.frankandrobot.reminderer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

import com.frankandrobot.reminderer.R.id;

public class RemindererActivity extends FragmentActivity
{
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);

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
}