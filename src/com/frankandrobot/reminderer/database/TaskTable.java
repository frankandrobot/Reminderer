package com.frankandrobot.reminderer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.util.Log;

import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.EnumSet;
import java.util.HashMap;

/**
 * Columns for the {@link Task} model
 *
 * To add a new column,
 *
 * 1. add field to {@link Task} datastructure
 * 2. update Task(Cursor)
 * 3. update Task#getContentValuesForInsert
 * 4. update Task#get*FromInitial
 */
final public class TaskTable
{
    public final static String TASK_TABLE = "task";
    public final static String REPEATABLE_TABLE = "repeatable";
    public final static String FOLDER_TABLE = "folder";

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
        if (aTables == null)
            throw new IllegalArgumentException("Need a table");

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
            aStrCols[len++] = col != null ? col.colname() : "NULL";
        return aStrCols;
    }

    public enum TaskCol implements Column
    {
        TASK_ID("_id")
        , TASK_DESC
        , TASK_DUE_DATE
        , TASK_REPEAT_TYPE
        , TASK_IS_COMPLETE
        , TASK_FOLDER_ID_PK;

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
        ,REPEAT_TASK_ID_FK
        ,REPEAT_NEXT_DUE_DATE;

        @Override
        public String colname() { return name(); }
    }

    public enum FolderCol implements Column
    {
        FOLDER_ID("_id")
        ,FOLDER_NAME;

        private String colname;
        FolderCol() {}
        FolderCol(String value) { this.colname = value; }
        /**
         * @deprecated use {@link #colname}
         * @return string
         */
        @Override
        public String toString() { return colname == null ? super.toString() : colname; }
        @Override
        public String colname() { return colname == null ? name() : colname; }
    }

    /**
     * Used to create and upgrade the database
     */
    static class TaskTableHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "reminderer.db";
        private static final int DATABASE_VERSION = 4;
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
            dbCreateString += TaskCol.TASK_DUE_DATE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_REPEAT_TYPE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_IS_COMPLETE + " INTEGER, ";
            dbCreateString += TaskCol.TASK_FOLDER_ID_PK + " INTEGER NOT NULL DEFAULT 1";
            dbCreateString += ");";
            db.execSQL(dbCreateString);

            dbCreateString = "";
            dbCreateString += "CREATE TABLE " + REPEATABLE_TABLE;
            dbCreateString += "(";
            dbCreateString += RepeatsCol.REPEAT_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
            dbCreateString += RepeatsCol.REPEAT_TASK_ID_FK + " INTEGER, ";
            dbCreateString += RepeatsCol.REPEAT_NEXT_DUE_DATE + " INTEGER ";
            dbCreateString += ");";
            db.execSQL(dbCreateString);

            //create folder table
            dbCreateString = "";
            dbCreateString += "CREATE TABLE " + FOLDER_TABLE;
            dbCreateString += "(";
            dbCreateString += FolderCol.FOLDER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
            dbCreateString += FolderCol.FOLDER_NAME + " TEXT NOT NULL ";
            dbCreateString += ");";
            db.execSQL(dbCreateString);

            //create default folders
            ContentValues values = new ContentValues();

            values.put(FolderCol.FOLDER_NAME.colname(), "Inbox");
            db.insert(FOLDER_TABLE, null, values);
            values.put(FolderCol.FOLDER_NAME.colname(), "Personal");
            db.insert(FOLDER_TABLE, null, values);
            values.put(FolderCol.FOLDER_NAME.colname(), "Work");
            db.insert(FOLDER_TABLE, null, values);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion,
                              int currentVersion)
        {
            if (Logger.LOGV)
                Log.v(TAG, "Upgrading database from version " + oldVersion
                                   + " to " + currentVersion
                                   + ", which will destroy all old data");

            //drop old tables
            db.execSQL("DROP TABLE IF EXISTS "+FOLDER_TABLE);

            //backup data
            db.execSQL("ALTER TABLE " + TASK_TABLE + " RENAME TO table1");
            db.execSQL("ALTER TABLE " + REPEATABLE_TABLE + " RENAME TO table2");

            //create tables
            onCreate(db);

            //copy old data into new
            db.execSQL("INSERT INTO " + TASK_TABLE + " SELECT * FROM table1");
            db.execSQL("INSERT INTO " + REPEATABLE_TABLE + " SELECT * FROM table2");

            //drop temp tables
            db.execSQL("DROP TABLE IF EXISTS table1");
            db.execSQL("DROP TABLE IF EXISTS table2");


        }
    }
}
