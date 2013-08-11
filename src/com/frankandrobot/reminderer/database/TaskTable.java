package com.frankandrobot.reminderer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Columns for the {@link Task} model
 */
final public class TaskTable
{
    public final static String TASK_TABLE = "task";
    public final static String REPEATABLE_TABLE = "repeatable";

    private HashMap<Class<? extends Enum>, EnumSet> hmColumns = new HashMap<Class<? extends Enum>, EnumSet>();

    public interface Column
    {
        public String colname();
    }

    /**
     * Gets the columns in the enum class.
     *
     * @param aTables array of enum class names (ex: TaskCol)
     * @return string of column names
     */
    public String[] getAllColumns(Class<? extends Enum>... aTables)
    {
        //create enum sets first
        for(Class<? extends Enum> table:aTables)
        {
            if (hmColumns.get(table) == null)
            {
                hmColumns.put(table, EnumSet.allOf(table));
            }
        }

        //create column array
        int length = 0;
        for(Class<? extends Enum> table:aTables)
        {
            length += hmColumns.get(table).size();
        }
        String[] aCols = new String[length];

        //populate array
        int len = 0;
        for(Class<? extends Enum> table:aTables)
        {
            for(Object column:hmColumns.get(table))
            {
                aCols[len++] = ((Column) column).colname();
            }
        }

        return aCols;
    }

    public String[] getColumns(Column... aCols)
    {
        String[] aStrCols = new String[aCols.length];
        int len = 0;
        for(Column col:aCols)
            aStrCols[len++] = col.colname();
        return aStrCols;
    }

    public enum TaskCol implements Column
    {
        TASK_ID("_id")
        , TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEATS_ID_FK
        , TASK_IS_COMPLETE;

        private String colname;
        TaskCol() {}
        TaskCol(String value) { this.colname = value; }

        /**
         * @deprecated use {@link #colname}
         * @return string
         */
        @Override
        public String toString() { return colname == null ? super.toString() : colname; }

        @Override
        public String colname() { return colname == null ? name() : colname; }
    }

    public enum RepeatsCol implements Column
    {
        REPEAT_ID
        ,REPEAT_TYPE
        ,NEXT_DUE_DATE;

        @Override
        public String colname() { return name(); }
    }

    /**
     * Used to create and upgrade the database
     */
    static class TaskTableHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "reminderer.db";
        private static final int DATABASE_VERSION = 9;
        private static final String TAG = "R:TaskHelper";

        public TaskTableHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            if (Logger.LOGV) Log.v(TAG, "Creating table");

            String dbCreateString = "";
            dbCreateString += "CREATE TABLE " + TASK_TABLE;
            dbCreateString += "(";
            dbCreateString += TaskCol.TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
            dbCreateString += TaskCol.TASK_DESC + " TEXT NOT NULL, ";
            dbCreateString += TaskCol.TASK_REPEATS_ID_FK + " INTEGER, ";
            dbCreateString += TaskCol.TASK_DUE_DATE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_IS_COMPLETE + " INTEGER";
            dbCreateString += ");";
            db.execSQL(dbCreateString);

            dbCreateString = "";
            dbCreateString += "CREATE TABLE " + REPEATABLE_TABLE;
            dbCreateString += "(";
            dbCreateString += RepeatsCol.REPEAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
            dbCreateString += RepeatsCol.REPEAT_TYPE + " INTEGER, ";
            dbCreateString += RepeatsCol.NEXT_DUE_DATE + " INTEGER ";
            dbCreateString += ");";
            db.execSQL(dbCreateString);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int currentVersion)
        {
            if (Logger.LOGV)
                Log.v(TAG, "Upgrading database from version " + oldVersion
                                   + " to " + currentVersion
                                   + ", which will destroy all old data");

            db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
            db.execSQL("DROP TABLE IF EXISTS " + REPEATABLE_TABLE);
            onCreate(db);

        }
    }
}
