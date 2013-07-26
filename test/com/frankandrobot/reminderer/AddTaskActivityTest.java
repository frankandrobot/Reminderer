package com.frankandrobot.reminderer;

import android.content.ContentResolver;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Button;
import android.widget.EditText;

import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static org.junit.Assert.assertTrue;


@RunWith(RobolectricTestRunner.class)
public class AddTaskActivityTest
{
    SQLiteDatabase _db;
    //IProfileManager _mgr;

    @Test
    public void testOnCreate() throws Exception
    {
        TaskProvider provider = new TaskProvider();
        AddTaskActivity activity = Robolectric.buildActivity(AddTaskActivity.class).create().get();

        //Activity activity = new Activity();
        ContentResolver resolver = activity.getContentResolver();

        provider.onCreate();
        ShadowContentResolver.registerProvider(TaskProvider.AUTHORITY_NAME, provider);

        Button addNewButton = (Button) activity.findViewById(id.add_new_button);
        EditText addTask = (EditText) activity.findViewById(id.add_task);
        addTask.setText("Hello world");
        addNewButton.performClick();

        Button saveButton = (Button) activity.findViewById(id.save_button);
        saveButton.performClick();


//        Task task = new Task();
//        task.set(Task_String.desc, "Hello world");
//        ContentValues cv = task.toContentValues();
//        resolver.insert(TaskProvider.CONTENT_URI, cv);

        Cursor cursor = resolver.query(TaskProvider.CONTENT_URI,
            new String[]{TaskTable.TaskCol.TASK_DESC.toString()},
            null,
            null,
            null);

        assertTrue(cursor != null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast())
        {
            System.out.println((cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString()))));
            cursor.moveToNext();
        }
    }
}
