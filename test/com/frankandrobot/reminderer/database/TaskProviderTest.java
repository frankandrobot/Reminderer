package com.frankandrobot.reminderer.database;

import android.app.Activity;
import android.database.Cursor;
import android.database.sqlite.SQLiteOpenHelper;

import com.frankandrobot.reminderer.database.TaskProvider.TaskJoinRepeatProvider;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskTableHelper;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.AddTask;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_Int;
import com.frankandrobot.reminderer.datastructures.Task.Task_Parser_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken;
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
        addTask("task1", now, null);
        addTask("task2", now.plusDays(5), null);
        addTask("task3", now, Type.DAY);
        addTask("task4", now.minusDays(1), Type.WEEK);
    }

    private long addTask(String desc, DateTime dueTime, Type repeatType)
    {
        Task task = new Task();
        task.set(Task_String.desc, desc);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(dueTime.getMillis());
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
    public void TaskJoinRepeatProvider()
    {
        //get tasks that are due now
        Cursor cursor = taskProvider.query(
                                       TASK_JOIN_REPEAT_URI,
                                       new TaskTable().getColumns(TASK_DESC),
                                       TASK_DUE_DATE+"=?",
                                       new String[]{String.valueOf(now.getMillis())},
                                       TASK_ID + " ASC"
                                       );
        assert(cursor != null);
        assertThat(cursor.getCount(), is(1));
        cursor.moveToFirst();
        String desc = cursor.getString(cursor.getColumnIndex(TASK_DESC.colname()));
        assertThat(desc, is("task3"));
    }

    @Test
    public void testConvertArrayToString() throws Exception
    {

    }


}
