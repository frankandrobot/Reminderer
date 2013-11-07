package com.frankandrobot.reminderer.database.databasefacade;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.CursorNonQueryLoaders.AddTask;
import com.frankandrobot.reminderer.database.databasefacade.CursorNonQueryLoaders.CompleteTaskLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorQueryLoaders.AllDueOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorQueryLoaders.AllFoldersLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorQueryLoaders.AllOpenTasksLoader;
import com.frankandrobot.reminderer.database.databasefacade.CursorQueryLoaders.FolderLoader;
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
    final static public int CURSOR_LOAD_ALL_FOLDERS_ID = 6;
    final static public int CURSOR_LOAD_FOLDER_ID = 7;

    private Context context;

    public TaskDatabaseFacade(Context context)
    {
        this.context = context;
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

        Bundle args = builder.buildBundle();
        Task optionalTask = builder.task;

        if (activityOrFragment instanceof FragmentActivity)
        {
            this.context = ((FragmentActivity) activityOrFragment);
            ((FragmentActivity) activityOrFragment).getSupportLoaderManager()
                    .restartLoader(builder.loaderId,
                                   args,
                                   new LoaderCallback(loaderListener, optionalTask));
        }
        else
        {
            this.context = ((Fragment) activityOrFragment).getActivity();
            ((Fragment) activityOrFragment).getLoaderManager()
                    .restartLoader(builder.loaderId,
                                   args,
                                   new LoaderCallback(loaderListener, optionalTask));
        }

        return this;
    }

    public static class LoaderBuilder
    {
        private int loaderId;
        private long dueTime;
        private long taskId;
        private long repeatId;
        private long folderId;
        private Task task;

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

        public LoaderBuilder setTask(Task task)
        {
            this.task = task;
            return this;
        }

        public Bundle buildBundle()
        {
            Bundle args = new Bundle();
            args.putLong("dueTime", dueTime);
            args.putLong("taskId", taskId);
            args.putLong("repeatId", repeatId);
            args.putLong("folderId", folderId);
            return args;
        }
    }

    public interface TaskLoaderListener<T>
    {
        public void onLoadFinished(Loader<T> loader, T data);
        public void onLoaderReset(Loader<T> loader);
    }

    private class LoaderCallback implements LoaderCallbacks<Cursor>
    {
        private TaskLoaderListener<Cursor> loaderListener;
        private Task task;

        /**
         * The existence of this method is because I don't want to implement Parcelable (for Task)
         *
         * @param loaderListener loader listener
         * @param task (optional) task
         */
        public LoaderCallback(TaskLoaderListener<Cursor> loaderListener, Task task)
        {
            this.loaderListener = loaderListener;
            this.task = task;
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
                case ADD_TASK_LOADER_ID:
                    if (task == null) throw new IllegalArgumentException("Need a task damnit!");
                    return new AddTask(context, task);
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

    /**
     * @deprecated use this framework
     * @return LoadAllTasks
     */
    public LoadAllTasks getLoadAllTasksLoader()
    {
        return new LoadAllTasks(context);
    }

    /**
     * @deprecated use {@link com.frankandrobot.reminderer.database.databasefacade.CursorQueryLoaders}
     */
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
