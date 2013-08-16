package com.frankandrobot.reminderer.database.databasefacade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.Task.Task_Boolean;
import com.frankandrobot.reminderer.helpers.Logger;

abstract public class CursorLoaders
{
    final static private String TAG = "R:"+CursorLoaders.class.getSimpleName();
    final static private TaskTable table = new TaskTable();

    static class AllOpenTasksLoader extends CursorLoader
    {
        public AllOpenTasksLoader(Context context)
        {
            super(context);
            this.setUri(TaskProvider.LOAD_OPEN_TASKS_URI);
        }
    }

    static class AllDueOpenTasksLoader extends CursorLoader
    {
        public AllDueOpenTasksLoader(Context context, long dueTime)
        {
            super(context);
            this.setUri(TaskProvider.CONTENT_URI);
            this.setProjection(table.getColumns(TaskCol.TASK_ID,
                                                TaskCol.TASK_DESC,
                                                TaskCol.TASK_DUE_DATE));
            this.setSelection(TaskCol.TASK_IS_COMPLETE+"=0 AND "
                              + TaskCol.TASK_DUE_DATE+"=?");
            this.setSelectionArgs(new String[]{String.valueOf(dueTime)});
            this.setSortOrder(TaskCol.TASK_DUE_DATE.colname());
        }
    }

    static public class CompleteTaskLoader extends AsyncTaskLoader<Cursor>
    {
        private String taskId;
        public CompleteTaskLoader(Context context, long taskId)
        {
            super(context);
            this.taskId = String.valueOf(taskId);
        }

        @Override
        public Cursor loadInBackground()
        {
            {
                if (Logger.LOGD) Log.d(TAG,
                                       "Completing task Id: " + taskId);

                ContentResolver resolver = getContext().getContentResolver();
                Cursor cursor = resolver.query(TaskProvider.CONTENT_URI,
                                               table.getAllColumns(TaskCol.class),
                                               TaskCol.TASK_ID+"=?",
                                               new String[]{taskId},
                                               null);
                if (cursor != null)
                {
                    cursor.moveToFirst();
                    if (Logger.LOGD) TaskDatabaseFacade.dumpCursor(cursor);
                    Task task = new Task(cursor);
                    task.set(Task_Boolean.isComplete, true);
                    if (Logger.LOGD) Log.d(TAG, task.toString());

                    resolver.update(TaskProvider.CONTENT_URI,
                                    task.getContentValues(Task_Boolean.isComplete),
                                    "_id=?",
                                    new String[]{taskId});
                }
            }

            return null;
        }
    }
}
