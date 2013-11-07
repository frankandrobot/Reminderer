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
import android.text.TextUtils;
import android.util.Log;

import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.HashMap;

import static com.frankandrobot.reminderer.database.TaskTable.FOLDER_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.REPEATABLE_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_ID;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_NEXT_DUE_DATE;
import static com.frankandrobot.reminderer.database.TaskTable.RepeatsCol.REPEAT_TASK_ID_FK;
import static com.frankandrobot.reminderer.database.TaskTable.TASK_TABLE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_DESC;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_DUE_DATE;
import static com.frankandrobot.reminderer.database.TaskTable.TaskCol.TASK_FOLDER_ID_PK;
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
     * Gives access to task table
     */
    public final static Uri TASKS_URI = Uri.parse(baseUri + TASK_TABLE);
    /**
     * Gives access to repeatable table
     */
    public final static Uri REPEAT_URI = Uri.parse(baseUri + TASK_TABLE + "/" + REPEATABLE_TABLE);
    /**
     * Provides a view that is a join of the task and repeat table
     */
    public final static Uri TASK_JOIN_REPEAT_URI = Uri.parse(baseUri+TASK_TABLE+"/views/taskjoinrepeat");
    /**
     * Convenience Uri to get a view of open tasks
     */
    public final static Uri LOAD_OPEN_TASKS_URI = Uri.parse(baseUri+TASK_TABLE+"/views/loadopentasks");
    /**
     * Convenience Uri to get the next due times
     */
    public final static Uri LOAD_DUE_TIMES_URI = Uri.parse(baseUri+TASK_TABLE+"/views/loadduetimes");
    /**
     * Gives access to the folders table
     */
    public final static Uri FOLDERS_URI = Uri.parse(baseUri + "folders");
    /**
     * Provides a view that is the union of the task and the task/repeat join view
     */
    public final static Uri TASK_UNION_REPEAT_URI = Uri.parse(baseUri+TASK_TABLE+"/views/taskunionrepeat");

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int TASKS_URI_ID = 0;
    private static HashMap<Integer, UriProvider> hmQueries = new HashMap<Integer, UriProvider>();
    private static int uriCount = 0;

    static
    {
        addUri(TASKS_URI, new TaskUriProvider(), false);
        addUri(REPEAT_URI, new RepeatUriProvider(), false);
        addUri(REPEAT_URI, new SingleRowRepeatUriProvider(), true);
        addUri(TASK_JOIN_REPEAT_URI, new TaskJoinRepeatProvider(), false);
        addUri(LOAD_OPEN_TASKS_URI, new LoadOpenTasksProvider(), false);
        addUri(LOAD_DUE_TIMES_URI, new LoadDueTimesProvider(), false);
        addUri(FOLDERS_URI, new FoldersUriProvider(), false);
        addUri(TASK_UNION_REPEAT_URI, new TaskUnionRepeatQuery(), false);
    }

    /**
     * The problem with using different Uris for different views of the same table
     * is that updating registered observers doesn't work as expected
     *
     * Ex: chaing the task table means updating observers for all Uris of this table.
     *
     */
    public enum CompareOp
    {
        AFTER(">")
        ,ON_OR_AFTER(">=")
        ,ON("=");

        CompareOp(String val) { this.val = val; }
        private String val;

        @Override
        public String toString() { return val; }
    }

    private SQLiteOpenHelper mOpenHelper;

    public TaskProvider() {}

    private static void addUri(Uri uri, UriProvider query, boolean isSingleRow)
    {
        uriMatcher.addURI(uri.getAuthority(),
                          uri.getPath().substring(1)+(isSingleRow?"/#":""),
                          uriCount);
        hmQueries.put(uriCount++, query);
    }

    protected static String convertArrayToString(String[] array)
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

        UriQueryProvider uriProvider = (UriQueryProvider)
                                               hmQueries.get(uriMatcher.match(url));

        Cursor ret = uriProvider.query(mOpenHelper,
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

        UriUpdateProvider uriProvider = (UriUpdateProvider)
                                                hmQueries.get(uriMatcher.match(url));

        int count = uriProvider.update(mOpenHelper, url, values, where, whereArgs);

        //getContext().getContentResolver().notifyChange(url, null);

        return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues)
    {
        if (Logger.LOGD) Log.d(TAG, "Inserting values " + url.toString());

        UriInsertProvider uriProvider = (UriInsertProvider)
                                                hmQueries.get(uriMatcher.match(url));

        Uri newUrl = uriProvider.insert(mOpenHelper, url, initialValues);

        getContext().getContentResolver().notifyChange(newUrl, null);

        if (url.getPath().startsWith("/"+TASK_TABLE))
            getContext().getContentResolver().notifyChange(TASKS_URI, null);

        return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs)
    {
        UriDeleteProvider uriProvider = (UriDeleteProvider)
                                                hmQueries.get(uriMatcher.match(url));

        return uriProvider.delete(mOpenHelper, url, where, whereArgs);
    }

    /**
     * Required. Returns the MIME type of the Uri.
     *
     * @param url the url
     * @return mime type
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
                throw new IllegalArgumentException("Unknown URI");
        }
    }

    interface UriProvider {}

    interface UriUpdateProvider extends UriProvider
    {
        public int update(SQLiteOpenHelper openHelper,
                          Uri url,
                          ContentValues values,
                          String where,
                          String[] whereArgs);
    }

    interface UriInsertProvider extends UriProvider
    {
        public Uri insert(SQLiteOpenHelper openHelper,
                          Uri url,
                          ContentValues initialValues);
    }

    interface UriQueryProvider extends UriProvider
    {
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort);
    }

    interface UriDeleteProvider extends UriProvider
    {
        public int delete(SQLiteOpenHelper openHelper,
                          Uri url,
                          String where,
                          String[] whereArgs);
    }

    static private class TaskUriProvider
            implements
            UriUpdateProvider,
            UriInsertProvider,
            UriQueryProvider
    {
        @Override
        public Uri insert(SQLiteOpenHelper openHelper, Uri url, ContentValues initialValues)
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();

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

            return ContentUris.withAppendedId(TASKS_URI, taskId);
        }

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

        @Override
        public int update(SQLiteOpenHelper openHelper,
                          Uri url,
                          ContentValues values,
                          String where,
                          String[] whereArgs)
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();

            int count = db.update(TASK_TABLE,
                                  values,
                                  where,
                                  whereArgs);

            if (Logger.LOGV) Log.v(TAG,
                                   "*** notifyChange() url " + url + ", count " + count);

            return count;
        }
    }

    /**
     * Task query class for the task and repeat table join
     */
    static protected class TaskJoinRepeatProvider
            implements
            UriQueryProvider
    {
        /**
         * Does a join on the task and repeat table.
         * You can use the projection to control the columns queried,
         * as well as the selection and sort.
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
            String joinCondition = TASK_ID+"="+REPEAT_TASK_ID_FK;
            String rawQuery = String.format("SELECT %s FROM %s,%s WHERE %s %s ORDER BY %s",
                                            convertArrayToString(projectionIn),
                                            TaskTable.TASK_TABLE,
                                            TaskTable.REPEATABLE_TABLE,
                                            joinCondition,
                                            selection!=null ? "AND "+selection:"",
                                            sort);
            return db.rawQuery(rawQuery, selectionArgs);
        }
    }

    /**
     * Convenience class to return a view of open tasks.
     */
    static private class LoadOpenTasksProvider
        implements
        UriQueryProvider
    {
        /**
         * Uses the TaskUnionRepeatQuery class to return a view showing open tasks.
         *
         * @param projectionIn don't pass this in
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String selection,
                            String[] selectionArgs,
                            String sort)
        {
            if (projectionIn != null)
                throw new IllegalArgumentException("This query does not support any params");

            String[] realProjection = new TaskTable().getColumns(
                    //task table
                    TASK_ID,
                    TASK_DESC,
                    TASK_DUE_DATE,
                    TASK_REPEAT_TYPE,
                    null,
                    TASK_FOLDER_ID_PK,
                    //task table join repeat table
                    TASK_ID,
                    TASK_DESC,
                    REPEAT_NEXT_DUE_DATE,
                    TASK_REPEAT_TYPE,
                    REPEAT_ID,
                    TASK_FOLDER_ID_PK
            );

            String realSelection = selection == null
                                           ? TASK_IS_COMPLETE+"=0"
                                           : selection+" AND "+TASK_IS_COMPLETE+"=0";
            String realOrder = sort == null
                                       ? TASK_DUE_DATE+","+TASK_ID
                                       : sort;

            return new TaskUnionRepeatQuery().query(openHelper,
                                                    url,
                                                    realProjection,
                                                    realSelection,
                                                    selectionArgs,
                                                    realOrder);
        }
    }

    /**
     * Convenience class to get a view of due times
     *
     * @note returns no other columns except the due time
     *
     */
    static private class LoadDueTimesProvider
        implements
        UriQueryProvider
    {
        /**
         * Convenience class to get a view of due tasks

         * @param projectionIn don't pass this in
         * @param operator the only string this accepts is a {@link com.frankandrobot.reminderer.database.TaskProvider.CompareOp}.
         * @param dueTime you can pass only the due time
         * @param sort you can sort only by TASK_ID, TASK_DUE_DATE, REPEAT_NEXT_DUE_DATE
         * @return cursor
         */
        @Override
        public Cursor query(SQLiteOpenHelper openHelper,
                            Uri url,
                            String[] projectionIn,
                            String operator,
                            String[] dueTime,
                            String sort)
        {
            if (projectionIn != null)
                throw new IllegalArgumentException("This query does not support projections");

            String[] realProjection = new TaskTable().getColumns(TASK_DUE_DATE,
                                                                 //... other table
                                                                 REPEAT_NEXT_DUE_DATE);
            //add distinct clause
            realProjection[0] = "DISTINCT "+realProjection[0];
            realProjection[1] = "DISTINCT "+realProjection[1];

            String realSelection = "";
            realSelection += TASK_IS_COMPLETE + "=0";
            realSelection += " AND ";
            realSelection += TASK_DUE_DATE + operator + "?"; //lower bound
            realSelection += TaskUnionRepeatQuery.SEPARATOR;
            realSelection += TASK_IS_COMPLETE + "=0";
            realSelection += " AND ";
            realSelection += REPEAT_NEXT_DUE_DATE + operator + "?"; //lower bound

            return new TaskUnionRepeatQuery().query(openHelper,
                                                    url,
                                                    realProjection,
                                                    realSelection,
                                                    dueTime,
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
    public static class TaskUnionRepeatQuery
        implements UriQueryProvider
    {
        final static public String SEPARATOR = "###";

        /**
         * Convenience class that does a union of tasks with a join of the task and repeat tables.
         * (i.e., task UNION (task JOIN repeat), i.e., non-repeating tasks UNION repeating tasks)
         *
         * @param projectionIn maps the first half to the task table, the
         *                     the second half to the join
         * @param selection if you use the SEPARATOR, the first half gets mapped
         *                  to the task table, the second half to the join.
         *                  Otherwise, both receive the same selection
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
            repeatQuery += " AND "+TASK_ID+"="+REPEAT_TASK_ID_FK;
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

    static class RepeatUriProvider
        implements
        UriInsertProvider,
        UriQueryProvider
    {
        @Override
        public Uri insert(SQLiteOpenHelper openHelper, Uri url, ContentValues initialValues)
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();

            long repeatId = db.insert(REPEATABLE_TABLE,
                                      null,
                                      initialValues);

            if (repeatId < 0)
            {
                throw new SQLException("Failed to insert row into " + url);
            }

            if (Logger.LOGD) Log.d(TAG, "Added repeatable repeatId = " + repeatId);

            return ContentUris.withAppendedId(REPEAT_URI, repeatId);
        }

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
            qb.setTables(REPEATABLE_TABLE);
            return qb.query(db,
                    projectionIn,
                    selection,
                    selectionArgs,
                    null,
                    null,
                    sort);
        }
    }

    static class SingleRowRepeatUriProvider
            implements
            UriDeleteProvider
    {
        @Override
        public int delete(SQLiteOpenHelper openHelper, Uri url, String where, String[] whereArgs)
        {
            SQLiteDatabase db = openHelper.getWritableDatabase();

            String rowId = url.getPathSegments().get(1);
            if (TextUtils.isEmpty(where))
            {
                where = REPEAT_ID+"="+rowId;
            }
            else
            {
                where = REPEAT_ID+"="+rowId + " AND (" + where + ")";
            }
            return db.delete(REPEATABLE_TABLE, where, whereArgs);
        }
    }

    static class FoldersUriProvider
        implements
        UriQueryProvider
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
            qb.setTables(FOLDER_TABLE);
            return qb.query(db,
                            projectionIn,
                            selection,
                            selectionArgs,
                            null,
                            null,
                            sort);
        }
    }
}
