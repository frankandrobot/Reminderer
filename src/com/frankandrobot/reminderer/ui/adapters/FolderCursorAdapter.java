package com.frankandrobot.reminderer.ui.adapters;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.support.v4.app.ListFragment;
import android.support.v4.widget.SimpleCursorAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.TextView;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.R.id;
import com.frankandrobot.reminderer.database.TaskTable;
import com.frankandrobot.reminderer.database.TaskTable.FolderCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.database.databasefacade.TaskDatabaseFacade;
import com.frankandrobot.reminderer.ui.activities.IndividualFolderActivity;

/**
 * Adapter that converts cursors to these rows:
 *
 * ++++++++
 * Folder
 * ++++++++
 *
 */
public class FolderCursorAdapter extends SimpleCursorAdapter
{
    final static protected String TAG = "R:"+FolderCursorAdapter.class.getSimpleName();

    protected ListFragment listFragment;
    protected TaskDatabaseFacade taskDatabaseFacade;
    protected OnClickListener onClickListener;

    public FolderCursorAdapter(final Context context,
                               ListFragment listFragment,
                               TaskDatabaseFacade taskDatabaseFacade)
    {
        super(context,
              android.R.layout.simple_list_item_1,
              null,
              new TaskTable().getColumns(FolderCol.FOLDER_ID,
                                         FolderCol.FOLDER_NAME),
              null, 0);

        this.listFragment = listFragment;
        this.taskDatabaseFacade = taskDatabaseFacade;

        onClickListener = new OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(context, IndividualFolderActivity.class);
                context.startActivity(intent);
            }
        };
    }


    @Override
    public Cursor swapCursor(Cursor c)
    {
        return super.swapCursor(c);
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
            if (!getCursor().moveToPosition(position)) {
                throw new IllegalStateException("couldn't move cursor to position " + position);
            }
        }

        View rowView = convertView;

        if (rowView == null)
        {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            rowView = inflater.inflate(R.layout.all_folders_row, parent, false);
            ViewHolder viewHolder = new ViewHolder();
            viewHolder.folderName = (TextView)rowView.findViewById(id.folder_name);
            rowView.setTag(viewHolder);
        }

        if (!getCursor().isClosed())
        {
            ViewHolder viewHolder = (ViewHolder) rowView.getTag();
            viewHolder.folderName.setText(getCursor().getString(getCursor().getColumnIndex(FolderCol.FOLDER_NAME.colname())));
            viewHolder.folderId = getCursor()
                                  .getLong(getCursor().getColumnIndex(TaskCol.TASK_FOLDER_ID_PK.colname()));
            rowView.setOnClickListener(onClickListener);
        }

        return rowView;
    }

    static class ViewHolder
    {
        TextView folderName;
        long folderId;
    }
}
