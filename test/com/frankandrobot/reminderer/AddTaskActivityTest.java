package com.frankandrobot.reminderer;

import android.content.ContentValues;
import android.widget.Button;
import android.widget.EditText;

import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class AddTaskActivityTest
{
    @Test
    public void testOnCreate() throws Exception
    {
        AddTaskActivity activity = Robolectric.buildActivity(AddTaskActivity.class).create().get();
        Button addNewButton = (Button) activity.findViewById(id.add_new_button);
        EditText addTask = (EditText) activity.findViewById(id.add_task);
        addTask.setText("Hello world");
        addNewButton.performClick();

        Button saveButton = (Button) activity.findViewById(id.save_button);
        saveButton.performClick();

        Task task = new Task();
        task.set(Task_String.desc, "Hello world");
        ContentValues cv = task.toContentValues();
        activity.getContentResolver().insert(TaskProvider.CONTENT_URI, cv);
    }
}
