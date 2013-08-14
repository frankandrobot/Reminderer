package com.frankandrobot.reminderer.datastructures;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.support.v4.app.FragmentActivity;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.AddTask;
import com.frankandrobot.reminderer.datastructures.Task.Task_Alarm_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import com.frankandrobot.reminderer.datastructures.Task.Task_Ids;
import com.frankandrobot.reminderer.datastructures.Task.Task_Int;
import com.frankandrobot.reminderer.datastructures.Task.Task_Parser_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.Type;

import org.hamcrest.core.IsNull;
import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.shadows.ShadowContentResolver;
import org.robolectric.tester.android.database.SimpleTestCursor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class TaskTest
{
    /**
     * Tests task that does not repeat
     *
     * @throws Exception
     */
    @Test
    public void testNewTaskFromCursor1() throws Exception
    {
        /*
        TASK_ID
        , TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEAT_TYPE
        , TASK_IS_COMPLETE;*/

        TaskTable table = new TaskTable();
        MatrixCursor cursor = new MatrixCursor(table.getAllColumns(TaskCol.class), 1);

        Date now = new Date();

        cursor.addRow(new Object[]
               { (long)1, "Hello world", now.getTime(), 5, 1}
        );
        cursor.moveToNext();

        Task task = new Task(cursor);
        assertThat(task.get(Task_Ids.id), is((long)1));
        assertThat(task.get(Task_String.desc), is("Hello world"));
        assertThat(task.get(Task_Parser_Calendar.dueDate).getDate().getTime(),
                   is(now.getTime()));
        assertThat(task.get(Task_Int.repeatsType), is(5));
        assertThat(task.get(Task_Boolean.isComplete), is(true));
        assertThat(task.get(Task_Ids.repeatId), IsNull.<Long>nullValue());
    }

    /**
     * Sets only two columns. Check that others are null or have default values
     *
     * @throws Exception
     */
    @Test
    public void testNewTaskFromCursor1b() throws Exception
    {
        /*TASK_DESC
                , TASK_DUE_DATE
                , TASK_REPEAT_TYPE
                , TASK_IS_COMPLETE;*/
        TaskTable table = new TaskTable();
        MatrixCursor cursor = new MatrixCursor(table.getColumns(TaskCol.TASK_ID,
                                                                Task_String.desc)
                                               ,1);
        cursor.addRow(new Object[] {
                        (long)1,
                        "Hello world"
        });
        cursor.moveToFirst();

        Task task = new Task(cursor);
        assertThat(task.get(Task_Ids.id), is((long)1));
        assertThat(task.get(Task_String.desc), is("Hello world"));
        assertThat(task.get(Task_Ids.repeatId), IsNull.<Long>nullValue());
        assertThat(task.get(Task_Int.repeatsType), is(0));
        assertThat(task.get(Task_Boolean.isComplete), is(false));
    }

    /**
     * Cursor contains a repeating task
     *
     * @throws Exception
     */
    @Test
    public void testNewTaskFromCursor1c() throws Exception
    {
        /*TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEAT_TYPE
        , TASK_IS_COMPLETE;*/
        TaskTable table = new TaskTable();
        long now = new Date().getTime();
        MatrixCursor cursor = new MatrixCursor(table.getAllColumns(TaskCol.class,
                                                                   RepeatsCol.class),
                                               1);
        cursor.addRow(new Object[] {
                                           (long)1,
                                           "Hello world",
                                           now,
                                           2, //repeat type
                                           0, //isComplete
                                           (long)2, //repeatsId
                                           (long)1, //task id FK
                                           now// next due date
        });
        cursor.moveToFirst();

        Task task = new Task(cursor);
        assertThat(task.get(Task_Ids.id), is((long)1));
        assertThat(task.get(Task_String.desc), is("Hello world"));
        assertThat(task.get(Task_Parser_Calendar.dueDate).getDate().getTime(),
                   is(now));
        assertThat(task.get(Task_Int.repeatsType), is(2));
        assertThat(task.get(Task_Boolean.isComplete), is(false));
        assertThat(task.get(Task_Ids.repeatId), is((long)2));
        assertThat(task.get(Task_Ids.taskId_fk), is((long)1));
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate), is(now));
    }

    @Test
    public void testContentValues() throws Exception
    {
        Task task = new Task();
        task.set(Task_Ids.id, (long)1);
        task.set(Task_Ids.repeatId, (long)2);
        task.set(Task_Ids.taskId_fk, (long)1);
        task.set(Task_String.desc, "Hello world");
        DateTime now = DateTime.now();
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(now.getMillis());
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Int.repeatsType, Type.MONTH);
        task.set(Task_Alarm_Calendar.nextDueDate, now.getMillis());

        ContentValues values = task.getContentValuesForInsert();
        assertThat(values.getAsString(TaskCol.TASK_DESC.colname()),
                   is("Hello world"));
        assertThat(values.getAsString(TaskCol.TASK_IS_COMPLETE.colname()),
                   is("0"));
        assertThat(values.getAsInteger(TaskCol.TASK_REPEAT_TYPE.colname()), is(Type.MONTH.getType()));
        assertThat(values.getAsString(RepeatsCol.REPEAT_NEXT_DUE_DATE.colname()),
                   is(String.valueOf(now.getMillis())));
    }

    /**
     * Insert non-repeating task
     *
     * @throws Exception
     */
    @Test
    public void insertNonRepeatingTaskIntoDb1() throws Exception
    {
        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        Date now = new Date();
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(now.getTime());

        //setup task provider
        FragmentActivity activity = Robolectric.buildActivity( FragmentActivity.class )
                                            .create()
                                            .start()
                                            .resume()
                                            .get();
        TaskProvider taskProvider = new TaskProvider();
        taskProvider.onCreate();


        ShadowContentResolver.registerProvider(TaskProvider.AUTHORITY_NAME,
                                               taskProvider);

        //run insert
        AddTask addTask = new TaskDatabaseFacade(activity).getAddTaskLoader(task);
        addTask.loadInBackground();

        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(TaskProvider.CONTENT_URI,
                                       new TaskTable().getAllColumns(TaskCol.class),
                                       TaskCol.TASK_DUE_DATE+"="+now.getTime(),
                                       null,
                                       null);
        assert(cursor != null);
        cursor.moveToFirst();
        assertThat(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.colname())),
                   is("Hell on earth"));

    }

    @Test
    public void testCalculateNextDueDateHour() throws Exception
    {
        DateTime calendar = new DateTime();
        DateTime past = calendar.minusHours(12).minusMinutes(1);

        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.HOUR.getType());
        task.calculateNextDueDate();

        DateTime tomorrow = past.plusHours(13);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));

        calendar = new DateTime();
        past = calendar.minusHours(12).plusMinutes(1);

        task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.HOUR.getType());
        task.calculateNextDueDate();

        tomorrow = past.plusHours(12);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));
    }

    @Test
    public void testCalculateNextDueDateDay() throws Exception
    {
        DateTime calendar = new DateTime();
        DateTime past = calendar.minusDays(12).minusMinutes(1);

        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.DAY.getType());
        task.calculateNextDueDate();

        DateTime tomorrow = past.plusDays(13);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));

        calendar = new DateTime();
        past = calendar.minusDays(12).plusMinutes(1);

        task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.DAY.getType());
        task.calculateNextDueDate();

        tomorrow = past.plusDays(12);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));
    }

    @Test
    public void testCalculateNextDueDateWeek() throws Exception
    {
        DateTime calendar = new DateTime();
        DateTime past = calendar.minusWeeks(12).minusMinutes(1);

        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.WEEK.getType());
        task.calculateNextDueDate();

        DateTime tomorrow = past.plusWeeks(13);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));

        calendar = new DateTime();
        past = calendar.minusWeeks(12).plusMinutes(1);

        task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.WEEK.getType());
        task.calculateNextDueDate();

        tomorrow = past.plusWeeks(12);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));
    }

    @Test
    public void testCalculateNextDueDateMonth() throws Exception
    {
        DateTime calendar = new DateTime();
        DateTime past = calendar.minusMonths(12).minusMinutes(1);

        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.MONTH.getType());
        task.calculateNextDueDate();

        DateTime tomorrow = past.plusMonths(13);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));

        calendar = new DateTime();
        past = calendar.minusMonths(12).plusMinutes(1);

        task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.MONTH.getType());
        task.calculateNextDueDate();

        tomorrow = past.plusMonths(12);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));
    }

    @Test
    public void testCalculateNextDueDateYear() throws Exception
    {
        DateTime calendar = new DateTime();
        DateTime past = calendar.minusYears(12).minusMinutes(1);

        Task task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.YEAR.getType());
        task.calculateNextDueDate();

        DateTime tomorrow = past.plusYears(13);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));

        calendar = new DateTime();
        past = calendar.minusYears(12).plusMinutes(1);

        task = new Task();
        task.set(Task_String.desc, "Hell on earth");
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(past.getMillis());
        task.set(Task_Int.repeatsType, Type.YEAR.getType());
        task.calculateNextDueDate();

        tomorrow = past.plusYears(12);
        assertThat(task.get(Task_Alarm_Calendar.nextDueDate),
                   is(tomorrow.getMillis()));
    }

    @Test
    public void insertIntoDb2() throws Exception
    {
        Task task = new Task();
        task.set(Task_String.desc, "Insert into db2");
        DateTime now = DateTime.now().minusDays(1).minusMinutes(1);
        task.set(Task_Parser_Calendar.dueDate, new TaskCalendar());
        task.get(Task_Parser_Calendar.dueDate).setTimeInMillis(now.getMillis());
        task.set(Task_Boolean.isComplete, false);
        task.set(Task_Int.repeatsType, Type.DAY);
        task.calculateNextDueDate();

        //setup task provider
        FragmentActivity activity = Robolectric.buildActivity( FragmentActivity.class )
                                            .create()
                                            .start()
                                            .resume()
                                            .get();
        TaskProvider taskProvider = new TaskProvider();
        taskProvider.onCreate();


        ShadowContentResolver.registerProvider(TaskProvider.AUTHORITY_NAME,
                                               taskProvider);

        //run insert
        AddTask addTask = new TaskDatabaseFacade(activity).getAddTaskLoader(task);
        addTask.loadInBackground();

        ContentResolver resolver = activity.getContentResolver();
        Cursor cursor = resolver.query(TaskProvider.ALL_TASK_COLUMNS_URI,
                                       new TaskTable().getAllColumns(TaskCol.class,
                                                                     RepeatsCol.class),
                                       TaskCol.TASK_DUE_DATE+"="+now.getMillis(),
                                       null,
                                       null);
        assert(cursor != null);
        cursor.moveToFirst();
        /*TASK_ID("_id")
                , TASK_DESC
                , TASK_DUE_DATE
                , TASK_REPEAT_TYPE
                , TASK_IS_COMPLETE;*/
        assert(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_ID.colname())) != null);
        assertThat(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.colname())),
                   is("Insert into db2"));
        assertThat(cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.colname())),
                   is(now.getMillis()));
        assertThat(cursor.getInt(cursor.getColumnIndex(TaskCol.TASK_REPEAT_TYPE.colname())),
                   is(Type.DAY.getType()));
        assertThat(cursor.getInt(cursor.getColumnIndex(TaskCol.TASK_IS_COMPLETE.colname())),
                   is(0));
        assert(cursor.getString(cursor.getColumnIndex(RepeatsCol.REPEAT_ID.colname())) != null);
        long taskId = cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_ID.colname()));
        long taskIdFk = cursor.getLong(cursor.getColumnIndex(RepeatsCol.REPEAT_TASK_ID_FK.colname()));
        assertThat(taskId, is(taskIdFk));
        assertThat(cursor.getLong(cursor.getColumnIndex(RepeatsCol.REPEAT_NEXT_DUE_DATE.colname())),
                   is(now.plusDays(2).getMillis()));
    }
}
