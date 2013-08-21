package com.frankandrobot.reminderer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.widget.Button;
import android.widget.EditText;

import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class AddTaskActivityTest
{

    private TaskProvider taskProvider;
    private AddTaskActivity activity;

    @Before
    public void setUp() throws Exception
    {
        taskProvider = new TaskProvider();
        activity = Robolectric.buildActivity(AddTaskActivity.class).create().get();

        taskProvider.onCreate();
        ShadowContentResolver.registerProvider(TaskProvider.AUTHORITY_NAME,
                                               taskProvider);
    }

    @Test
    public void testAddTask() throws Exception
    {
        ContentResolver resolver = activity.getContentResolver();

        Button addNewButton = (Button) activity.findViewById(id.add_new_button);
        EditText addTask = (EditText) activity.findViewById(id.add_task);
        addTask.setText("Hello world");
        addNewButton.performClick();

        Button saveButton = (Button) activity.findViewById(id.save_button);
        saveButton.performClick();

        Cursor cursor = resolver.query(TaskProvider.TASKS_URI,
            new String[]{TaskTable.TaskCol.TASK_DESC.toString()},
            null,
            null,
            null);

        assertTrue(cursor != null);

        cursor.moveToFirst();
        assertThat(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())),
                   is("Hello world"));
    }
}
