package com.frankandrobot.reminderer.database;

import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

import org.junit.Test;

import java.util.Arrays;

public class TaskTableTest
{
    @Test
    public void testGetAllColumns() throws Exception
    {
        TaskTable table = new TaskTable();
        String[] aCols = table.getAllColumns(TaskCol.class);
        assert(aCols.length == TaskCol.values().length);
        for(TaskCol col:TaskCol.values())
            assert(Arrays.asList(aCols).contains(col.colname()));

        aCols = table.getAllColumns(RepeatsCol.class);
        assert(aCols.length == RepeatsCol.values().length);
        for(RepeatsCol col:RepeatsCol.values())
            assert(Arrays.asList(aCols).contains(col.colname()));

        aCols = table.getAllColumns(TaskCol.class, RepeatsCol.class);
        assert(aCols.length == TaskCol.values().length + RepeatsCol.values().length);
        for(TaskCol col:TaskCol.values())
            assert(Arrays.asList(aCols).contains(col.colname()));
        for(RepeatsCol col:RepeatsCol.values())
            assert(Arrays.asList(aCols).contains(col.colname()));

    }

    @Test
    public void testGetColumns() throws Exception
    {
        TaskTable table = new TaskTable();
        String[] aCols = table.getColumns(TaskCol.TASK_ID);
        assert(aCols.length == 1);
        assert(Arrays.asList(aCols).contains("_id"));

        aCols = table.getColumns(RepeatsCol.REPEAT_NEXT_DUE_DATE);
        assert(aCols.length == 1);
        assert(Arrays.asList(aCols).contains(RepeatsCol.REPEAT_NEXT_DUE_DATE.colname()));

        aCols = table.getColumns(TaskCol.TASK_DUE_DATE, RepeatsCol.REPEAT_NEXT_DUE_DATE);
        assert(aCols.length == 2);
        assert(Arrays.asList(aCols).contains(RepeatsCol.REPEAT_NEXT_DUE_DATE.colname()));
        assert(Arrays.asList(aCols).contains(TaskCol.TASK_DUE_DATE.colname()));
    }

    @Test
    public void testGetColumnsWithNull()
    {
        TaskTable table = new TaskTable();
        String[] aCols = table.getColumns(TaskCol.TASK_ID, null);
        assert(aCols.length == 2);
        assert(Arrays.asList(aCols).contains(TaskCol.TASK_ID.colname()));
        assert(Arrays.asList(aCols).contains("NULL"));

        aCols = table.getColumns(null, TaskCol.TASK_ID);
        assert(aCols.length == 2);
        assert(Arrays.asList(aCols).contains("NULL"));
        assert(Arrays.asList(aCols).contains(TaskCol.TASK_ID.colname()));

    }
}
