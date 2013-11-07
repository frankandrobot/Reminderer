package com.frankandrobot.reminderer.database.databasefacade;

import android.content.Context;
import android.support.v4.content.CursorLoader;

import com.frankandrobot.reminderer.database.TaskProvider;
import com.frankandrobot.reminderer.database.TaskProvider.CompareOp;
import com.frankandrobot.reminderer.database.TaskProvider.TaskUnionRepeatQuery;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.FolderCol;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

abstract public class CursorQueryLoaders
{
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
