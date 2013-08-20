package com.frankandrobot.reminderer.alarm;

import android.app.Activity;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AlarmManagerTest
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
    public void testCheckEnableNextAlarm() throws Exception
    {
        Task task2 = new Task();
        task2.set(Task.Task_String.desc, "hello world2");
        task2.set(Task.Task_Parser_Calendar.dueDate, new TaskCalendar());
        task2.get(Task.Task_Parser_Calendar.dueDate).setTomorrow();

        //add the task
        TaskDatabaseFacade.AddTask addTask2 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task.Task_String.desc, "hello world1");
        task1.set(Task.Task_Parser_Calendar.dueDate, new TaskCalendar());

        //add the task
        TaskDatabaseFacade.AddTask addTask1 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask1.loadInBackground();

        assertThat(new AlarmManager().findAndEnableNextTasksDue(activity,
                task1.get(Task.Task_Parser_Calendar.dueDate).getDate().getTime(),
                AlarmManager.CompareOp.ON_OR_AFTER)
                ,is(task2.get(Task.Task_Parser_Calendar.dueDate).getDate().getTime()));
    }
}
