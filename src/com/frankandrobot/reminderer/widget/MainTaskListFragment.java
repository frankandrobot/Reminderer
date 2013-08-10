package com.frankandrobot.reminderer.widget;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ListView;
import android.widget.TextView;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.CursorDeleteProxy;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.widget.gestures.LeftFlingListener;
import com.frankandrobot.reminderer.widget.gestures.LeftFlingListener.IFlingListener;

import java.util.Calendar;

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
        adapter = new TaskCursorAdapter(getActivity(),
                                        android.R.layout.simple_list_item_2,
                                        null,
                                        TaskCol.getColumns(TaskCol.TASK_DESC, TaskCol.TASK_DUE_DATE),
                                        new int[]{ android.R.id.text1, android.R.id.text2 },
                                        0);
        setListAdapter(adapter);
        setListShown(false);

        taskDatabaseFacade = new TaskDatabaseFacade(this.getActivity());

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

    private class TaskCursorAdapter extends SimpleCursorAdapter implements
                                                                IFlingListener
    {
        private Calendar now = Calendar.getInstance();
        private Calendar dueCal = Calendar.getInstance();

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
        public Cursor swapCursor(Cursor c)
        {
            //update now time every time the cursor gets reloaded
            now.setTimeInMillis(System.currentTimeMillis());
            return super.swapCursor(c);
        }

        /**
         * Gets the due date from the TASK_DUE_DATE column
         * in the cursor
         *
         * NOTE: Can be improved by removing the use of {@link Calendar}
         *
         * @param cursor the cursor
         */
        public String getDueDate(Cursor cursor)
        {
            //this method is slow because it uses a Calendar obj, which is expensive
            long dueDate = cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.toString()));
            dueCal.setTimeInMillis(dueDate);

            // if same day and and same year
            if (dueCal.get(Calendar.DAY_OF_YEAR) == now.get(Calendar.DAY_OF_YEAR)
                    && dueCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
            {
                //ex: 11:20pm
                return String.format("%1$tl:%1$tM%1$tp", dueDate);
            }
            else if (dueCal.get(Calendar.YEAR) == now.get(Calendar.YEAR))
            {
                //ex: Jun 1 11:20pm
                return String.format("%1$tb %1$te, %1$tl:%1$tM%1$tp", dueDate);
            }
            return String.format("%1$tm/%1$te/%1$ty, %1$tl:%1$tM%1$tp", dueDate);
        }
        /**
         * Uses the holder pattern for performance boost.
         *
         * @param position the position
         * @param convertView the convertView
         * @param parent the parent
         * @return View to show
         */
        @Override
        public View getView(final int position, View convertView, ViewGroup parent)
        {
            //being paranoid here. Cursor should probably never be closed
            //even after completing task but just in case
            if (!getCursor().isClosed())
            {
               /* if (!mDataValid) {
                    throw new IllegalStateException("this should only be called when the cursor is valid");
                }*/
                if (!getCursor().moveToPosition(position)) {
                    throw new IllegalStateException("couldn't move cursor to position " + position);
                }
            }

            View rowView = convertView;

            if (rowView == null)
            {
                LayoutInflater inflater = LayoutInflater.from(mContext);
                rowView = inflater.inflate(R.layout.main_screen_row, parent, false);
                MainTaskViewHolder viewHolder = new MainTaskViewHolder();
                viewHolder.taskDesc = (TextView)rowView.findViewById(id.task_desc_textview);
                viewHolder.taskDueDate = (TextView)rowView.findViewById((id.task_due_date_textview));
                viewHolder.touchListener = new LeftFlingListener(MainTaskListFragment.this,
                                                                 this);
                rowView.setTag(viewHolder);
                rowView.setOnTouchListener(viewHolder.touchListener);
            }

            if (!getCursor().isClosed())
            {
                MainTaskViewHolder viewHolder = (MainTaskViewHolder) rowView.getTag();
                viewHolder.taskDesc.setText(getCursor().getString(getCursor().getColumnIndex(TaskCol.TASK_DESC.toString())));
                viewHolder.taskDueDate.setText(getDueDate(getCursor()));
                viewHolder.touchListener.setCursorPosition(position);
            }

            return rowView;
        }
        @Override
        public void onFling(final int positionToRemove, final View view)
        {
            final ListView listView = MainTaskListFragment.this.getListView();
            final ViewTreeObserver observer = listView.getViewTreeObserver();
            observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
            {
                public boolean onPreDraw()
                {
                    observer.removeOnPreDrawListener(this);
                    getCursor().moveToPosition(positionToRemove);

                    if (Logger.LOGD) Log.d(TAG, "onFling removing "+positionToRemove+" "+getCursor().getInt(getCursor().getColumnIndex(TaskCol.TASK_ID.toString())));

                    //save the task to delete before "deleting" from cursor
                    taskDatabaseFacade.setTaskToComplete(getCursor().getInt(getCursor().getColumnIndex(TaskCol.TASK_ID.toString())));
                    //"delete" row from cursor
                    CursorDeleteProxy newCursor = new CursorDeleteProxy(getCursor(),
                                                                        positionToRemove);
                    swapCursor(newCursor);

                    //Complete the task
                    taskDatabaseFacade.forceLoad(TaskDatabaseFacade.CURSOR_COMPLETE_TASK_ID,
                                                 MainTaskListFragment.this,
                                                 new TaskLoadListenerAdapter()
                                                 {
                                                     @Override
                                                     public void onLoadFinished(Loader<Cursor> loader,
                                                                                Cursor data)
                                                     {
                                                         adapter.swapCursor(data);
                                                     }
                                                 });


                    return true;
                }
            });
        }
    }

    private class TaskLoadListenerAdapter implements TaskLoaderListener<Cursor>
    {
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {}

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    }

}
