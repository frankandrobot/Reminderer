package com.frankandrobot.reminderer.widget;

import android.R.layout;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;

public class DueListFragment extends ListFragment implements LoaderCallbacks<String[]>
{
    private ArrayAdapter<String> adapter;
    private long dueTime;

    static public interface DueListFragmentListener
    {
        public void setDueTime(final long dueTime);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        adapter = new ArrayAdapter<String>(getActivity(),
                                           layout.simple_list_item_1);
        setListAdapter(adapter);
    }

    /**
     * Called when the activity gets attached
     *
     * @param activity
     */
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof DueListFragmentListener)
        {
//            listener = (DueListFragmentListener) activity;
        }
        else
        {
            //throw new ClassCastException(activity.toString() + " must implement "+DueListFragmentListener.class.getSimpleName());
        }
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
  //      listener = null;
    }

    public void setDueTime(long dueTime)
    {
        this.dueTime = dueTime;

        //reload database
        getLoaderManager().initLoader(TaskDatabaseFacade.LOAD_TASKS_LOADER_ID,
                                      null,
                                      this).forceLoad();
    }

    @Override
    public Loader<String[]> onCreateLoader(int i, Bundle bundle)
    {
        return new TaskDatabaseFacade()
                .getLoadTasksLoader(getActivity().getApplicationContext(),
                                    dueTime);
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] strings)
    {
        for(String task:strings)
            adapter.add(task);
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader)
    {

    }
}
