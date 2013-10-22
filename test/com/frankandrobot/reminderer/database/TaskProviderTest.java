package com.frankandrobot.reminderer.database;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;

import com.frankandrobot.reminderer.database.TaskProvider.CompareOp;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
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
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.*;
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
        addTask("task5", now.plusMinutes(10), null, true); //disabled
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
    public void testDeleteRows()
    {
        //delete task 3 in repeat table
        Uri deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, "1");
        int noDeleted = taskProvider.delete(deleteUri, null, null);
        assertThat(noDeleted, is(1));

        deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, "3");
        noDeleted = taskProvider.delete(deleteUri, null, null);
        assertThat(noDeleted, is(0));
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
        //add duplicate task
        addTask("task6", now.plusMinutes(5), null, false);

        Cursor cursor = taskProvider.query(LOAD_DUE_TIMES_URI,
                                           null,
                                           CompareOp.ON_OR_AFTER.toString(),
                                           new String[]{String.valueOf(now.getMillis())},
                                           TASK_DUE_DATE.colname());
        assert(cursor != null);
        dump(cursor);
        assertThat(cursor.getCount(), is(2));

        cursor.moveToFirst();
        long dueTime = cursor.getLong(0);
        assertThat(dueTime, is(now.plusMinutes(5).getMillis()));

        cursor.moveToNext();
        dueTime = cursor.getLong(0);
        assertThat(dueTime, is(now.plusDays(5).getMillis()));
    }

    @Test
    public void testSingleRowRepeatUriProvider()
    {
        //delete both repeating tasks

        Uri deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, "1");
        int noDeleted = taskProvider.delete(deleteUri, null, null);
        assertThat(noDeleted, is(1));

        deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, "2");
        noDeleted = taskProvider.delete(deleteUri, null, null);
        assertThat(noDeleted, is(1));

        Cursor cursor = taskProvider.query(LOAD_DUE_TIMES_URI,
                                           null,
                                           CompareOp.ON_OR_AFTER.toString(),
                                           new String[]{String.valueOf(now.plusWeeks(1).getMillis())},
                                           TASK_DUE_DATE.colname());
        assert(cursor != null);
        dump(cursor);
        assertThat(cursor.getCount(), is(0));
    }

    @Test
    public void testRepeatUriProviderInsert1() throws Exception
    {
        ContentValues values = new ContentValues();
        values.put(REPEAT_NEXT_DUE_DATE.colname(), now.plusHours(1).getMillis());
        values.put(REPEAT_TASK_ID_FK.colname(), 3);

        taskProvider.insert(TaskProvider.REPEAT_URI, values);

        Cursor cursor = taskProvider.query(TaskProvider.LOAD_OPEN_TASKS_URI, null, null, null, null);

        assert(cursor != null);

        boolean newRowFound = false;
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            long taskId = cursor.getLong(cursor.getColumnIndex(TASK_ID.colname()));
            long nextDueDate = 0;
            int nextDueDateIndex = cursor.getColumnIndex(TASK_DUE_DATE.colname());
            if (!cursor.isNull(nextDueDateIndex))
                nextDueDate = cursor.getLong(nextDueDateIndex);
            if (taskId == 3 && nextDueDate == now.plusHours(1).getMillis())
            {
                newRowFound = true;
                //break;
            }
            cursor.moveToNext();
        }

        assertThat(newRowFound, is(true));

    }

    @Test
    public void testRepeatUriProviderInsert2() throws Exception
    {
        DateTime now = DateTime.now().minusMinutes(30);
        addTask("Repeat", now, Type.HOUR, false);

        Cursor cTask = taskProvider.query(TaskProvider.LOAD_OPEN_TASKS_URI, null, null, null, null);
        assert(cTask != null);
        cTask.moveToFirst();
        long taskId = cTask.getLong(cTask.getColumnIndex(TaskCol.TASK_ID.colname()));

        long nextDueTime = Task.calculateNextDueDate(Type.HOUR, now.getMillis());
        System.out.println("nextDueTime:"+new DateTime(nextDueTime));

        ContentValues values = new ContentValues();
        values.put(REPEAT_NEXT_DUE_DATE.colname(), nextDueTime);
        values.put(REPEAT_TASK_ID_FK.colname(), taskId);

        taskProvider.insert(TaskProvider.REPEAT_URI, values);

        Cursor cursor = taskProvider.query(TaskProvider.LOAD_DUE_TIMES_URI,
                                           null,
                                           CompareOp.ON_OR_AFTER.toString(),
                                           new String[]{Long.toString(nextDueTime)},
                                           TASK_DUE_DATE.colname());
        assert(cursor != null);

        boolean newRowFound = false;
        cursor.moveToFirst();
        while(!cursor.isAfterLast())
        {
            long mtaskId = cursor.getLong(cursor.getColumnIndex(TASK_ID.colname()));
            if (mtaskId == taskId)
            {
                newRowFound = true;
                long nextDueDate = cursor.getLong(cursor.getColumnIndex(TASK_DUE_DATE.colname()));
                assertThat(nextDueDate, is(nextDueTime));
                for(int i=0; i<cursor.getColumnCount(); i++)
                    if (!cursor.getColumnName(i).equals(TASK_DUE_DATE.colname().toLowerCase()))
                        System.out.print(cursor.getColumnName(i)+":"+cursor.getString(i)+" ");
                    else
                        System.out.print(cursor.getColumnName(i)+":"+new DateTime(cursor.getLong(i))+" ");
                break;
            }
            cursor.moveToNext();
        }

        assertThat(newRowFound, is(true));

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
