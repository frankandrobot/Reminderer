package com.frankandrobot.reminderer.database;

import android.app.Activity;
import android.content.ContentResolver;
import android.database.Cursor;

import com.frankandrobot.reminderer.alarm.AlarmManager;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade.AddTask;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class TaskDatabaseFacadeTest
{
    private TaskProvider taskProvider;
    private Activity activity;

    @Before
    public void setUp() throws Exception
    {
        taskProvider = new TaskProvider();
        activity = new Activity();

        taskProvider.onCreate();
        ShadowContentResolver.registerProvider(TaskProvider.AUTHORITY_NAME,
                                               taskProvider);
    }

    @Test
    public void testGetAddTaskLoader() throws Exception
    {

        Task task = new Task();
        task.set(Task_String.desc, "hello world");
        task.set(Task_Calendar.class, new TaskCalendar());

        AddTask addTask = new TaskDatabaseFacade().getAddTaskLoader(activity.getApplicationContext(),
                                                                    task);
        addTask.loadInBackground();

        ContentResolver resolver = activity.getContentResolver();

        Cursor cursor = resolver.query(TaskProvider.CONTENT_URI,
                                       new String[]{TaskTable.TaskCol.TASK_DESC.toString()},
                                       null,
                                       null,
                                       null);

        assertTrue(cursor != null);

        cursor.moveToFirst();
        assertThat(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())),
                   is("hello world"));

    }

    @Test
    public void testCheckEnableNextAlarm() throws Exception
    {
        Task task2 = new Task();
        task2.set(Task_String.desc, "hello world2");
        task2.set(Task_Calendar.class, new TaskCalendar());
        task2.get(Task_Calendar.class).setTomorrow();

        //add the task
        AddTask addTask2 = new TaskDatabaseFacade().getAddTaskLoader(activity.getApplicationContext(),
                                                                    task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task_String.desc, "hello world1");
        task1.set(Task_Calendar.class, new TaskCalendar());

        //add the task
        AddTask addTask1 = new TaskDatabaseFacade().getAddTaskLoader(activity.getApplicationContext(),
                                                                     task2);
        addTask1.loadInBackground();

        assertThat(new AlarmManager().findAndEnableNextTasksDue(activity,
                                                               task1.get(Task_Calendar.class).getDate().getTime())
                          ,is(task2.get(Task_Calendar.class).getDate().getTime()));
    }

    @Test
    public void testGetLoadAllTasksLoader() throws Exception
    {

    }

    @Test
    public void testGetLoadTasksLoader() throws Exception
    {

    }
}
