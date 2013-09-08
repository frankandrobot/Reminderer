package com.frankandrobot.reminderer.alarm;

import android.R;
import android.app.NotificationManager;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.LoaderBuilder;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.ui.adapters.TaskCursorAdapter;

public class AlarmDueListFragment extends ListFragment implements
                                                       TaskLoaderListener<Cursor>
{
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

        return view;
    }

/*
    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

    }
*/

    public void setDueTime(long dueTime)
    {
        //reload database
        LoaderBuilder builder = new LoaderBuilder();
        builder.setLoaderId(TaskDatabaseFacade.CURSOR_LOAD_ALL_DUE_TASKS_ID)
                .setDueTime(dueTime);
        taskDatabaseFacade.load(builder, this, this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data)
    {
        adapter.swapCursor(data);

        data.moveToFirst();

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

    @Override
    public void onLoaderReset(Loader<Cursor> loader)
    {

    }
}
