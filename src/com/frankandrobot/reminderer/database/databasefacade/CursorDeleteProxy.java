package com.frankandrobot.reminderer.database.databasefacade;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.database.AbstractCursor;
import android.database.CharArrayBuffer;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;

/**
 * Proxy for the {@link Cursor} class
 * that ignores the row passed in the constructor.
 *
 * Use this to "delete" rows in the listview before deleting them for real
 * in the database.
 *
 */
public class CursorDeleteProxy extends AbstractCursor
{
    private Cursor cursor;
    private int posToIgnore;

    public CursorDeleteProxy(Cursor cursor, int posToRemove)
    {
        this.cursor = cursor;
        this.posToIgnore = posToRemove;
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition)
    {
        if (newPosition < posToIgnore)
        {
            cursor.moveToPosition(newPosition);
        }
        else
        {
            cursor.moveToPosition(newPosition+1);
        }
        return true;
    }

    @Override
    public int getCount()
    {
        return cursor.getCount() - 1;
    }

    @Override
    public String[] getColumnNames()
    {
        return cursor.getColumnNames();
    }

    @Override
    public String getString(int i)
    {
        return cursor.getString(i);
    }

    @Override
    public short getShort(int i)
    {
        return cursor.getShort(i);
    }

    @Override
    public int getInt(int i)
    {
        return cursor.getInt(i);
    }

    @Override
    public long getLong(int i)
    {
        return cursor.getLong(i);
    }

    @Override
    public float getFloat(int i)
    {
        return cursor.getFloat(i);
    }

    @Override
    public double getDouble(int i)
    {
        return cursor.getDouble(i);
    }

    @Override
    public boolean isNull(int i)
    {
        return cursor.isNull(i);
    }

    @TargetApi(VERSION_CODES.HONEYCOMB)
    @Override
    public int getType(int column)
    {
        return cursor.getType(column);
    }

    @Override
    public byte[] getBlob(int column)
    {
        return cursor.getBlob(column);
    }

    @Override
    public int getColumnCount()
    {
        return cursor.getColumnCount();
    }

    @Override
    public boolean requery()
    {
        return super.requery();
    }

    @Override
    public boolean isClosed()
    {
        return cursor.isClosed();
    }

    @Override
    public void close()
    {
        cursor.close();
    }

    @Override
    public void copyStringToBuffer(int columnIndex, CharArrayBuffer buffer)
    {
        cursor.copyStringToBuffer(columnIndex, buffer);
    }

    @Override
    public int getColumnIndex(String columnName)
    {
        return cursor.getColumnIndex(columnName);
    }

    @Override
    public int getColumnIndexOrThrow(String columnName)
    {
        return cursor.getColumnIndexOrThrow(columnName);
    }

    @Override
    public String getColumnName(int columnIndex)
    {
        return cursor.getColumnName(columnIndex);
    }

    @Override
    public void registerContentObserver(ContentObserver observer)
    {
        cursor.registerContentObserver(observer);
    }

    @Override
    public void unregisterContentObserver(ContentObserver observer)
    {
        cursor.unregisterContentObserver(observer);
    }

    @Override
    public void registerDataSetObserver(DataSetObserver observer)
    {
        cursor.registerDataSetObserver(observer);
    }

    @Override
    public void unregisterDataSetObserver(DataSetObserver observer)
    {
        cursor.unregisterDataSetObserver(observer);
    }

    @Override
    public void setNotificationUri(ContentResolver cr, Uri notifyUri)
    {
        cursor.setNotificationUri(cr, notifyUri);
    }

    @Override
    public boolean getWantsAllOnMoveCalls()
    {
        return cursor.getWantsAllOnMoveCalls();
    }

    @Override
    public Bundle getExtras()
    {
        return cursor.getExtras();
    }

    @Override
    public Bundle respond(Bundle extras)
    {
        return cursor.respond(extras);
    }

}
