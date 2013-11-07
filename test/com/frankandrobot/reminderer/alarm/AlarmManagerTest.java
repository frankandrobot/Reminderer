package com.frankandrobot.reminderer.alarm;

import android.app.Activity;
import android.database.Cursor;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.databasefacade.CursorNonQueryLoaders.AddTask;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;

import org.hamcrest.CoreMatchers;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static com.frankandrobot.reminderer.alarm.AlarmManager.GetNextAlarm;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_NEXT_DUE_DATE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_ID;
import static com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import static com.frankandrobot.reminderer.datastructures.Task.Task_Int;
import static com.frankandrobot.reminderer.datastructures.Task.Task_Parser_Calendar;
import static com.frankandrobot.reminderer.datastructures.Task.Task_String;
import static com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.Type;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class AlarmManagerTest
{
    private TaskProvider taskProvider;
    private Activity activity;
    private DateTime now = DateTime.now();

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
        AddTask addTask2 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task_String.desc, "hello world1");
        task1.set(Task_Parser_Calendar.dueDate, new TaskCalendar());

        //add the task
        AddTask addTask1 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask1.loadInBackground();

        assertThat(new AlarmManager().findAndEnableNextTasksDue(activity,
                task1.get(Task_Parser_Calendar.dueDate).getDate().getTime(),
                TaskProvider.CompareOp.ON_OR_AFTER)
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
        task2.set(Task_Int.repeatsType, Type.MONTH);
        long dueTime2 = task2.calculateNextDueDate();

        //add the task
        AddTask addTask2 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask2.loadInBackground();

        //add the second one
        Task task1 = new Task();
        task1.set(Task_String.desc, "hello world1");
        task1.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task1.set(Task_Int.repeatsType, Type.DAY);
        task1.calculateNextDueDate();

        //add the task
        AddTask addTask1 = new TaskDatabaseFacade(activity).getAddTaskLoader(task2);
        addTask1.loadInBackground();

        long dueTime = new AlarmManager().findAndEnableNextTasksDue(activity,
                                                                    now.getMillis(),
                                                                    TaskProvider.CompareOp.ON_OR_AFTER);
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

    @Test
    public void testGetNextAlarm()
    {
        //add some sample tasks
        addTask("task1", now.plusMinutes(5), null, false);
        addTask("task2", now.plusDays(5), null, false);
        addTask("task3", now.plusMinutes(5), Type.DAY, false); //due date NOW + 5 min
        addTask("task4", now.minusDays(1), Type.WEEK, false); //due date in 1 week
        addTask("task5", now.plusMinutes(10), null, true); //disabled

        new GetNextAlarm().getNextAlarm(now.plusMinutes(5).getMillis(), false);

        //this should calculate the next occurrance of task3
        Cursor cursor = taskProvider.query(TaskProvider.TASK_JOIN_REPEAT_URI,
                                           new TaskTable().getColumns(TASK_ID, REPEAT_NEXT_DUE_DATE),
                                           REPEAT_NEXT_DUE_DATE+"="+now.plusMinutes(5).plusDays(1).getMillis(),
                                           null,
                                           null);

        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        assertThat(cursor.getInt(0), is(3));

    }

    private long addTask(String desc,
                         DateTime dueTime,
                         Type repeatType,
                         boolean isComplete)
    {
        Task task = new Task();
        task.set(Task_String.desc, desc);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(dueTime.getMillis());
        task.set(Task_Boolean.isComplete, isComplete);
        long taskDueTime = task.get(Task_Parser_Calendar.dueDate).getDate().getTime();
        if (repeatType != null)
        {
            task.set(Task_Int.repeatsType, repeatType.getType());
            taskDueTime = task.calculateNextDueDate();
        }
        AddTask addTask = new AddTask(activity, task);
        addTask.loadInBackground();
        return taskDueTime;
    }

}
