/*
 * Copyright (C) 2007 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.frankandrobot.reminderer.database;

import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.util.Log;

import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import static com.frankandrobot.reminderer.database.DbColumns.*;

/**
 * Responsible for the CRUD.
 */
public class TaskDAO
{
    private static String TAG = "R:" + TaskDAO.class.getSimpleName();
    private Context context;
    private SQLiteOpenHelper mOpenHelper;

    public TaskDAO(Context context)
    {
        this.context = context;
        mOpenHelper = new TaskDAOHelper(context);
    }

    /*public Cursor query(Uri url,
                        String[] projectionIn,
                        String selection,
                        String[] selectionArgs,
                        String sort)
    {
        if (Logger.LOGV)
            Log.v(TAG, "query() ");

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        // Generate the body of the query
        switch (uriMatcher.match(url))
        {
            case TASKS_URI: // query is for all tasks
                qb.setTables(DbColumns.TASK_TABLE);
                break;
            case TASK_ID_URI: // query is for specific task
                qb.setTables(DbColumns.TASK_TABLE);
                qb.appendWhere(DbColumns.TASK_ID + "=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            case TASKS_DUE_URI: // query is for specific task
                qb.setTables(DbColumns.TASK_TABLE);
                qb.appendWhere(DbColumns.TASK_DUE_DATE + "=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            default:
                throw new IllegalArgumentException("Unknown URI " + url);
        }

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null,
                              null, sort);

        if (ret == null && Logger.LOGV)
        {
            Log.v(TAG, "query: failed");
        }
        else
        {
            ret.setNotificationUri(getContext().getContentResolver(), url);
        }

        return ret;
    }*/


    /*@Override
    public int update(Uri url,
                      ContentValues values,
                      String where,
                      String[] whereArgs)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        long rowId;

        switch (uriMatcher.match(url))
        {
            case TASK_ID_URI:
            {
                // "com..frankandrobot.reminderer.dbprovider/tasks/#"
                rowId = Long.parseLong(url.getPathSegments().get(1));
                count = db.update("tasks", values, "_id=" + rowId, null);
                break;
            }

            default:
            {
                throw new IllegalArgumentException("Cannot update URL: " + url);
            }
        }

        if (Logger.LOGV)
            Log.v(TAG, "*** notifyChange() rowId: " + rowId + " url " + url);

        getContext().getContentResolver().notifyChange(url, null);

        return count;
    }*/

    public Task create(Task task)
    {
        if (Logger.LOGV) Log.v(TAG, "Inserting task " + task.toString());

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long rowId = db.insert(TASK_TABLE,
                               null,
                               task.toContentValues());

        if (rowId < 0) throw new SQLException("Failed to insert row");
        else if (Logger.LOGV) Log.v(TAG, "Added task rowId = " + rowId);

        return (Task) task.set(Task.Task_Long.id, rowId);
    }

    public Cursor find(final String taskID)
    {
        SQLiteDatabase db = mOpenHelper.getReadableDatabase();

        Cursor cursor = db.query(TASK_TABLE,
                                 TaskCol.getAllColumns(),
                                 TaskCol.TASK_ID + "=?",
                                 new String[]{String.valueOf(taskID)},
                                 null,
                                 null,
                                 null,
                                 null);

        return cursor;
    }

    public void delete(Task task)
    {
         SQLiteDatabase db = mOpenHelper.getWritableDatabase();
         int count = db.delete(TASK_TABLE,
                               TaskCol.TASK_ID + "= ?",
                               new String[]{String.valueOf(task.getId())});

         if (count <= 0) throw new SQLException("Unable to delete task");
         else if (count > 1) throw new SQLException("Wierd. More than one deleted task had the same id.");    }

    public void update(Task task)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        int count = db.update(TASK_TABLE,
                              task.toContentValues(),
                              TaskCol.TASK_ID + "= ?",
                              new String[]{String.valueOf(task.getId())});

        if (count <= 0) throw new SQLException("Unable to update task");
        else if (count > 1) throw new SQLException("Wierd. More than one updated task had the same id.");
    }

    /**
     * Used to create and upgrade the database
     */
    private static class TaskDAOHelper extends SQLiteOpenHelper
    {
        private static final String DATABASE_NAME = "reminderer.db";
        private static final int DATABASE_VERSION = 2;

        public TaskDAOHelper(Context context)
        {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            String dbCreateString = "";
            dbCreateString += "CREATE TABLE " + TASK_TABLE;
            dbCreateString += "(";
            dbCreateString += TaskCol.TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,";
            dbCreateString += TaskCol.TASK_DESC + " TEXT NOT NULL, ";
            dbCreateString += TaskCol.TASK_REPEATS_TYPE + " TEXT, ";
            dbCreateString += TaskCol.TASK_DUE_DATE + " INTEGER";
            dbCreateString += ");";
            if (Logger.LOGV) Log.v(TAG, "dbCreateString:" + dbCreateString);
            db.execSQL(dbCreateString);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db,
                              int oldVersion,
                              int currentVersion)
        {
            if (Logger.LOGV) Log.v(TAG,
                                   "Upgrading database from version " + oldVersion + " to " + currentVersion + ", which will destroy all old data");
            // TODO mike fix
            db.execSQL("DROP TABLE IF EXISTS " + TASK_TABLE);
            onCreate(db);
        }
    }
}
