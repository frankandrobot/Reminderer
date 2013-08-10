package com.frankandrobot.reminderer.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

/**
 * Columns for the {@link Task} model
 */
public class TaskTable
{
    public final static String TASK_TABLE = "task";

    public enum TaskCol
    {
        TASK_ID("_id")
        , TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEATS_TYPE
        , TASK_IS_COMPLETE;

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
        public static String[] getColumns(TaskCol... aCols)
        {
            String[] aStrCols = new String[aCols.length];
            int len = 0;
            for(TaskCol col:aCols)
                aStrCols[len++] = col.toString();
            return aStrCols;
        }
    }

    /**
     * Used to create and upgrade the database
     */
    static class TaskTableHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "reminderer.db";
        private static final int DATABASE_VERSION = 8;
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
            dbCreateString += TaskCol.TASK_REPEATS_TYPE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_DUE_DATE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_IS_COMPLETE + " INTEGER";
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
            onCreate(db);

        }
    }
}
