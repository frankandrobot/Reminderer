package com.frankandrobot.reminderer.widget.main;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Typeface;
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
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.CursorDeleteProxy;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.LoaderBuilder;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade.TaskLoaderListener;
import com.frankandrobot.reminderer.helpers.Logger;
import com.frankandrobot.reminderer.parser.GrammarRule;
import com.frankandrobot.reminderer.widget.gestures.LeftFlingListener;
import com.frankandrobot.reminderer.widget.gestures.LeftFlingListener.IFlingListener;
import com.frankandrobot.reminderer.widget.main.MainTaskListFragment.MainTaskViewHolder;

import java.util.Calendar;

import static com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken;

/**
 * Adapter that converts cursors to these rows:
 *
 * ++++++++
 * TaskDesc
 * dueDate
 * ++++++++
 *
 * Also adds a {@link LeftFlingListener} to complete tasks.
 *
 */
public class TaskCursorAdapter extends SimpleCursorAdapter
        implements IFlingListener
{
    final static protected String TAG = "R:"+TaskCursorAdapter.class.getSimpleName();

    protected Calendar now = Calendar.getInstance();
    protected Calendar dueCal = Calendar.getInstance();
    protected ListFragment listFragment;
    protected TaskDatabaseFacade taskDatabaseFacade;

    public TaskCursorAdapter(Context context,
                             ListFragment listFragment,
                             TaskDatabaseFacade taskDatabaseFacade)
    {
        super(context,
              android.R.layout.simple_list_item_1,
              null,
              new TaskTable().getColumns(TaskCol.TASK_ID,
                                         TaskCol.TASK_DESC,
                                         TaskCol.TASK_DUE_DATE),
              null, 0);

        this.listFragment = listFragment;
        this.taskDatabaseFacade = taskDatabaseFacade;
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
     * NOTE: Can be improved by removing the use of {@link java.util.Calendar}
     *
     * @param cursor the cursor
     */
    public String getDueDate(Cursor cursor)
    {
        //this method is slow because it uses a Calendar obj, which is expensive
        long dueDate = cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.colname()));
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
            viewHolder.touchListener = new LeftFlingListener(listFragment,
                                                             this);
            rowView.setTag(viewHolder);
            rowView.setOnTouchListener(viewHolder.touchListener);
        }

        if (!getCursor().isClosed())
        {
            MainTaskViewHolder viewHolder = (MainTaskViewHolder) rowView.getTag();
            viewHolder.taskDesc.setText(getCursor().getString(getCursor().getColumnIndex(TaskCol.TASK_DESC.colname())));
            String dueDate = getDueDate(getCursor());
            int repeatType = getCursor().getInt(getCursor().getColumnIndex(TaskCol.TASK_REPEAT_TYPE.colname()));
            String repeatVal = "";
            if (repeatType > 0) repeatVal=", Repeats: "+ RepeatsToken.Type.toType(repeatType).getDescription();
            viewHolder.taskDueDate.setText(dueDate+repeatVal);
            viewHolder.touchListener.setCursorPosition(position);
        }

        return rowView;
    }

    @Override
    public void onFling(final int positionToRemove, final View view)
    {
        final ListView listView = listFragment.getListView();
        final ViewTreeObserver observer = listView.getViewTreeObserver();
        observer.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener()
        {
            public boolean onPreDraw()
            {
                observer.removeOnPreDrawListener(this);
                //save the task to delete before "deleting" from cursor
                getCursor().moveToPosition(positionToRemove);
                long taskToCompleteId = getCursor().getLong(getCursor().getColumnIndex(TaskCol.TASK_ID.colname()));
                long repeatIdToComplete = !getCursor().isNull(getCursor().getColumnIndex(RepeatsCol.REPEAT_ID.colname()))
                                                  ? getCursor().getLong(getCursor().getColumnIndex(RepeatsCol.REPEAT_ID.colname()))
                                                  : -1;

                if (Logger.LOGD) Log.d(TAG,
                                       "onFling removing " + positionToRemove + " " + taskToCompleteId);


                //"delete" row from cursor
                CursorDeleteProxy newCursor = new CursorDeleteProxy(getCursor(),
                                                                    positionToRemove);
                swapCursor(newCursor);

                //Complete the task
                LoaderBuilder builder = new LoaderBuilder();
                builder.setLoaderId(TaskDatabaseFacade.CURSOR_COMPLETE_TASK_ID)
                        .setTaskId(taskToCompleteId)
                        .setRepeatId(repeatIdToComplete);

                taskDatabaseFacade.forceLoad(builder,
                                             listFragment,

                                             new TaskLoadListenerAdapter()
                                             {
                                                 @Override
                                                 public void onLoadFinished(Loader<Cursor> loader,
                                                                            Cursor data)
                                                 {
                                                     //((SimpleCursorAdapter)listFragment.getListAdapter()).swapCursor(data);
                                                 }
                                             });


                return true;
            }
        });
    }

    private static class TaskLoadListenerAdapter implements TaskLoaderListener<Cursor>
    {
        @Override
        public void onLoadFinished(Loader<Cursor> loader, Cursor data) {}

        @Override
        public void onLoaderReset(Loader<Cursor> loader) {}
    }

    static public class MainTaskCursorAdapter extends TaskCursorAdapter
    {

        public MainTaskCursorAdapter(Context context,
                                     ListFragment listFragment,
                                     TaskDatabaseFacade taskDatabaseFacade)
        {
            super(context, listFragment, taskDatabaseFacade);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent)
        {
            View row = super.getView(position, convertView, parent);

            long dueDate = getCursor().getLong(getCursor().getColumnIndex(TaskCol.TASK_DUE_DATE.colname()));
            if (dueDate < now.getTimeInMillis())
            {
                MainTaskViewHolder holder = (MainTaskViewHolder) row.getTag();
                holder.taskDesc.setTypeface(holder.taskDesc.getTypeface(),
                                            Typeface.ITALIC);
                holder.taskDueDate.setTypeface(holder.taskDueDate.getTypeface(),
                                               Typeface.ITALIC);
            }
            return row;
        }
    }

}
