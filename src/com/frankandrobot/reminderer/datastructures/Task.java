package com.frankandrobot.reminderer.datastructures;

import android.content.ContentValues;
import android.database.Cursor;

import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken;
import com.frankandrobot.reminderer.datastructures.DataStructure.*;

import java.util.Calendar;

/**
 * Represents a task.
 *
 * The task date is **always** a date in the future. Ex:
 *
 * - given "buy milk monday" and its Tuesday, then task date = Monday.
 * - given "buy milk 8pm" and its 9pm, then task date = tomorrow 8pm
 * - given "buy milk Jun 1" and its June 2nd, then task date = June 1 next year.
 *
 * The only time it will set a date in the past is when you explicitly set it in
 * the past (ex: "buy milk June 1, 1979")
 *
 * If you use the wrong date and day (ex: Monday June 23 and June 23 is a Tuesday),
 * task will use the date.
 *
 */
public class Task extends DataStructure
{
    // variables

    public enum Task_String implements Field<String>
    {
        desc
        ,location
    }

    public enum Task_Int implements Field<Integer>
    {
        repeatsType
    }

    public class Task_Calendar implements Field<TaskCalendar> {}

    public enum Task_Ids implements Field<Long>
    {
        id
        ,repeatsId
    }

    public enum Task_Boolean implements Field<Boolean>
    {
        isComplete
    }

    private void init()
    {
        set(Task_Calendar.class, new TaskCalendar());
        set(Task_Boolean.isComplete, false);
    }

    public Task()
    {
        init();
    }

    public Task(Cursor cursor)
    {
        init();
        set(Task_Ids.id, cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_ID.toString())));
        get(Task_Calendar.class).setTimeInMillis(cursor.getLong(cursor.getColumnIndex(TaskCol.TASK_DUE_DATE.toString())));
        set(Task_String.desc, cursor.getString(cursor.getColumnIndex(TaskCol.TASK_DESC.toString())));
        set(Task_Int.repeatsType, cursor.getInt(cursor.getColumnIndex(TaskCol.TASK_REPEATS_ID_FK.toString())));
        boolean isComplete = cursor.getInt(cursor.getColumnIndex(TaskCol.TASK_IS_COMPLETE.toString())) == 1
                ? true : false;
        set(Task_Boolean.isComplete, isComplete);
    }

    /**
     * Gets task
     *
     * @return
     */
    public String getTaskDesc()
    {
        return get(Task_String.desc);
    }

    /**
     * Gets date/time task is due in epoch time
     *
     * @return
     */
    public long getTimeInMillis()
    {
        return get(Task_Calendar.class).getDate().getTime();
    }

    /**
     * Gets day task is due
     *
     * @return
     */
    public int getDayForDb()
    {
        return get(Task_Calendar.class).get(Calendar.DAY_OF_WEEK);
    }

    // ///////////////////////////////////////////////////////////////////////
    // ///////////////////////////////////////////////////////////////////////
    /*
     * Start of methods used for displaying dates in user's locale
     */

    public long getId()
    {
        return get(Task_Ids.id);
    }

    public Task set(Task_Int repeatsType, RepeatsToken.Type type)
    {
        set(repeatsType, type.getType());
        return this;
    }

    public int describeContents()
    {
        return 0;
    }

   @Override
    public <T extends DataStructure> T combine(T ds)
    {
        TaskCalendar taskCalendar = get(Task_Calendar.class);

        super.combine(ds);

        set(Task_Calendar.class, taskCalendar);

        get(Task_Calendar.class).date = ds.get(Task_Calendar.class).date != null
                ? ds.get(Task_Calendar.class).date
                : get(Task_Calendar.class).date;

        get(Task_Calendar.class).time = ds.get(Task_Calendar.class).time != null
                ? ds.get(Task_Calendar.class).time
                : get(Task_Calendar.class).time;

        get(Task_Calendar.class).day = ds.get(Task_Calendar.class).day != null
                ? ds.get(Task_Calendar.class).day
                : get(Task_Calendar.class).day;

        return (T) this;
    }

    /**
     * @deprecated use a builder instead
     *
     * @return content values
     */
    public ContentValues toContentValues()
    {
        ContentValues values = new ContentValues();

        //if (get(Task_Ids.id) != null) values.put("id", get(Task_Ids.id));

        values.put(TaskCol.TASK_DESC.toString(), get(Task_String.desc));

        if (get(Task_Int.repeatsType) != null)
            values.put(TaskCol.TASK_REPEATS_ID_FK.toString(),
                       get(Task_Int.repeatsType));

        values.put(TaskCol.TASK_DUE_DATE.toString(),
                   get(Task_Calendar.class).getDate().getTime());

        values.put(TaskCol.TASK_IS_COMPLETE.toString(),
                   get(Task_Boolean.isComplete) ? 1 : 0);

        return values;
    }
}