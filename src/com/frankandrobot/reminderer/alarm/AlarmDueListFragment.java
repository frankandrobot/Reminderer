package com.frankandrobot.reminderer.alarm;

import android.R;
import android.R.layout;
import android.app.Activity;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.widget.ArrayAdapter;

import com.frankandrobot.reminderer.database.TaskDatabaseFacade;

public class AlarmDueListFragment extends ListFragment implements LoaderCallbacks<String[]>
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
        return new TaskDatabaseFacade(getActivity())
                .getLoadTasksLoader(getActivity().getApplicationContext(),
                                    dueTime);
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] aTaskDesc)
    {
        for(String taskDesc:aTaskDesc)
            adapter.add(taskDesc);

        String notText = aTaskDesc[0]
                                 + ((aTaskDesc.length > 1) ? " and others " : "");
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(getActivity())
                        .setSmallIcon(R.drawable.sym_def_app_icon)
                        .setContentTitle("Task(s) are due")
                        .setContentText(notText);

        NotificationManager mNotificationManager = (NotificationManager)
                              getActivity().getApplicationContext()
                                      .getSystemService(Context.NOTIFICATION_SERVICE);
        mNotificationManager.notify(1, mBuilder.getNotification());
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader)
    {

    }
}
