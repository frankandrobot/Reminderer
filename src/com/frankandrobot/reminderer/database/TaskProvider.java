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

import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import static com.frankandrobot.reminderer.database.TaskTable.REPEATABLE_TABLE;
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

    private final static String baseUri = "content://" + AUTHORITY_NAME + "/";

    /**
     * {@link Uri}s provide different views of different tables
     */
    public final static Uri CONTENT_URI = Uri.parse(baseUri + TASK_TABLE);

    /**
     * View that uses all columns from the task table and its related tables
     */
    public final static Uri VIEW_EVERYTHING_URI = Uri.parse(baseUri + "task/allcolumns");

    /**
     * A {@link UriMatcher} is a helper object that helps parse incoming
     * Uri requests.
     *
     * Example:
     *
     * - com.frankandrobot.reminderer.dbprovider/tasks
     * - com.frankandrobot.reminderer.dbprovider/tasks/#
     * - com.frankandrobot.reminderer.dbprovider/duedate/#
     */
    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    private static final int TASKS_URI_ID = 1;
    private static final int VIEW_EVERYTHING_URI_ID = 2;


    static
    {
        uriMatcher.addURI(AUTHORITY_NAME, TASK_TABLE, TASKS_URI_ID);
        uriMatcher.addURI(VIEW_EVERYTHING_URI.getAuthority(),
                          VIEW_EVERYTHING_URI.getPath().substring(1),
                          VIEW_EVERYTHING_URI_ID);
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

        SQLiteDatabase db = mOpenHelper.getReadableDatabase();
        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
        Cursor ret;
        String rawQuery;

        switch (uriMatcher.match(url))
        {
            case TASKS_URI_ID:
                qb.setTables(TASK_TABLE);
                ret = qb.query(db,
                               projectionIn,
                               selection,
                               selectionArgs,
                               null,
                               null,
                               sort);

                break;
            case VIEW_EVERYTHING_URI_ID:
                rawQuery = "SELECT * FROM "+TaskTable.TASK_TABLE+","+TaskTable.REPEATABLE_TABLE;
                ret = db.rawQuery(rawQuery, null);
                break;
            /*case TASK_ID_URI: // query is for specific task
                qb.setTables(TASK_TABLE);
                qb.appendWhere(TaskCol.TASK_ID + "=");
                qb.appendWhere(url.getPathSegments().get(1));
                break;
            */
            default:
                throw new IllegalArgumentException("Unknown URI " + url);
        }

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
            case TASKS_URI_ID:
            {
                count = db.update(TaskTable.TASK_TABLE, values, where, whereArgs);
                break;
            }
            /*case TASK_ID_URI:
            {
                // "com..frankandrobot.reminderer.dbprovider/tasks/#"
                rowId = Long.parseLong(url.getPathSegments().get(1));
                count = db.update(TaskTable.TASK_TABLE, values, "_id=" + rowId, null);
                break;
            }

*/            default:
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
        if (uriMatcher.match(url) != TASKS_URI_ID)
        {
            throw new IllegalArgumentException("Cannot insert into URL: " + url);
        }

        if (Logger.LOGD) Log.d(TAG, "Inserting values " + url.toString());

        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

        long taskId = db.insert(TASK_TABLE,
                                null,
                                Task.getTaskValuesFromInitial(initialValues));

        if (taskId < 0)
        {
            throw new SQLException("Failed to insert row into " + url);
        }

        if (initialValues.containsKey(TaskCol.TASK_REPEAT_TYPE.colname())
                    && initialValues.getAsInteger(TaskCol.TASK_REPEAT_TYPE.colname()) != 0)
        {
            ContentValues repeatValues = Task.getRepeatValuesFromInitial(initialValues);
            repeatValues.put(RepeatsCol.REPEAT_TASK_ID_FK.colname(), taskId);
            long repeatId = db.insert(REPEATABLE_TABLE, null, repeatValues);

            if (repeatId < 0)
            {
                throw new SQLException("Failed to insert row into " + url);
            }
        }

        if (Logger.LOGD) Log.d(TAG, "Added task rowId = " + taskId);

        Uri newUrl = ContentUris.withAppendedId(CONTENT_URI, taskId);
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
            case TASKS_URI_ID:
                return "vnd.android.cursor.dir/" + AUTHORITY_NAME + "." + TASK_TABLE;
            /*case TASK_ID_URI:
                return "vnd.android.cursor.item/" + AUTHORITY_NAME + "." + TASK_TABLE;*/
            default:
                throw new IllegalArgumentException("Unknown URII");
        }
    }

}
