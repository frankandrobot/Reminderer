package com.frankandrobot.reminderer.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.frankandrobot.reminderer.alarm.AlarmManager.CompareOp;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.AddTask;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import com.frankandrobot.reminderer.datastructures.Task.Task_Int;
import com.frankandrobot.reminderer.datastructures.Task.Task_Parser_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.Type;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowContentResolver;

import static com.frankandrobot.reminderer.database.TaskProvider.*;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.*;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(RobolectricTestRunner.class)
public class TaskProviderTest
{
    private TaskProvider taskProvider;
    private Activity activity;
    private DateTime now = DateTime.now();
    private SQLiteOpenHelper openHelper;

    @Before
    public void setUp() throws Exception
    {
        taskProvider = new TaskProvider();
        activity = new Activity();

        taskProvider.onCreate();
        ShadowContentResolver.registerProvider(AUTHORITY_NAME,
                                               taskProvider);

        //openHelper = new TaskTableHelper(activity);

        //add some sample tasks
        addTask("task1", now.plusMinutes(5), null, false);
        addTask("task2", now.plusDays(5), null, false);
        addTask("task3", now.plusMinutes(5), Type.DAY, false); //due date NOW + 5 min
        addTask("task4", now.minusDays(1), Type.WEEK, false); //due date in 1 week
        addTask("task5", now.plusMinutes(10), null, true);
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

    @Test
    public void testTaskJoinRepeatProvider()
    {
        //get tasks that are due now
        Cursor cursor = taskProvider.query(
                                       TASK_JOIN_REPEAT_URI,
                                       new TaskTable().getColumns(TASK_DESC),
                                       TASK_DUE_DATE+"=?",
                                       new String[]{String.valueOf(now.plusMinutes(5).getMillis())},
                                       TASK_ID + " ASC"
                                       );
        assert(cursor != null);
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        String desc = cursor.getString(cursor.getColumnIndex(TASK_DESC.colname()));
        assertThat(desc, is("task3"));
    }

    @Test
    public void testLoadOpenTasksProvider()
    {
        Cursor cursor = taskProvider.query(LOAD_OPEN_TASKS_URI,
                                           null,
                                           null,
                                           null,
                                           null);
        assert(cursor != null);
        dump(cursor);
        assertThat(cursor.getCount(), is(4));
        cursor.moveToFirst();
        String[] dateOrder = new String[]{"task1", "task3", "task2", "task4"};
        while(!cursor.isAfterLast())
        {
            String desc = cursor.getString(cursor.getColumnIndex(TASK_DESC.colname()));
            assertThat(desc, is(dateOrder[cursor.getPosition()]));
            cursor.moveToNext();
        }
    }

    @Test
    public void testLoadDueTimesProvider()
    {
        Cursor cursor = taskProvider.query(LOAD_DUE_TIMES_URI,
                                           null,
                                           CompareOp.ON_OR_AFTER.toString(),
                                           new String[]{String.valueOf(now.plusMinutes(5).getMillis()),
                                                        String.valueOf(now.plusMinutes(5).getMillis())},
                                           TASK_ID.colname());
        assert(cursor != null);
        dump(cursor);
        assertThat(cursor.getCount(), is(2));
        cursor.moveToFirst();
        String[] dateOrder = new String[]{"1", "3"};
        while(!cursor.isAfterLast())
        {
            String desc = cursor.getString(cursor.getColumnIndex(TASK_ID.colname()));
            assertThat(desc, is(dateOrder[cursor.getPosition()]));
            cursor.moveToNext();
        }
    }

    @Test
    public void testSingleRowRepeatUriProvider()
    {
        //delete task 3 in repeat table
        Uri deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, "1");
        int noDeleted = taskProvider.delete(deleteUri, null, null);
        assertThat(noDeleted, is(1));

        Cursor cursor = taskProvider.query(LOAD_DUE_TIMES_URI,
                                           null,
                                           CompareOp.ON_OR_AFTER.toString(),
                                           new String[]{String.valueOf(now.plusMinutes(5).getMillis()),
                                                   String.valueOf(now.plusMinutes(5).getMillis())},
                                           TASK_ID.colname());
        assert(cursor != null);
        dump(cursor);
        assertThat(cursor.getCount(), is(1));
    }

    @Test
    public void testConvertArrayToString() throws Exception
    {

    }

    static private void dump(Cursor cursor)
    {
        int pos = cursor.getPosition();
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            System.out.println(new Task(cursor));
            cursor.moveToNext();
        }
        if (pos >= 0) cursor.moveToPosition(pos);
    }
}
