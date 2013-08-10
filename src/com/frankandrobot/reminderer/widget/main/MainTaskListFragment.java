package com.frankandrobot.reminderer.widget.main;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.widget.TextView;

import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.widget.gestures.LeftFlingListener;

public class MainTaskListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
    final static private String TAG = "R:MainTaskList";

    private SimpleCursorAdapter adapter;
    private TaskDatabaseFacade taskDatabaseFacade;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        taskDatabaseFacade = new TaskDatabaseFacade(this.getActivity());

        adapter = new TaskCursorAdapter(getActivity(),
                                        this,
                                        taskDatabaseFacade);

        setListAdapter(adapter);
        setListShown(false);



        taskDatabaseFacade.load(TaskDatabaseFacade.CURSOR_LOAD_ALL_OPEN_TASKS_ID,
                                this,
                                this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor)
    {
        if (Logger.LOGD) Log.d(TAG, "onLoadFinished");
        adapter.swapCursor(cursor);
        setListShown(true);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader)
    {
        if (Logger.LOGD) Log.d(TAG, "onLoaderReset");
        adapter.swapCursor(null);
    }

    static public class MainTaskViewHolder
    {
        public TextView taskDesc;
        public TextView taskDueDate;
        public LeftFlingListener touchListener;
    }

}
