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

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.util.Log;

import com.frankandrobot.reminderer.helpers.Logger;

import static com.frankandrobot.reminderer.database.TaskTable.TASK_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import static com.frankandrobot.reminderer.database.TaskTable.TaskTableHelper;

/**
 * <p>The DAO without the DAO..</p>
 *
 */
public class TaskProvider extends ContentProvider
{
    private static String TAG = "R:Provider";

    /**
     * The authority name is the unique identifier of this provideer
     */
    public final static String AUTHORITY_NAME = "com.frankandrobot.reminderer.dbprovider";
    public final static Uri DUEDATE_URI = Uri.parse("content://" + AUTHORITY_NAME
                                                            + "/" + TASK_TABLE + "/"
                                                            + TaskCol.TASK_DUE_DATE);
    // URIs
    public final static Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY_NAME
                                                            + "/" + TASK_TABLE);
    /**
     * A {@link UriMatcher} is a helper object that helps parse incoming
     * Uri requests.
     * <p/>
     * This uri matcher supports the following Uris:
     * <p/>
     * - com.frankandrobot.reminderer.dbprovider/tasks
     * - com.frankandrobot.reminderer.dbprovider/tasks/#
     * - com.frankandrobot.reminderer.dbprovider/duedate/#
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    /**
     * Get all tasks
     */
    private static final int TASKS_URI = 1;

    /**
     * Get task with specific ID
     */
    private static final int TASK_ID_URI = 2;

    /**
     * Get tasks due on specific date and time
     */
    private static final int TASKS_DUE_URI = 3;

    private static final int GPS_TASKS = 4;
    private static final int GPS_TASKS_ID = 5;

    static
    {
        // com.frankandrobot.reminderer.dbprovider/tasks
        uriMatcher.addURI(AUTHORITY_NAME, TASK_TABLE, TASKS_URI);
        uriMatcher.addURI(AUTHORITY_NAME,
                          TASK_TABLE + "/#",
                          TASK_ID_URI);
        uriMatcher.addURI(AUTHORITY_NAME,
                          TASK_TABLE + "/duedate/#",
                          TASKS_DUE_URI);
        uriMatcher.addURI(AUTHORITY_NAME, "gps_tasks", GPS_TASKS);
        uriMatcher.addURI(AUTHORITY_NAME, "gps_tasks/#", GPS_TASKS_ID);
    }

    private SQLiteOpenHelper mOpenHelper;

    public TaskProvider()
    {
    }

    @Override
    public boolean onCreate()
    {
        mOpenHelper = new TaskTableHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri url,
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
                qb.setTables(TASK_TABLE);
                break;
            case TASK_ID_URI: // query is for specific task
                qb.setTables(TASK_TABLE);
                qb.appendWhere(TaskCol.TASK_ID + "=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            case TASKS_DUE_URI: // query is for specific task
                qb.setTables(TASK_TABLE);
                qb.appendWhere(TaskCol.TASK_DUE_DATE + "=");
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
    }


    @Override
    public int update(Uri url,
                      ContentValues values,
                      String where,
                      String[] whereArgs)
    {
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        int count;
        long rowId = 0;

        switch (uriMatcher.match(url))
        {
            case TASKS_URI:
            {
                count = db.update(TaskTable.TASK_TABLE, values, where, whereArgs);
                break;
            }
            case TASK_ID_URI:
            {
                // "com..frankandrobot.reminderer.dbprovider/tasks/#"
                rowId = Long.parseLong(url.getPathSegments().get(1));
                count = db.update(TaskTable.TASK_TABLE, values, "_id=" + rowId, null);
                break;
            }

            default:
            {
                throw new IllegalArgumentException("Cannot update URL: " + url);
            }
        }

        if (Logger.LOGV)
            Log.v(TAG, "*** notifyChange() rowId: " + rowId + " url " + url + "count");

        getContext().getContentResolver().notifyChange(url, null);

        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues)
    {
        if (Logger.LOGV)
            Log.v(TAG, "Inserting values " + url.toString());

        if (uriMatcher.match(url) != TASKS_URI)
        {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }
        //
        // ContentValues values;
        // if (initialValues != null)
        // values = new ContentValues(initialValues);
        // else
        // values = new ContentValues();
        //
        // if (!values.containsKey(alarm.Columns.HOUR))
        // values.put(alarm.Columns.HOUR, 0);
        //
        // if (!values.containsKey(alarm.Columns.MINUTES))
        // values.put(alarm.Columns.MINUTES, 0);
        //
        // if (!values.containsKey(alarm.Columns.DAYS_OF_WEEK))
        // values.put(alarm.Columns.DAYS_OF_WEEK, 0);
        //
        // if (!values.containsKey(alarm.Columns.ALARM_TIME))
        // values.put(alarm.Columns.ALARM_TIME, 0);
        //
        // if (!values.containsKey(alarm.Columns.ENABLED))
        // values.put(alarm.Columns.ENABLED, 0);
        //
        // if (!values.containsKey(alarm.Columns.VIBRATE))
        // values.put(alarm.Columns.VIBRATE, 1);
        //
        // if (!values.containsKey(alarm.Columns.MESSAGE))
        // values.put(alarm.Columns.MESSAGE, "");
        //
        // if (!values.containsKey(alarm.Columns.ALERT))
        // values.put(alarm.Columns.ALERT, "");
        //
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        long rowId = db.insert(TASK_TABLE, null, initialValues);
        if (rowId < 0)
        {
            throw new SQLException("Failed to insert row into " + url);
        }
        if (Logger.LOGV)
            Log.v(TAG, "Added task rowId = " + rowId);
        Uri newUrl = ContentUris.withAppendedId(CONTENT_URI, rowId);
        getContext().getContentResolver().notifyChange(newUrl, null);
        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs)
    {
        return 0;
        // SQLiteDatabase db = mOpenHelper.getWritableDatabase();
        // int count;
        // long rowId = 0;
        // switch (uriMatcher.match(url)) {
        // case ALARMS:
        // count = db.delete("alarms", where, whereArgs);
        // break;
        // case ALARMS_ID:
        // String segment = url.getPathSegments().get(1);
        // rowId = Long.parseLong(segment);
        // if (TextUtils.isEmpty(where)) {
        // where = "_id=" + segment;
        // } else {
        // where = "_id=" + segment + " AND (" + where + ")";
        // }
        // count = db.delete("alarms", where, whereArgs);
        // break;
        // default:
        // throw new IllegalArgumentException("Cannot delete from URL: " + url);
        // }
        //
        // getContext().getContentResolver().notifyChange(url, null);
        // return count;
    }

    /**
     * Required. Returns the MIME type of the Uri.
     *
     * @param url
     * @return
     */
    @Override
    public String getType(Uri url)
    {
        switch (uriMatcher.match(url))
        {
            case TASKS_URI:
                return "vnd.android.cursor.dir/" + AUTHORITY_NAME + "." + TASK_TABLE;
            case TASK_ID_URI:
                return "vnd.android.cursor.item/" + AUTHORITY_NAME + "." + TASK_TABLE;
            case TASKS_DUE_URI:
                return "vnd.android.cursor.item/" + AUTHORITY_NAME + "."
                               + TASK_TABLE + ".duedate";
            default:
                throw new IllegalArgumentException("Unknown URII");
        }
    }

}
