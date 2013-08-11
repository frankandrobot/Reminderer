package com.frankandrobot.reminderer.datastructures;

import android.content.ContentValues;
import android.database.Cursor;

import com.frankandrobot.reminderer.database.TaskTable.Column;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
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
    public enum Task_Ids implements Field<Long>, Column
    {
        id(TaskCol.TASK_ID)
        ,repeatId(RepeatsCol.REPEAT_ID);

        Task_Ids(Enum colname) { this.colname = colname.toString(); }
        public String colname;
        public String colname() { return colname; }
    }

    public enum Task_String implements Field<String>, Column
    {
        desc(TaskCol.TASK_DESC);
        //,location();

        Task_String(Enum colname) { this.colname = colname.toString(); }
        public String colname() { return colname; }
        public String colname;
    }

    public enum Task_Int implements Field<Integer>, Column
    {
        repeatsType(RepeatsCol.REPEAT_TYPE);

        Task_Int(Enum colname) { this.colname = colname.toString(); }
        public String colname;
        public String colname() { return colname; }
    }

    /**
     * dueDate is used by the grammar parser.
     * nextDueDate is used by the alarm system.
     */
    public enum Task_Calendar implements Field<TaskCalendar>, Column
    {
        dueDate(TaskCol.TASK_DUE_DATE)
        ,nextDueDate(RepeatsCol.NEXT_DUE_DATE);

        Task_Calendar(Enum colname) { this.colname = colname.toString(); }
        public String colname;
        public String colname() { return colname; }
    }

    public enum Task_Boolean implements Field<Boolean>, Column
    {
        isComplete(TaskCol.TASK_IS_COMPLETE);

        Task_Boolean(Enum colname) { this.colname = colname.toString(); }
        public String colname;
        public String colname() { return colname; }
    }

    private void init()
    {
        set(Task_Calendar.dueDate, new TaskCalendar());
        set(Task_Boolean.isComplete, false);
    }

    public Task()
    {
        init();
    }

    public Task(Cursor cursor)
    {
        init();

        if (checkColumn(Task_Ids.id, cursor))
        set(Task_Ids.id, cursor.getLong(cursor.getColumnIndex(Task_Ids.id.colname)));

        if (checkColumn(Task_Ids.repeatId, cursor))
        set(Task_Ids.repeatId, cursor.getLong(cursor.getColumnIndex(Task_Ids.repeatId.colname)));

        if (checkColumn(Task_String.desc, cursor))
        set(Task_String.desc, cursor.getString(cursor.getColumnIndex(Task_String.desc.colname)));

        if (checkColumn(Task_Int.repeatsType, cursor))
        set(Task_Int.repeatsType, cursor.getInt(cursor.getColumnIndex(Task_Int.repeatsType.colname)));

        if (checkColumn(Task_Calendar.dueDate, cursor))
        get(Task_Calendar.dueDate)
                .setTimeInMillis(cursor.getLong(cursor.getColumnIndex(Task_Calendar.dueDate.colname)));

        if (checkColumn(Task_Calendar.nextDueDate, cursor))
            get(Task_Calendar.nextDueDate)
                    .setTimeInMillis(cursor.getLong(cursor.getColumnIndex(Task_Calendar.nextDueDate.colname)));

        if (checkColumn(Task_Boolean.isComplete, cursor))
        {
            boolean isComplete = cursor.getInt(cursor.getColumnIndex(Task_Boolean.isComplete.colname)) == 1;
            set(Task_Boolean.isComplete, isComplete);
        }
    }

    /**
     * Gets task
     *
     * @return task description
     */
    public String getTaskDesc()
    {
        return get(Task_String.desc);
    }

    /**
     * Gets date/time task is due in epoch time
     *
     * @return dueDate in epoch time
     */
    public long getTimeInMillis()
    {
        return get(Task_Calendar.nextDueDate) != null
               ? get(Task_Calendar.nextDueDate).getDate().getTime()
                : get(Task_Calendar.dueDate).getDate().getTime();
    }

    public Task set(Task_Int repeatsType, RepeatsToken.Type type)
    {
        set(repeatsType, type.getType());
        return this;
    }

    /**
     * Combines two tasks into one. Copies the source datastructure into this.
     *
     * Used by the grammar parser.
     *
     * @param source datastructure
     * @param <T> datastructure class
     * @return new datastructure
     */
    @Override
    public <T extends DataStructure> T combine(T source)
    {
        TaskCalendar taskCalendar = get(Task_Calendar.dueDate);

        super.combine(source);

        set(Task_Calendar.dueDate, taskCalendar);

        get(Task_Calendar.dueDate).date = source.get(Task_Calendar.dueDate).date != null
                ? source.get(Task_Calendar.dueDate).date
                : get(Task_Calendar.dueDate).date;

        get(Task_Calendar.dueDate).time = source.get(Task_Calendar.dueDate).time != null
                ? source.get(Task_Calendar.dueDate).time
                : get(Task_Calendar.dueDate).time;

        get(Task_Calendar.dueDate).day = source.get(Task_Calendar.dueDate).day != null
                ? source.get(Task_Calendar.dueDate).day
                : get(Task_Calendar.dueDate).day;

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
                   get(Task_Calendar.dueDate).getDate().getTime());

        values.put(TaskCol.TASK_IS_COMPLETE.toString(),
                   get(Task_Boolean.isComplete) ? 1 : 0);

        return values;
    }

    static boolean checkColumn(Column column, Cursor cursor)
    {
        return (cursor.getColumnIndex(column.colname()) >= 0);
    }
}