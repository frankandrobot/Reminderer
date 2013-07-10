package com.frankandrobot.reminderer.database;

import com.frankandrobot.reminderer.datastructures.Task;

/**
 * Columns for the {@link Task} model
 */
public class DbColumns
{
    public final static String TASK_TABLE = "task";
    // projection used to display in alert listview
    //
    // you use two pairs. One used inside the contentprovider and another
    // outside of the content provider
    public final static String[] TASK_ALERT_LISTVIEW_CP = {TaskCol.TASK_ID.toString(), TaskCol.TASK_DESC.toString(), TaskCol.TASK_DUE_DATE.toString()};
    public final static String[] TASK_ALERT_LISTVIEW_NO_CP = {TaskCol.TASK_DESC.toString(), TaskCol.TASK_DUE_DATE.toString()};
    public final static String DEFAULT_SORT = TaskCol.TASK_DUE_DATE + " ASC";
    public final static String LTE = "<=";
    public final static String EQ = "=";
    public final static String GTE = ">=";

    public enum TaskCol
    {
        TASK_ID("_id")
        , TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEATS_TYPE;

        private String value;

        TaskCol() {}

        TaskCol(String value) { this.value = value; }

        @Override
        public String toString()
        {
            return value == null ? super.toString() : value;
        }

        public static String[] getAllColumns()
        {
            String[] aCols = new String[values().length];
            int len = 0;
            for(TaskCol col:values())
                aCols[len++] = col.toString();
            return aCols;
        }
    }
}
