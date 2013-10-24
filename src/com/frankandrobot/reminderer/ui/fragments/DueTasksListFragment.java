package com.frankandrobot.reminderer.ui.fragments;

import android.R;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.LoaderBuilder;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.ui.adapters.SimpleTaskCursorAdapter;

import org.joda.time.DateTime;

public class DueTasksListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
    static final String TAG = "R:DueFragment";

    private SimpleCursorAdapter adapter;
    private TaskDatabaseFacade taskDatabaseFacade;
    private long dueTime;

    @Override
    public View onCreateView(LayoutInflater inflater,
                             ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = super.onCreateView(inflater, container, savedInstanceState);

        taskDatabaseFacade = new TaskDatabaseFacade(this.getActivity());

        adapter = new SimpleTaskCursorAdapter(getActivity(),
                                        this,
                                        taskDatabaseFacade);

        setListAdapter(adapter);

        return view;
    }

    /**
     * Called by the parent activity to set the due time
     * @param dueTime
     */
    public void setDueTime(long dueTime)
    {
        this.dueTime = dueTime;
    }

    public void setupLoaderManager()
    {
        //reload database
        LoaderBuilder builder = new LoaderBuilder();
        builder.setLoaderId(TaskDatabaseFacade.CURSOR_LOAD_ALL_DUE_TASKS_ID)
                .setDueTime(dueTime);

        taskDatabaseFacade.load(builder, this, this);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        if (Logger.LOGD) Log.d(TAG, "dueTime: " + new DateTime(dueTime));

        setupLoaderManager();
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        adapter.swapCursor(data);

        data.moveToFirst();

        if (data.getCount() == 0)
        {
            Log.e(TAG, "Somethin' went wrong. "+this.getClass().getSimpleName()
                       +" fired but doesn't show any due tasks at "+new DateTime(dueTime));
        }
        else
        {
            String notText = data.getString(data.getColumnIndex(TaskCol.TASK_DESC.toString()))
                                     + ((data.getCount() > 1) ? " and others " : "");

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
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {
        adapter.swapCursor(null);
    }
}
