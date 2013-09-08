package com.frankandrobot.reminderer.database.databasefacade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.frankandrobot.reminderer.alarm.AlarmManager;
import com.frankandrobot.reminderer.alarm.AlarmManager.CompareOp;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllDueOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllFoldersLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.CompleteTaskLoader;
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
    final static public int CURSOR_LOAD_ALL_OPEN_TASKS_ID = 3;
    final static public int CURSOR_COMPLETE_TASK_ID = 4;
    final static public int CURSOR_LOAD_ALL_DUE_TASKS_ID = 5;
    final static public int CURSOR_LOAD_FOLDERS_ID = 6;

    private Context context;
    private TaskLoaderListener<Cursor> loaderListener;

    public interface TaskLoaderListener<T>
    {
        public void onLoadFinished(Loader<T> loader, T data);
        public void onLoaderReset(Loader<T> loader);
    }

    public static class LoaderBuilder
    {
        private int loaderId;
        private long dueTime;
        private long taskId;
        private long repeatId;

        public LoaderBuilder setLoaderId(int loaderId)
        {
            this.loaderId = loaderId;
            return this;
        }

        public LoaderBuilder setDueTime(long dueTime)
        {
            this.dueTime = dueTime;
            return this;
        }

        public LoaderBuilder setTaskId(long taskId)
        {
            this.taskId = taskId;
            return this;
        }

        public LoaderBuilder setRepeatId(long repeatId)
        {
            this.repeatId = repeatId;
            return this;
        }
    }

    public TaskDatabaseFacade(Context context)
    {
        this.context = context;
    }

    /**
     * @deprecated use {@link #load}
     *
     * @param loaderId
     * @param activity
     * @param loaderListener
     * @return
     */
    public TaskDatabaseFacade forceLoad(final int loaderId,
                                        Object activity,
                                        TaskLoaderListener<Cursor> loaderListener)
    {
        LoaderBuilder builder = new LoaderBuilder();
        builder.loaderId = loaderId;

        return forceLoad(builder, activity, loaderListener);
    }

    /**
     * @deprecated  use {@link #load}
     *
     * @param builder
     * @param activity
     * @param loaderListener
     * @return
     */
    public TaskDatabaseFacade forceLoad(LoaderBuilder builder,
                                        Object activity,
                                        TaskLoaderListener<Cursor> loaderListener)
    {
        if (!(activity instanceof FragmentActivity)
                    && !(activity instanceof Fragment))
            throw new IllegalArgumentException(activity.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        this.loaderListener = loaderListener;

        Bundle args = new Bundle();
        args.putLong("dueTime", builder.dueTime);
        args.putLong("taskId", builder.taskId);
        args.putLong("repeatId", builder.repeatId);

        if (activity instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activity);
            ((FragmentActivity) activity).getSupportLoaderManager()
                    .restartLoader(builder.loaderId, args, new LoaderCallback()).forceLoad();
        }
        else
        {
            this.context = ((Fragment) activity).getActivity();
            ((Fragment) activity).getLoaderManager()
                    .restartLoader(builder.loaderId, args, new LoaderCallback()).forceLoad();
        }

        return this;
    }

    public TaskDatabaseFacade load(final int loaderId,
                                   Object activity,
                                   TaskLoaderListener<Cursor> loaderListener)
    {
        LoaderBuilder builder = new LoaderBuilder();
        builder.loaderId = loaderId;

        return load(builder, activity, loaderListener);
    }

    public TaskDatabaseFacade load(LoaderBuilder builder,
                                   Object activity,
                                   TaskLoaderListener<Cursor> loaderListener)
    {
        if (!(activity instanceof FragmentActivity)
                    && !(activity instanceof Fragment))
            throw new IllegalArgumentException(activity.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        this.loaderListener = loaderListener;

        Bundle args = new Bundle();
        args.putLong("dueTime", builder.dueTime);
        args.putLong("taskId", builder.taskId);
        args.putLong("repeatId", builder.repeatId);

        if (activity instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activity);
            ((FragmentActivity) activity).getSupportLoaderManager()
                    .restartLoader(builder.loaderId, args, new LoaderCallback());
        }
        else
        {
            this.context = ((Fragment) activity).getActivity();
            ((Fragment) activity).getLoaderManager()
                    .restartLoader(builder.loaderId, args, new LoaderCallback());
        }

        return this;
    }

    private class LoaderCallback implements LoaderCallbacks<Cursor>
    {

        @Override
        public Loader<Cursor> onCreateLoader(int loaderId, Bundle args)
        {
            switch(loaderId)
            {
                case CURSOR_LOAD_ALL_OPEN_TASKS_ID:
                    return new AllOpenTasksLoader(context);
                case CURSOR_COMPLETE_TASK_ID :
                    long taskId = args.getLong("taskId");
                    long repeatId = args.getLong("repeatId");
                    return new CompleteTaskLoader(context, taskId, repeatId);
                case CURSOR_LOAD_ALL_DUE_TASKS_ID:
                    long dueTime = args.getLong("dueTime", 0);
                    return new AllDueOpenTasksLoader(context, dueTime);
                case CURSOR_LOAD_FOLDERS_ID:
                    return new AllFoldersLoader(context);
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

    public static class AddTask extends AsyncTaskLoader<Void>
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
                long now = System.currentTimeMillis();

                task.calculateNextDueDate();

                resolver.insert(TaskProvider.TASKS_URI,
                                task.getContentValuesForInsert());
                alarmHelper.findAndEnableNextTasksDue(getContext(),
                                                      now,
                                                      CompareOp.ON_OR_AFTER);
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

            Cursor cursor = getContext().getContentResolver().query(TaskProvider.TASKS_URI,
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

    static protected void dumpCursor(Cursor cursor)
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
