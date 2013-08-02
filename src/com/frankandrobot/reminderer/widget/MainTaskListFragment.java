package com.frankandrobot.reminderer.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MainTaskListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
    private SimpleCursorAdapter adapter;
    private TaskDatabaseFacade taskDatabaseFacade;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        adapter = new TaskCursorAdapter(getActivity(),
                                        android.R.layout.simple_list_item_2,
                                        null,
                                        TaskCol.getColumns(TaskCol.TASK_DESC, TaskCol.TASK_DUE_DATE),
                                        new int[]{ android.R.id.text1, android.R.id.text2 },
                                        0);
        setListAdapter(adapter);
        setListShown(false);

        taskDatabaseFacade = new TaskDatabaseFacade(this,
                                                    TaskDatabaseFacade.CURSOR_LOAD_ALL_TASKS_LOADER_ID);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        adapter.swapCursor(cursor);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        adapter.swapCursor(null);
    }

    private class TaskCursorAdapter extends SimpleCursorAdapter
    {
        private SimpleDateFormat sdfLong = new SimpleDateFormat("MMM d, hh:mmaa");
        private SimpleDateFormat sdfShort = new SimpleDateFormat("hh:mm aa");
        private SimpleDateFormat sdfDay = new SimpleDateFormat("DD yyyy");

        public TaskCursorAdapter(Context context,
                                 int layout,
                                 Cursor c,
                                 String[] from,
                                 int[] to,
                                 int flags)
        {
            super(context, layout, c, from, to, flags);
        }

        @Override
        public void bindView(View view, Context context, Cursor cursor) {
            TextView taskDesc = (TextView)view.findViewById(id.task_desc_textview);
            taskDesc.setText(cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
            TextView dueDate = (TextView)view.findViewById((id.task_due_date_textview));
            long due = cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.toString()));
            Date time = new Date(due);
            Date now = new Date();
            if (sdfDay.format(time).equals(sdfDay.format(now)))
            {
                dueDate.setText(sdfShort.format(time));
            }
            else
            {
                dueDate.setText(sdfLong.format(time));
            }
        }

        @Override
        public View newView(Context context, Cursor cursor, ViewGroup parent) {
            LayoutInflater inflater = LayoutInflater.from(context);
            View v = inflater.inflate(R.layout.main_screen_row, parent, false);
            bindView(v, context, cursor);
            return v;
        }

    }
}
