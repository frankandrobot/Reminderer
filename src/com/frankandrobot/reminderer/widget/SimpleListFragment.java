package com.frankandrobot.reminderer.widget;

import android.R.layout;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;

public class SimpleListFragment extends ListFragment implements LoaderCallbacks<String[]>
{
    ArrayAdapter<String> adapter;

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getActivity(),
                                           layout.simple_list_item_1);
        setListAdapter(adapter);

        getLoaderManager().initLoader(TaskDatabaseFacade.LOAD_ALL_TASKS_LOADER_ID,
                                      null,
                                      this).forceLoad();
    }

    @Override
    public Loader<String[]> onCreateLoader(int i, Bundle bundle)
    {
        return new TaskDatabaseFacade(getActivity())
                .getLoadAllTasksLoader(getActivity().getApplicationContext());
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] aTaskDesc)
    {
        for(String taskDesc:aTaskDesc)
            adapter.add(taskDesc);

    }

    @Override
    public void onLoaderReset(Loader<String[]> loader)
    {

    }
}
