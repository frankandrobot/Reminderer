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

package com.frankandrobot.reminderer.Database;

import com.frankandrobot.reminderer.Helpers.Logger;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.text.TextUtils;
import android.util.Log;

public class DatabaseProvider extends ContentProvider {
    private static String TAG = "R:Provider";
    private SQLiteOpenHelper mOpenHelper;

    // The DB will have tow tables: tasks and gps_tasks
    private static final int TASKS = 1;
    private static final int TASKS_ID = 2;
    private static final int TASKS_DUE = 3;
    private static final int GPS_TASKS = 4;
    private static final int GPS_TASKS_ID = 5;

    private static final UriMatcher sURLMatcher = new UriMatcher(
	    UriMatcher.NO_MATCH);

    static {
	// com.frankandrobot.reminderer.dbprovider/tasks
	sURLMatcher.addURI(DbColumns.AUTHORITY_NAME, DbColumns.TASK_TABLE,
		TASKS);
	sURLMatcher.addURI(DbColumns.AUTHORITY_NAME, DbColumns.TASK_TABLE
		+ "/#", TASKS_ID);
	sURLMatcher.addURI(DbColumns.AUTHORITY_NAME, DbColumns.TASK_TABLE
		+ "/duedate/#", TASKS_DUE);
	sURLMatcher.addURI(DbColumns.AUTHORITY_NAME, "gps_tasks", GPS_TASKS);
	sURLMatcher.addURI(DbColumns.AUTHORITY_NAME, "gps_tasks/#",
		GPS_TASKS_ID);

    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
	private static final String DATABASE_NAME = "reminderer.db";
	private static final int DATABASE_VERSION = 2;

	public DatabaseHelper(Context context) {
	    super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
	    String dbCreateString = "";
	    dbCreateString += "CREATE TABLE " + DbColumns.TASK_TABLE;
	    dbCreateString += "(";
	    dbCreateString += DbColumns.TASK_ID
		    + " INTEGER PRIMARY KEY AUTOINCREMENT,";
	    dbCreateString += DbColumns.TASK + " TEXT NOT NULL, ";
	    dbCreateString += DbColumns.TASK_DUE_DATE + " INTEGER";
	    dbCreateString += ");";
	    if (Logger.LOGV)
		Log.v(TAG, "dbCreateString:" + dbCreateString);
	    db.execSQL(dbCreateString);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion,
		int currentVersion) {
	    if (Logger.LOGV)
		Log.v(TAG, "Upgrading database from version " + oldVersion
			+ " to " + currentVersion
			+ ", which will destroy all old data");
	    // TODO mike fix
	    db.execSQL("DROP TABLE IF EXISTS "+DbColumns.TASK_TABLE);
	    onCreate(db);
	}
    }

    public DatabaseProvider() {
    }

    @Override
    public boolean onCreate() {
	mOpenHelper = new DatabaseHelper(getContext());
	return true;
    }

    @Override
    public Cursor query(Uri url, String[] projectionIn, String selection,
	    String[] selectionArgs, String sort) {
	if (Logger.LOGV)
	    Log.v(TAG,"query() ");
	SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

	// Generate the body of the query
	int match = sURLMatcher.match(url);
	switch (match) {
	case TASKS: // query is for all tasks
	    qb.setTables(DbColumns.TASK_TABLE);
	    break;
	case TASKS_ID: // query is for specific task
	    qb.setTables(DbColumns.TASK_TABLE);
	    qb.appendWhere(DbColumns.TASK_ID + "=");
	    qb.appendWhere(url.getPathSegments().get(1));
	    break;
	case TASKS_DUE: // query is for specific task
	    qb.setTables(DbColumns.TASK_TABLE);
	    qb.appendWhere(DbColumns.TASK_DUE_DATE + "=");
	    qb.appendWhere(url.getPathSegments().get(1));
	    break;
	default:
	    throw new IllegalArgumentException("Unknown URL " + url);
	}

	SQLiteDatabase db = mOpenHelper.getReadableDatabase();
	Cursor ret = qb.query(db, projectionIn, selection, selectionArgs, null,
		null, sort);

	if (ret == null) {
	    if (Logger.LOGV)
		Log.v(TAG, "query: failed");
	} else {
	    ret.setNotificationUri(getContext().getContentResolver(), url);
	}
	return ret;
    }

