package com.frankandrobot.reminderer.widget;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

public class MainTaskListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
    private SimpleCursorAdapter adapter;
    private TaskDatabaseFacade taskDatabaseFacade;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        adapter = new SimpleCursorAdapter(getActivity(),
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

    /**
     * Called when the activity gets attached
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
/*
        if (activity instanceof DueListFragmentListener)
        {
//            listener = (DueListFragmentListener) activity;
        }
        else
        {
            //throw new ClassCastException(activity.toString() + " must implement "+DueListFragmentListener.class.getSimpleName());
        }
*/
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
  //      listener = null;
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

}
