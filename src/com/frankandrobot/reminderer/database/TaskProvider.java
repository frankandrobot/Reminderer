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

import com.frankandrobot.reminderer.alarm.AlarmManager;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.HashMap;

import static com.frankandrobot.reminderer.database.TaskTable.REPEATABLE_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_NEXT_DUE_DATE;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_TASK_ID_FK;
import static com.frankandrobot.reminderer.database.TaskTable.TASK_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_DESC;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_DUE_DATE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_ID;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_IS_COMPLETE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_REPEAT_TYPE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskTableHelper;

/**
 * <p>The DAO without the DAO..</p>
 */
public class TaskProvider extends ContentProvider
{
    final private static String TAG = "R:Provider";

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
     * Provides a view that is a join of the task and repeat table
     */
    public final static Uri TASK_JOIN_REPEAT_URI = Uri.parse(baseUri + "taskjoinrepeat");
    /**
     * Convenience Uri to get a view of open tasks
     */
    public final static Uri LOAD_OPEN_TASKS_URI = Uri.parse(baseUri + "loadopentasks");
    /**
     * Convenience Uri to get a view of due tasks
     */
    public final static Uri LOAD_DUE_TASKS_URI = Uri.parse(baseUri + "loadduetasks");


    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TASKS_URI_ID = 0;
    private static HashMap<Integer, TaskQuery> hmQueries = new HashMap<Integer, TaskQuery>();
    private static int uriCount = 0;

    static
    {
        addUri(CONTENT_URI, new TaskUriQuery());
        addUri(TASK_JOIN_REPEAT_URI, new TaskJoinRepeatQuery());
        addUri(LOAD_OPEN_TASKS_URI, new LoadOpenTasksQuery());
        addUri(LOAD_DUE_TASKS_URI, new LoadDueTasksQuery());
    }

    private SQLiteOpenHelper mOpenHelper;

    public TaskProvider() {}

    private static void addUri(Uri uri, TaskQuery query)
    {
        uriMatcher.addURI(uri.getAuthority(),
                          uri.getPath().substring(1),
                          uriCount++);
        hmQueries.put(uriMatcher.match(uri), query);
    }

