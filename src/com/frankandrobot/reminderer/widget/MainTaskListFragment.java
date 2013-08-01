package com.frankandrobot.reminderer.widget;

import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.SimpleCursorAdapter;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;

public class MainTaskListFragment extends ListFragment implements LoaderCallbacks<Cursor>
{
    private SimpleCursorAdapter adapter;

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

        getLoaderManager().initLoader(TaskDatabaseFacade.CURSOR_LOAD_ALL_TASKS_LOADER_ID,
                                      null,
                                      this).forceLoad();
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
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle)
    {
        return new TaskDatabaseFacade(getActivity()).getCursorLoadAllTasksLoader();
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
