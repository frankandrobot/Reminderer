package com.frankandrobot.reminderer.datastructures;

import android.content.ContentResolver;
import android.database.MatrixCursor;
import android.net.Uri;

import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import com.frankandrobot.reminderer.datastructures.Task.Task_Ids;
import com.frankandrobot.reminderer.datastructures.Task.Task_Parser_Calendar;
import com.frankandrobot.reminderer.datastructures.Task.Task_String;

import org.hamcrest.core.Is;
import org.hamcrest.core.IsNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
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
    @Test
    public void testNewTaskFromCursor1() throws Exception
    {
        /*TASK_DESC
                , TASK_DUE_DATE
                , TASK_REPEATS_ID_FK
                , TASK_IS_COMPLETE;*/
        SimpleTestCursor cursor = new SimpleTestCursor();
        ArrayList<String> alCols = new ArrayList<String>();
        String[] aCols = new TaskTable().getAllColumns(TaskCol.class);
        alCols.addAll(Arrays.asList(aCols));
        cursor.setColumnNames(alCols);

        Date now = new Date();

        cursor.setResults(new Object[][] {
               new Object[] { (long)1, "Hello world", now.getTime(), null, 1}
        });
        cursor.moveToNext();

        Task task = new Task(cursor);
        assertThat(task.get(Task_Ids.id), is((long)1));
        assertThat(task.get(Task_String.desc), is("Hello world"));
        assertThat(task.get(Task_Parser_Calendar.dueDate).getDate().getTime(),
                   is(now.getTime()));
        assertThat(task.get(Task_Ids.repeatId), IsNull.<Long>nullValue());
        assertThat(task.get(Task_Boolean.isComplete), is(true));
    }

    @Test
    public void testNewTaskFromCursor1b() throws Exception
    {
        /*TASK_DESC
                , TASK_DUE_DATE
                , TASK_REPEATS_ID_FK
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
    }

    @Test
    public void testCalculateNextDueDate() throws Exception
    {

    }
}
