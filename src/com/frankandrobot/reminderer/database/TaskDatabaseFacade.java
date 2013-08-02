package com.frankandrobot.reminderer.database;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.frankandrobot.reminderer.alarm.AlarmManager;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.LinkedList;

/**
 * The app-specific interface to the database.
 *
 * Abstracts away the init loader functionality.
 * Implement the TaskLoaderListener in your FragmentActivity.
 *
 */
public class TaskDatabaseFacade
{
    final static private String TAG = "R:TaskFacade";

    final static public int ADD_TASK_LOADER_ID = 0;
    final static public int LOAD_ALL_TASKS_LOADER_ID = 1;
    final static public int LOAD_TASKS_LOADER_ID = 2;
    final static public int CURSOR_LOAD_ALL_TASKS_LOADER_ID = 3;

    private Context context;
    private TaskLoaderListener<Cursor> activity;

    public interface TaskLoaderListener<T>
    {
        public void onLoadFinished(Loader<T> loader, T data);
        public void onLoaderReset(Loader<T> loader);
    }

    public TaskDatabaseFacade(Context context)
    {
        this.context = context;
    }

    public TaskDatabaseFacade(TaskLoaderListener<Cursor> activity,
                              final int loaderId)
    {
        if (!(activity instanceof FragmentActivity)
                && !(activity instanceof Fragment))
            throw new IllegalArgumentException(activity.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        this.activity = activity;

        if (activity instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activity);
            ((FragmentActivity) activity).getSupportLoaderManager()
                    .initLoader(loaderId, null, new LoaderCallback())
                    .forceLoad();
        }
        else
        {
            this.context = ((Fragment) activity).getActivity();
            ((Fragment) activity).getLoaderManager()
                    .initLoader(loaderId, null, new LoaderCallback())
                    .forceLoad();
        }
    }

    private class LoaderCallback implements LoaderCallbacks<Cursor>
    {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle)
        {
            switch(loaderId)
            {
                case CURSOR_LOAD_ALL_TASKS_LOADER_ID :
                    return new CursorLoadAllTasks(context);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
        {
            activity.onLoadFinished(cursorLoader, cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader)
        {
            activity.onLoaderReset(cursorLoader);
        }
    }

    public AddTask getAddTaskLoader(Task task)
    {
        return new AddTask(context, task);
    }

    public LoadAllTasks getLoadAllTasksLoader()
    {
        return new LoadAllTasks(context);
    }

    public LoadTasks getLoadTasksLoader(long dueTime)
    {
        return new LoadTasks(context, dueTime);
    }

    static protected class AddTask extends AsyncTaskLoader<Void>
    {
        private Task task;
        private AlarmManager alarmHelper = new AlarmManager();

        public AddTask(Context context, Task task) {
            super(context);

            this.task = task;

        }

        @Override
        public Void loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Saving task:\n" + task);
            }

            if (task != null)
            {
                ContentResolver resolver = getContext().getContentResolver();
                resolver.insert(TaskProvider.CONTENT_URI, task.toContentValues());
                alarmHelper.findAndEnableNextTasksDue(getContext(), System.currentTimeMillis());
            }
            return null;
        }
    }

    static private class LoadAllTasks extends AsyncTaskLoader<String[]>
    {
        public LoadAllTasks(Context context)
        {
            super(context);
        }

        @Override
        public String[] loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Loading tasks");
            }

            Cursor cursor = getContext().getContentResolver().query(TaskProvider.CONTENT_URI,
                                                                    new String[]{TaskCol.TASK_DESC.toString()},
                                                                    null,
                                                                    null,
                                                                    null);

            if (cursor != null)
            {
                LinkedList<String> llTasks = new LinkedList<String>();

                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    llTasks.add(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
                    cursor.moveToNext();
                }
                return llTasks.toArray(new String[llTasks.size()]);
            }

            return new String[0];
        }
    }

    static private class LoadTasks extends AsyncTaskLoader<String[]>
    {
        private long dueTime;

        public LoadTasks(Context context, long dueTime)
        {
            super(context);
            this.dueTime = dueTime;
        }

        @Override
        public String[] loadInBackground()
        {
            if (Logger.LOGV)
            {
                Log.v(TAG, "Loading tasks");
            }

            Cursor cursor = getContext().getContentResolver().query(TaskProvider.CONTENT_URI,
                                                                    new String[]{TaskCol.TASK_DESC.toString()},
                                                                    TaskCol.TASK_DUE_DATE+"=?",
                                                                    new String[]{String.valueOf(dueTime)},
                                                                    null);

            if (cursor != null)
            {
                LinkedList<String> llTasks = new LinkedList<String>();

                cursor.moveToFirst();
                while (!cursor.isAfterLast())
                {
                    llTasks.add(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
                    cursor.moveToNext();
                }
                return llTasks.toArray(new String[llTasks.size()]);
            }

            return new String[0];
        }
    }

    static private class CursorLoadAllTasks extends CursorLoader
    {
        public CursorLoadAllTasks(Context context)
        {
            super(context);
            this.setUri(TaskProvider.CONTENT_URI);
            this.setProjection(TaskCol.getColumns(TaskCol.TASK_ID,
                                                  TaskCol.TASK_DESC,
                                                  TaskCol.TASK_DUE_DATE));
            this.setSelection(null);
            this.setSelectionArgs(null);
            this.setSortOrder(null);
        }
    }
}
