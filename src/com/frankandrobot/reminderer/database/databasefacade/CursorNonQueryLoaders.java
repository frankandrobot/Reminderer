package com.frankandrobot.reminderer.database.databasefacade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;

import com.frankandrobot.reminderer.alarm.AlarmManager;
import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskProvider.CompareOp;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import com.frankandrobot.reminderer.helpers.Logger;

public class CursorNonQueryLoaders
{
    final static private TaskTable table = new TaskTable();

    static class CompleteTaskLoader extends AsyncTaskLoader<Cursor>
    {
        final static private String TAG = CompleteTaskLoader.class.getSimpleName();

        private String taskId;
        private String repeatId;

        public CompleteTaskLoader(Context context, long taskId, long repeatId)
        {
            super(context);
            this.taskId = String.valueOf(taskId);
            if (repeatId > 0) this.repeatId = String.valueOf(repeatId);
        }

        @Override
        public Cursor loadInBackground()
        {
            {
                if (Logger.LOGD) Log.d(TAG, "Completing task Id: " + taskId);

                ContentResolver resolver = getContext().getContentResolver();

                if (repeatId == null)
                {
                    Cursor cursor = resolver.query(TaskProvider.TASKS_URI,
                                                   table.getAllColumns(TaskCol.class),
                                                   TaskCol.TASK_ID + "=?",
                                                   new String[]{taskId},
                                                   null);
                    if (cursor != null)
                    {
                        cursor.moveToFirst();

                        if (Logger.LOGD) TaskDatabaseFacade.dumpCursor(cursor);

                        Task task = new Task(cursor);
                        task.set(Task_Boolean.isComplete, true);

                        if (Logger.LOGD) Log.d(TAG, task.toString());

                        resolver.update(TaskProvider.TASKS_URI,
                                        task.getContentValues(Task_Boolean.isComplete),
                                        "_id=?",
                                        new String[]{taskId});
                    }
                }
                else
                {
                    Uri deleteUri = Uri.withAppendedPath(TaskProvider.REPEAT_URI, repeatId);
                    int rowsDeleted = resolver.delete(deleteUri, null, null);
                    if (rowsDeleted == 0) Log.d(TAG, "Unable to delete task for some reason");
                }
            }

            return null;
        }

        /**
         * This makes the loader complete the task as soon as it gets created
         */
        @Override
        protected void onStartLoading()
        {
            forceLoad();
        }
    }

    static class AddTask extends AsyncTaskLoader<Cursor>
    {
        final static private String TAG = AddTask.class.getSimpleName();

        private Task task;
        private AlarmManager alarmHelper = new AlarmManager();

        public AddTask(Context context, Task task) {
            super(context);

            this.task = task;

        }

        @Override
        public Cursor loadInBackground()
        {
            if (Logger.LOGV) Log.v(TAG, "Saving task:\n" + task);

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

        /**
         * This makes the loader start as soon as it gets created
         */
        @Override
        protected void onStartLoading()
        {
            forceLoad();
        }
    }
}