    @Override
    public String getType(Uri url) {
	int match = sURLMatcher.match(url);
	switch (match) {
	case TASKS:
	    return "vnd.android.cursor.dir/" + DbColumns.AUTHORITY_NAME + "."
		    + DbColumns.TASK_TABLE;
	case TASKS_ID:
	    return "vnd.android.cursor.item/" + DbColumns.AUTHORITY_NAME + "."
		    + DbColumns.TASK_TABLE;
	case TASKS_DUE:
	    return "vnd.android.cursor.item/" + DbColumns.AUTHORITY_NAME + "."
		    + DbColumns.TASK_TABLE + ".duedate";
	case GPS_TASKS:
	    return "vnd.android.cursor.dir/" + DbColumns.AUTHORITY_NAME
		    + ".gps_tasks";
	case GPS_TASKS_ID:
	    return "vnd.android.cursor.item/" + DbColumns.AUTHORITY_NAME
		    + ".gps_tasks";

	default:
	    throw new IllegalArgumentException("Unknown URL");
	}
    }

    @Override
    public int update(Uri url, ContentValues values, String where,
	    String[] whereArgs) {
	int count;
	long rowId = 0;
	int match = sURLMatcher.match(url);
	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	switch (match) {
	case TASKS_ID: {
	    // ex: "com..frankandrobot.reminderer.dbprovider/tasks/1"
	    String segment = url.getPathSegments().get(1);
	    rowId = Long.parseLong(segment);
	    count = db.update("tasks", values, "_id=" + rowId, null);
	    break;
	}
	case GPS_TASKS_ID: {
	    String segment = url.getPathSegments().get(1);
	    rowId = Long.parseLong(segment);
	    count = db.update("gps_tasks", values, "_id=" + rowId, null);
	    break;
	}
	default: {
	    throw new UnsupportedOperationException("Cannot update URL: " + url);
	}
	}
	if (Logger.LOGV)
	    Log.v(TAG, "*** notifyChange() rowId: " + rowId + " url " + url);
	getContext().getContentResolver().notifyChange(url, null);
	return count;
    }

    @Override
    public Uri insert(Uri url, ContentValues initialValues) {
	if (Logger.LOGV)
	    Log.v(TAG, "Inserting valuse "+url.toString());
	if (sURLMatcher.match(url) != TASKS) {
	    throw new IllegalArgumentException("Cannot insert into URL: " + url);
	}
	//
	// ContentValues values;
	// if (initialValues != null)
	// values = new ContentValues(initialValues);
	// else
	// values = new ContentValues();
	//
	// if (!values.containsKey(Alarm.Columns.HOUR))
	// values.put(Alarm.Columns.HOUR, 0);
	//
	// if (!values.containsKey(Alarm.Columns.MINUTES))
	// values.put(Alarm.Columns.MINUTES, 0);
	//
	// if (!values.containsKey(Alarm.Columns.DAYS_OF_WEEK))
	// values.put(Alarm.Columns.DAYS_OF_WEEK, 0);
	//
	// if (!values.containsKey(Alarm.Columns.ALARM_TIME))
	// values.put(Alarm.Columns.ALARM_TIME, 0);
	//
	// if (!values.containsKey(Alarm.Columns.ENABLED))
	// values.put(Alarm.Columns.ENABLED, 0);
	//
	// if (!values.containsKey(Alarm.Columns.VIBRATE))
	// values.put(Alarm.Columns.VIBRATE, 1);
	//
	// if (!values.containsKey(Alarm.Columns.MESSAGE))
	// values.put(Alarm.Columns.MESSAGE, "");
	//
	// if (!values.containsKey(Alarm.Columns.ALERT))
	// values.put(Alarm.Columns.ALERT, "");
	//
	SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	long rowId = db.insert(DbColumns.TASK_TABLE, null, initialValues);
	if (rowId < 0) {
	    throw new SQLException("Failed to insert row into " + url);
	}
	if (Logger.LOGV)
	    Log.v(TAG, "Added task rowId = " + rowId);
	Uri newUrl = ContentUris.withAppendedId(DbColumns.CONTENT_URI, rowId);
	getContext().getContentResolver().notifyChange(newUrl, null);
	return newUrl;
    }

    public int delete(Uri url, String where, String[] whereArgs) {
	return 0;
	// SQLiteDatabase db = mOpenHelper.getWritableDatabase();
	// int count;
	// long rowId = 0;
	// switch (sURLMatcher.match(url)) {
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
}
