package com.frankandrobot.reminderer.database.databasefacade;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.CursorLoader;
import android.util.Log;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskProvider.CompareOp;
import com.frankandrobot.reminderer.database.TaskProvider.TaskUnionRepeatQuery;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.FolderCol;
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

    public static class AllDueOpenTasksLoader extends CursorLoader
    {
        public AllDueOpenTasksLoader(Context context, long dueTime)
        {
            super(context);
            this.setUri(TaskProvider.TASK_UNION_REPEAT_URI);
            this.setProjection(new TaskTable().getColumns(TaskCol.TASK_ID,
                                                          TaskCol.TASK_DESC,
                                                          TaskCol.TASK_REPEAT_TYPE,
                                                          TaskCol.TASK_DUE_DATE,
                                                          //join
                                                          TaskCol.TASK_ID,
                                                          TaskCol.TASK_DESC,
                                                          TaskCol.TASK_REPEAT_TYPE,
                                                          RepeatsCol.REPEAT_NEXT_DUE_DATE));
            StringBuilder sel = new StringBuilder(100);
            sel.append(TaskCol.TASK_IS_COMPLETE + "=0");
            sel.append(" AND ");
            sel.append(TaskCol.TASK_DUE_DATE.colname() + CompareOp.ON + "?");
            sel.append(TaskUnionRepeatQuery.SEPARATOR);
            sel.append(TaskCol.TASK_IS_COMPLETE + "=0");
            sel.append(" AND ");
            sel.append(RepeatsCol.REPEAT_NEXT_DUE_DATE.colname() + CompareOp.ON + "?");
            this.setSelection(sel.toString());
            this.setSelectionArgs(new String[]{String.valueOf(dueTime)});
            this.setSortOrder(TaskCol.TASK_ID.colname());
        }
    }

    static public class CompleteTaskLoader extends AsyncTaskLoader<Cursor>
    {
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
                if (Logger.LOGD) Log.d(TAG,
                                       "Completing task Id: " + taskId);

                ContentResolver resolver = getContext().getContentResolver();

                if (repeatId == null)
                {
                    Cursor cursor = resolver.query(TaskProvider.TASKS_URI,
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
    }

    static class AllFoldersLoader extends CursorLoader
    {
        public AllFoldersLoader(Context context)
        {
            super(context);
            this.setUri(TaskProvider.FOLDERS_URI);
            this.setProjection(table.getColumns(FolderCol.FOLDER_ID,
                                                FolderCol.FOLDER_NAME));
            this.setSortOrder(FolderCol.FOLDER_ID.colname());
        }
    }

    static class FolderLoader extends CursorLoader
    {
        public FolderLoader(Context context, String folderId)
        {
            super(context);
            this.setUri(TaskProvider.LOAD_OPEN_TASKS_URI);
            this.setSelection(TaskCol.TASK_FOLDER_ID_PK+"=?");
            this.setSelectionArgs(new String[]{folderId});
        }
    }
}