    public static String convertArrayToString(String[] array)
    {
        String str = "";
        for (int i = 0; i < array.length; i++)
        {
            str += array[i];
            if (i < array.length - 1)
            {
                str = str + ",";
            }
        }
        return str;
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
        if (Logger.LOGV) Log.v(TAG, "query() ");

        TaskQuery taskQuery = hmQueries.get(uriMatcher.match(url));

        Cursor ret = taskQuery.query(mOpenHelper,
                                     url,
                                     projectionIn,
                                     selection,
                                     selectionArgs,
                                     sort);

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
                count = db.update(TaskTable.TASK_TABLE,
                                  values,
                                  where,
                                  whereArgs);
                break;
            }
            default:
            {
                throw new IllegalArgumentException("Cannot update URL: " + url);
            }
        }

        if (Logger.LOGV) Log.v(TAG,
                               "*** notifyChange() rowId: " + rowId + " url " + url + "count");

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

        if (initialValues.containsKey(TASK_REPEAT_TYPE.colname()) && initialValues.getAsInteger(TASK_REPEAT_TYPE.colname()) != 0)
        {
            ContentValues repeatValues = Task.getRepeatValuesFromInitial(initialValues);
            repeatValues.put(REPEAT_TASK_ID_FK.colname(), taskId);
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

    private interface TaskQuery
    {
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort);
    }

    static private class TaskUriQuery implements TaskQuery
    {

        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort)
        {
            SQLiteDatabase db = openHelper.getReadableDatabase();
            SQLiteQueryBuilder qb = new SQLiteQueryBuilder();
            qb.setTables(TASK_TABLE);
            return qb.query(db,
                            projectionIn,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sort);

        }
    }

    /**
     * Task query class for the task and repeat table join
     */
    static private class TaskJoinRepeatQuery implements TaskQuery
    {
        /**
         * Does a join on the task and repeat table.
         * You can use the projection to control the columns queried,
         * as well as the selection and sort.
         *
         * @note it looks like we're ignoring sort (for now)
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort)
        {
            SQLiteDatabase db = openHelper.getReadableDatabase();
            String rawQuery = selection != null ?
                              String.format("SELECT %s FROM %s,%s WHERE %s",
                                            convertArrayToString(projectionIn),
                                            TaskTable.TASK_TABLE,
                                            TaskTable.REPEATABLE_TABLE,
                                            selection)
                                      : String.format("SELECT %s FROM %s,%s",
                                                      convertArrayToString(projectionIn),
                                                      TaskTable.TASK_TABLE,
                                                      TaskTable.REPEATABLE_TABLE);
            return db.rawQuery(rawQuery, selectionArgs);
        }
    }

    /**
     * Convenience class to return a view of open tasks.
     */
    static private class LoadOpenTasksQuery implements TaskQuery
    {
        /**
         * Uses the TaskUnionRepeatQuery class to return a view showing open tasks.
         *
         * @param projectionIn don't pass this in
         * @param selection don't pass this in
         * @param selectionArgs don't pass this in
         * @param sort don't pass this in
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort)
        {
            if (projectionIn != null || selection != null || selectionArgs != null || sort != null)
                throw new IllegalArgumentException("This query does not support any params");

            String[] realProjection = new TaskTable().getColumns(
                    //task table
                    TASK_ID,
                    TASK_DESC,
                    TASK_DUE_DATE,
                    TASK_REPEAT_TYPE,
                    null,
                    //repeat table
                    TASK_ID,
                    TASK_DESC,
                    REPEAT_NEXT_DUE_DATE,
                    TASK_REPEAT_TYPE,
                    REPEAT_TASK_ID_FK
            );

            String realSelection = TASK_IS_COMPLETE + "=0";
            String realOrder = TASK_DUE_DATE.colname();

            return new TaskUnionRepeatQuery().query(openHelper,
                                                    url,
                                                    realProjection,
                                                    realSelection,
                                                    null,
                                                    realOrder);
        }
    }

    /**
     * Convenience class to get a view of due tasks
     */
    static private class LoadDueTasksQuery implements TaskQuery
    {
        /**
         * Convenience class to get a view of due tasks

         * @param projectionIn don't pass this in
         * @param operator the only string this accepts is a {@link AlarmManager.CompareOp}.
         * @return cursor
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String operator,
                            String[] selectionArgs,
                            String sort)
        {
            if (projectionIn != null)
                throw new IllegalArgumentException("This query does not support projectin or selection params");

            String[] realProjection = new TaskTable().getColumns(TASK_ID,
                                                                 TASK_DUE_DATE,
                                                                 //... other table
                                                                 TASK_ID,
                                                                 REPEAT_NEXT_DUE_DATE);

            String realSelection = "";
            realSelection += TASK_IS_COMPLETE + "=0";
            realSelection += " AND ";
            realSelection += TASK_DUE_DATE + operator + "?";
            realSelection += TaskUnionRepeatQuery.SEPARATOR;
            realSelection += TASK_IS_COMPLETE + "=0";
            realSelection += " AND ";
            realSelection += REPEAT_NEXT_DUE_DATE + operator + "?";

            return new TaskUnionRepeatQuery().query(openHelper,
                                                    url,
                                                    realProjection,
                                                    realSelection,
                                                    selectionArgs,
                                                    sort);
        }
    }

    /**
     * Convenience class that does a union of tasks with a join of the task and repeat tables.
     * (i.e., task UNION (task JOIN repeat), i.e., non-repeating tasks UNION repeating tasks)
     *
     * Maps the first half of the projection to the task table.
     * Maps the second half of the projection to the repeat table.
     * Both tables share the selection args.
     * Sort is done on the union.
     *
     * @note the repeat table is actually a join with the task table!
     */
    static private class TaskUnionRepeatQuery implements TaskQuery
    {
        final static private String SEPARATOR = "###";

        /**
         * Convenience class that does a union of tasks with a join of the task and repeat tables.
         * (i.e., task UNION (task JOIN repeat), i.e., non-repeating tasks UNION repeating tasks)
         *
         * @param projectionIn maps the first half to the task table, the
         *                     the second half to the join
         * @param selection if you use the SEPARATOR, the first half gets mapped
         *                  to the task table, the second half to the join.
         *                  Otherwise, but receive the same selection
         * @param selectionArgs is duplicated. __We should instead split it__
         * @param sort sort argument
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort)
        {
            if (projectionIn.length % 2 != 0)
                throw new IllegalArgumentException("Not a multiple of 2. Are you using the right projection?");

            SQLiteDatabase db = openHelper.getReadableDatabase();

            String[] splitSelection = null;
            if (selection != null)
            {
                if (selection.matches("^.*" + SEPARATOR + ".*$"))
                {
                    splitSelection = selection.split(SEPARATOR);
                }
                else
                {
                    splitSelection = new String[]{selection, selection};
                }
            }

            String taskQuery = "";
            taskQuery += "SELECT ";
            //first half of projection arguments are for task table
            for (int i = 0; i < projectionIn.length / 2 - 1; ++i)
                taskQuery += projectionIn[i] + ",";
            taskQuery += projectionIn[projectionIn.length / 2 - 1];
            //end of selection
            taskQuery += " FROM ";
            taskQuery += TASK_TABLE;
            taskQuery += " WHERE ";
            taskQuery += TASK_REPEAT_TYPE + "=0";
            if (selection != null)
            {
                taskQuery += " AND ";
                taskQuery += splitSelection[0];
            }

            String repeatQuery = "";
            repeatQuery += "SELECT ";
            //second half of projection arguments are for repeat table
            for (int i = projectionIn.length / 2; i < projectionIn.length - 1; ++i)
                repeatQuery += projectionIn[i] + ",";
            repeatQuery += projectionIn[projectionIn.length - 1];
            //end of selection
            repeatQuery += " FROM ";
            repeatQuery += TASK_TABLE + "," + REPEATABLE_TABLE;
            repeatQuery += " WHERE ";
            repeatQuery += TASK_REPEAT_TYPE + ">0";
            if (selection != null)
            {
                repeatQuery += " AND ";
                repeatQuery += splitSelection[1];
            }

            String rawQuery = "";
            rawQuery += taskQuery + " UNION " + repeatQuery;
            rawQuery += " ORDER BY " + sort;

            String[] newSelectionArgs = null;
            if (selectionArgs != null)
            {
                newSelectionArgs = new String[2 * selectionArgs.length];
                int len = 0;
                for (String selectionArg : selectionArgs)
                    newSelectionArgs[len++] = selectionArg;
                for (String selectionArg : selectionArgs)
                    newSelectionArgs[len++] = selectionArg;
            }
            return db.rawQuery(rawQuery, newSelectionArgs);
        }
    }
}
