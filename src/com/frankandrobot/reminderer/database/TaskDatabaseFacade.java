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
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
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
    final static public int CURSOR_LOAD_ALL_OPEN_TASKS_ID = 3;
    final static public int CURSOR_COMPLETE_TASK_ID = 4;

    private Context context;
    private TaskLoaderListener<Cursor> loaderListener;

    private String taskToCompleteId;

    public interface TaskLoaderListener<T>
    {
        public void onLoadFinished(Loader<T> loader, T data);
        public void onLoaderReset(Loader<T> loader);
    }

    public TaskDatabaseFacade(Context context)
    {
        this.context = context;
    }

    public TaskDatabaseFacade forceLoad(final int loaderId,
                                        Object activity,
                                        TaskLoaderListener<Cursor> loaderListener)
    {
        if (!(activity instanceof FragmentActivity)
                && !(activity instanceof Fragment))
            throw new IllegalArgumentException(activity.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        this.loaderListener = loaderListener;

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

        return this;
    }

    public TaskDatabaseFacade load(final int loaderId,
                                   Object activity,
                                   TaskLoaderListener<Cursor> loaderListener)
    {
        if (!(activity instanceof FragmentActivity)
                    && !(activity instanceof Fragment))
            throw new IllegalArgumentException(activity.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        this.loaderListener = loaderListener;

        if (activity instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activity);
            ((FragmentActivity) activity).getSupportLoaderManager()
                    .initLoader(loaderId, null, new LoaderCallback());
        }
        else
        {
            this.context = ((Fragment) activity).getActivity();
            ((Fragment) activity).getLoaderManager()
                    .initLoader(loaderId, null, new LoaderCallback());
        }

        return this;
    }

    private class LoaderCallback implements LoaderCallbacks<Cursor>
    {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle bundle)
        {
            switch(loaderId)
            {
                case CURSOR_LOAD_ALL_OPEN_TASKS_ID:
                    return new CursorLoadAllOpenTasks(context);
                case CURSOR_COMPLETE_TASK_ID :
                    return new CursorCompleteTask(context,
                                                  TaskDatabaseFacade.this);
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
        {
            loaderListener.onLoadFinished(cursorLoader, cursor);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> cursorLoader)
        {
            loaderListener.onLoaderReset(cursorLoader);
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

    static private class CursorLoadAllOpenTasks extends CursorLoader
    {
        public CursorLoadAllOpenTasks(Context context)
        {
            super(context);
            this.setUri(TaskProvider.CONTENT_URI);
            this.setProjection(TaskCol.getColumns(TaskCol.TASK_ID,
                                                  TaskCol.TASK_DESC,
                                                  TaskCol.TASK_DUE_DATE));
            this.setSelection(TaskCol.TASK_IS_COMPLETE+"=0");
            this.setSelectionArgs(null);
            this.setSortOrder(null);
        }
    }

    public void setTaskToComplete(final int id)
    {
        taskToCompleteId = String.valueOf(id);
    }

    public String getTaskToCompleteId() { return taskToCompleteId; }

    static private class CursorCompleteTask extends AsyncTaskLoader<Cursor>
    {
        private TaskDatabaseFacade facade;

        public CursorCompleteTask(Context context, TaskDatabaseFacade facade)
        {
            super(context);
            this.facade = facade;
        }

        @Override
        public Cursor loadInBackground()
        {
            {
                if (Logger.LOGV)
                {
                    Log.v(TAG, "Completing task Id: "+facade.getTaskToCompleteId());
                }
                ContentResolver resolver = getContext().getContentResolver();
                Cursor cursor = resolver.query(TaskProvider.CONTENT_URI,
                                               TaskCol.getAllColumns(),
                                               TaskCol.TASK_ID+"=?",
                                               new String[]{facade.getTaskToCompleteId()},
                                               null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    if (Logger.LOGD) dumpCursor(cursor);
                    Task task = new Task(cursor);
                    task.set(Task_Boolean.isComplete, true);
                    if (Logger.LOGD) Log.d(TAG, task.toString());

                    resolver.update(TaskProvider.CONTENT_URI,
                                    task.toContentValues(),
                                    "_id=?",
                                    new String[]{facade.getTaskToCompleteId()});
                }
            }

            return null;
        }
    }

    static private void dumpCursor(Cursor cursor)
    {
        if (cursor != null)
        {
            for(int i=0; i<cursor.getColumnCount(); i++)
            {
                Log.d(TAG, cursor.getColumnName(i)+"="+cursor.getString(i));
            }
        }
    }
}
