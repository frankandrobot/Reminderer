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
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllDueOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllFoldersLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.AllOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.CompleteTaskLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorLoaders.FolderLoader;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.helpers.Logger;

import java.util.HashMap;
import java.util.LinkedList;

import static com.frankandrobot.reminderer.database.TaskProvider.CompareOp;

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
    final static public int CURSOR_LOAD_ALL_FOLDERS_ID = 6;
    final static public int CURSOR_LOAD_FOLDER_ID = 7;

    private Context context;
    /**
     * Cache to store loaders
     */
    private HashMap<Integer,TaskLoaderArgs> hmCache = new HashMap<Integer, TaskLoaderArgs>();

    public TaskDatabaseFacade(Context context)
    {
        this.context = context;
    }

    /**
     * Forces a load of the Loader.
     * You probably want to use this only for updates/inserts/deletions---
     * things that need to be done immediately.
     *
     * @return this
     */
    public TaskDatabaseFacade forceLoad(int loaderId)
    {
        TaskLoaderArgs args = hmCache.get(loaderId);

        if (args == null)
            throw new IllegalStateException("loaderId "+loaderId+" was never initialized");

        if (args.activityOrFragment instanceof FragmentActivity)
        {
            ((FragmentActivity) args.activityOrFragment).getSupportLoaderManager()
                    .restartLoader(args.loaderBuilder.loaderId,
                                   args.bundle,
                                   new LoaderCallback(args.loaderListener))
                    .forceLoad();
        }
        else
        {
            ((Fragment) args.activityOrFragment).getLoaderManager()
                    .restartLoader(args.loaderBuilder.loaderId,
                                   args.bundle,
                                   new LoaderCallback(args.loaderListener))
                    .forceLoad();
        }

        return this;
    }

    public TaskDatabaseFacade load(final int loaderId,
                                   Object activityOrFragment,
                                   TaskLoaderListener<Cursor> loaderListener)
    {
        LoaderBuilder builder = new LoaderBuilder();
        builder.loaderId = loaderId;

        return load(builder, activityOrFragment, loaderListener);
    }

    public TaskDatabaseFacade load(LoaderBuilder builder,
                                   Object activityOrFragment,
                                   TaskLoaderListener<Cursor> loaderListener)
    {
        if (!(activityOrFragment instanceof FragmentActivity)
                    && !(activityOrFragment instanceof Fragment))
            throw new IllegalArgumentException(activityOrFragment.getClass().getSimpleName()
                                                       + " must be a Fragment or FragmentActivity");

        Bundle args = new Bundle();
        args.putLong("dueTime", builder.dueTime);
        args.putLong("taskId", builder.taskId);
        args.putLong("repeatId", builder.repeatId);
        args.putLong("folderId", builder.folderId);

        if (activityOrFragment instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activityOrFragment);
            ((FragmentActivity) activityOrFragment).getSupportLoaderManager()
                    .restartLoader(builder.loaderId,
                                   args,
                                   new LoaderCallback(loaderListener));
        }
        else
        {
            this.context = ((Fragment) activityOrFragment).getActivity();
            ((Fragment) activityOrFragment).getLoaderManager()
                    .restartLoader(builder.loaderId,
                                   args,
                                   new LoaderCallback(loaderListener));
        }

        hmCache.put(builder.loaderId,
                    new TaskLoaderArgs(builder, activityOrFragment, loaderListener, args));

        return this;
    }

    public TaskLoaderArgs getLoaderArgs(int loaderId)
    {
        return hmCache.get(loaderId);
    }

    public static class LoaderBuilder
    {
        private int loaderId;
        private long dueTime;
        private long taskId;
        private long repeatId;
        private long folderId;

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

        public LoaderBuilder setFolderId(long folderId)
        {
            this.folderId = folderId;
            return this;
        }
    }

    public interface TaskLoaderListener<T>
    {
        public void onLoadFinished(Loader<T> loader, T data);
        public void onLoaderReset(Loader<T> loader);
    }

    static public class TaskLoaderArgs
    {
        public LoaderBuilder loaderBuilder;
        public Object activityOrFragment;
        public TaskLoaderListener<Cursor> loaderListener;
        public Bundle bundle;

        public TaskLoaderArgs(LoaderBuilder loaderBuilder,
                              Object activityOrFragment,
                              TaskLoaderListener<Cursor> loaderListener,
                              Bundle bundle)
        {
            this.loaderBuilder = loaderBuilder;
            this.activityOrFragment = activityOrFragment;
            this.loaderListener = loaderListener;
            this.bundle = bundle;
        }
    }

    private class LoaderCallback implements LoaderCallbacks<Cursor>
    {
        private TaskLoaderListener<Cursor> loaderListener;

        public LoaderCallback(TaskLoaderListener<Cursor> loaderListener)
        {
            this.loaderListener = loaderListener;
        }

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
                case CURSOR_LOAD_ALL_FOLDERS_ID:
                    return new AllFoldersLoader(context);
                case CURSOR_LOAD_FOLDER_ID:
                    long folderId = args.getLong("folderId");
                    return new FolderLoader(context, String.valueOf(folderId));
            }
            return null;
        }

        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data)
        {
            loaderListener.onLoadFinished(loader, data);
        }

        @Override
        public void onLoaderReset(Loader<Cursor> loader)
        {
            loaderListener.onLoaderReset(loader);
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
