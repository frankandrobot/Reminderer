package com.frankandrobot.reminderer.alarm;

import android.app.Activity;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;
import com.frankandrobot.reminderer.parser.GrammarRule;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static com.frankandrobot.reminderer.alarm.AlarmManager.*;
import static com.frankandrobot.reminderer.datastructures.Task.*;
import static com.frankandrobot.reminderer.parser.GrammarRule.*;
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
        task2.set(Task_String.desc, "hello world2");
        task2.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task2.get(Task_Parser_Calendar.dueDate).setTomorrow();

        //add the task
        TaskDatabaseFacade.AddTask addTask2 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task_String.desc, "hello world1");
        task1.set(Task_Parser_Calendar.dueDate, new TaskCalendar());

        //add the task
        TaskDatabaseFacade.AddTask addTask1 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask1.loadInBackground();

        assertThat(new AlarmManager().findAndEnableNextTasksDue(activity,
                task1.get(Task_Parser_Calendar.dueDate).getDate().getTime(),
                CompareOp.ON_OR_AFTER)
                ,is(task2.get(Task_Parser_Calendar.dueDate).getDate().getTime()));
    }

    @Test
    public void testCheckEnableNextRepeatingAlarm() throws Exception
    {
        DateTime now = DateTime.now();

        Task task2 = new Task();
        task2.set(Task_String.desc, "hello world2");
        task2.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task2.get(Task_Parser_Calendar.dueDate).setTomorrow();
        task2.set(Task_Int.repeatsType, RepeatsToken.Type.MONTH);
        long dueTime2 = task2.calculateNextDueDate();

        //add the task
        TaskDatabaseFacade.AddTask addTask2 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task_String.desc, "hello world1");
        task1.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task1.set(Task_Int.repeatsType, RepeatsToken.Type.DAY);
        task1.calculateNextDueDate();

        //add the task
        TaskDatabaseFacade.AddTask addTask1 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask1.loadInBackground();

        long dueTime = new AlarmManager().findAndEnableNextTasksDue(activity,
                                                                    now.getMillis(),
                                                                    CompareOp.ON_OR_AFTER);
        assertThat(dueTime,
                   is(dueTime2));
    }

    @Test
    public void testIntentUniqueId()
    {
        DateTime now = DateTime.now();
        long longval1 = now.getMillis();
        long longval2 = now.plusMinutes(1).getMillis();
        int value1= (int)longval1;
        int value2= (int)longval2;

        System.out.println(Long.toBinaryString(now.getMillis()));
        System.out.println(Long.toBinaryString(longval1));
        System.out.println(Integer.toBinaryString((int)now.getMillis()));
        System.out.println(Integer.toBinaryString((int)now.plusMinutes(1).getMillis()));
        System.out.println(Integer.toBinaryString(value1));
        System.out.println(Integer.toBinaryString(value2));

        assertThat(value1, CoreMatchers.not(value2));
    }
}
