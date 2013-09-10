package com.frankandrobot.reminderer.ui.fragments;

import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.LoaderBuilder;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.ui.adapters.SimpleTaskCursorAdapter;
import com.frankandrobot.reminderer.ui.adapters.SimpleTaskCursorAdapter.TaskCursorAdapter;

public class IndividualFolderListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
    final static private String TAG = "R:"+IndividualFolderListFragment.class.getSimpleName();

    private SimpleCursorAdapter adapter;
    private TaskDatabaseFacade taskDatabaseFacade;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        taskDatabaseFacade = new TaskDatabaseFacade(this.getActivity());

        adapter = new TaskCursorAdapter(getActivity(),
                                        this,
                                        taskDatabaseFacade);

        setListAdapter(adapter);
        setListShown(false);

        return view;
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

    public void setFolderId(String folderId)
    {
        LoaderBuilder builder = new LoaderBuilder();
        builder.setLoaderId(TaskDatabaseFacade.CURSOR_LOAD_FOLDER_ID)
                .setFolderId(folderId);
        taskDatabaseFacade.load(builder, this, this);
    }
}
