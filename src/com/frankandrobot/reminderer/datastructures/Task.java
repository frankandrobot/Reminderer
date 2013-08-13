package com.frankandrobot.reminderer.datastructures;

import android.content.ContentValues;
import android.database.Cursor;

import com.frankandrobot.reminderer.database.TaskTable.Column;
import com.frankandrobot.reminderer.database.TaskTable.RepeatsCol;
import com.frankandrobot.reminderer.database.TaskTable.TaskCol;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken;
import com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.Type;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Hours;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

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
        ,repeatId_fk(TaskCol.TASK_REPEATS_ID_FK)
        ,repeatId(RepeatsCol.REPEAT_ID);

        Task_Ids(Column colname) { this.colname = colname.colname(); }
        public String colname;
        public String colname() { return colname; }
    }

    public enum Task_String implements Field<String>, Column
    {
        desc(TaskCol.TASK_DESC);
        //,location();

        Task_String(Column colname) { this.colname = colname.colname(); }
        public String colname() { return colname; }
        public String colname;
    }

    public enum Task_Int implements DataStructure.Field<Integer>, Column
    {
        repeatsType(RepeatsCol.REPEAT_TYPE);

        Task_Int(Column colname) { this.colname = colname.colname(); }
        public String colname;
        public String colname() { return colname; }
    }

    public enum Task_Parser_Calendar implements Field<TaskCalendar>, Column
    {
        dueDate(TaskCol.TASK_DUE_DATE);

        Task_Parser_Calendar(Column colname) { this.colname = colname.colname(); }
        public String colname;
        public String colname() { return colname; }
    }

    protected enum Task_Alarm_Calendar implements Field<Long>, Column
    {
        nextDueDate(RepeatsCol.NEXT_DUE_DATE);

        Task_Alarm_Calendar(Column colname) { this.colname = colname.colname(); }
        public String colname;
        public String colname() { return colname; }
    }

    public enum Task_Boolean implements Field<Boolean>, Column
    {
        isComplete(TaskCol.TASK_IS_COMPLETE);

        Task_Boolean(Column colname) { this.colname = colname.colname(); }
        public String colname;
        public String colname() { return colname; }
    }

    private void init()
    {
        set(Task_Parser_Calendar.dueDate, new TaskCalendar());
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

        if (checkColumn(Task_Ids.repeatId_fk, cursor))
        set(Task_Ids.repeatId_fk,
            cursor.getLong(cursor.getColumnIndex(Task_Ids.repeatId_fk.colname)));

        if (checkColumn(Task_Ids.repeatId, cursor))
        set(Task_Ids.repeatId,
            cursor.getLong(cursor.getColumnIndex(Task_Ids.repeatId.colname)));

        if (checkColumn(Task_String.desc, cursor))
        set(Task_String.desc,
            cursor.getString(cursor.getColumnIndex(Task_String.desc.colname)));

        if (checkColumn(Task_Int.repeatsType, cursor))
        set(Task_Int.repeatsType,
            cursor.getInt(cursor.getColumnIndex(Task_Int.repeatsType.colname)));

        if (checkColumn(Task_Parser_Calendar.dueDate, cursor))
        get(Task_Parser_Calendar.dueDate)
                .setTimeInMillis(cursor.getLong(cursor.getColumnIndex(Task_Parser_Calendar.dueDate.colname)));

        if (checkColumn(Task_Alarm_Calendar.nextDueDate, cursor))
        {
            set(Task_Alarm_Calendar.nextDueDate,
                cursor.getLong(cursor.getColumnIndex(Task_Alarm_Calendar.nextDueDate.colname)));
        }

        if (checkColumn(Task_Boolean.isComplete, cursor))
        {
            boolean isComplete = cursor.getInt(cursor.getColumnIndex(Task_Boolean.isComplete.colname)) == 1;
            set(Task_Boolean.isComplete, isComplete);
        }
    }

    /**
     * Calculates the next due date.
     * It's somewhat expensive so much sure to store the result.
     *
     * @return next due date
     */
    public long calculateNextDueDate()
    {
        if (get(Task_Int.repeatsType) == null)
        {
            return get(Task_Parser_Calendar.dueDate).getDate().getTime();
        }
        else
        {
            //due date is in future so return that
            if (get(Task_Parser_Calendar.dueDate).getDate().getTime() > System.currentTimeMillis())
            {
                return get(Task_Parser_Calendar.dueDate).getDate().getTime();
            }
            else
            {
                DateTime nextDueDate = new DateTime(get(Task_Parser_Calendar.dueDate).getDate());

                LocalDate dueDate = new LocalDate(get(Task_Parser_Calendar.dueDate).getDate());
                LocalDate today = LocalDate.now();

                switch (Type.toType(get(Task_Int.repeatsType)))
                {
                    case HOUR:
                        int hours = Hours.hoursBetween(dueDate, today).getHours();
                        nextDueDate = nextDueDate.plusHours(hours);
                        if (nextDueDate.isBefore(System.currentTimeMillis()))
                            nextDueDate = nextDueDate.plusHours(1);
                        break;
                    case DAY:
                        int days = Days.daysBetween(dueDate, today).getDays();
                        nextDueDate = nextDueDate.plusDays(days);
                        if (nextDueDate.isBefore(System.currentTimeMillis()))
                            nextDueDate = nextDueDate.plusDays(1);
                        break;
                    case WEEK:
                        int weeks = Weeks.weeksBetween(dueDate, today).getWeeks();
                        nextDueDate = nextDueDate.plusWeeks(weeks);
                        if (nextDueDate.isBefore(System.currentTimeMillis()))
                            nextDueDate = nextDueDate.plusWeeks(1);
                        break;
                    case MONTH:
                        int months = Months.monthsBetween(dueDate, today).getMonths();
                        nextDueDate = nextDueDate.plusMonths(months);
                        if (nextDueDate.isBefore(System.currentTimeMillis()))
                            nextDueDate = nextDueDate.plusMonths(1);
                        break;
                    case YEAR:
                        int years = Years.yearsBetween(dueDate, today).getYears();
                        nextDueDate = nextDueDate.plusYears(years);
                        if (nextDueDate.isBefore(System.currentTimeMillis()))
                            nextDueDate = nextDueDate.plusYears(1);
                        break;
                }

                set(Task_Alarm_Calendar.nextDueDate, nextDueDate.getMillis());
                return nextDueDate.getMillis();
            }
        }
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
        TaskCalendar taskCalendar = get(Task_Parser_Calendar.dueDate);

        super.combine(source);

        set(Task_Parser_Calendar.dueDate, taskCalendar);

        get(Task_Parser_Calendar.dueDate).date = source.get(Task_Parser_Calendar.dueDate).date != null
                ? source.get(Task_Parser_Calendar.dueDate).date
                : get(Task_Parser_Calendar.dueDate).date;

        get(Task_Parser_Calendar.dueDate).time = source.get(Task_Parser_Calendar.dueDate).time != null
                ? source.get(Task_Parser_Calendar.dueDate).time
                : get(Task_Parser_Calendar.dueDate).time;

        get(Task_Parser_Calendar.dueDate).day = source.get(Task_Parser_Calendar.dueDate).day != null
                ? source.get(Task_Parser_Calendar.dueDate).day
                : get(Task_Parser_Calendar.dueDate).day;

        return (T) this;
    }

    public ContentValues getContentValuesForInsert()
    {
        ContentValues values = new ContentValues();

        //if (get(Task_Ids.id) != null) values.put("id", get(Task_Ids.id));

        //your creating a new row, so you dont know what the repeat id is
        //values.put(Task_Ids.repeatId_fk.colname, get(Task_Ids.repeatId_fk));
        //values.put(Task_Ids.repeatId.colname, get(Task_Ids.repeatId));

        values.put(Task_String.desc.colname, get(Task_String.desc));

        values.put(Task_Int.repeatsType.colname, get(Task_Int.repeatsType));

        values.put(Task_Parser_Calendar.dueDate.colname,
                   get(Task_Parser_Calendar.dueDate).getDate().getTime());

        if (get(Task_Int.repeatsType) != null)
        {
            if (get(Task_Alarm_Calendar.nextDueDate) == null)
                throw new IllegalStateException("Forgot to calculate next due date");

            values.put(Task_Alarm_Calendar.nextDueDate.colname,
                       get(Task_Alarm_Calendar.nextDueDate));
        }

        values.put(Task_Boolean.isComplete.colname,
                   get(Task_Boolean.isComplete) ? 1 : 0);

        return values;
    }

    static boolean checkColumn(Column column, Cursor cursor)
    {
        return (cursor.getColumnIndex(column.colname()) >= 0);
    }

    public static ContentValues getTaskValuesFromInitial(ContentValues initialValues)
    {
        ContentValues taskValues = new ContentValues();
        taskValues.put(Task_String.desc.colname,
                       (String)initialValues.get(Task_String.desc.colname));
        taskValues.put(Task_Parser_Calendar.dueDate.colname,
                       (Long)initialValues.get(Task_Parser_Calendar.dueDate.colname));
        taskValues.put(Task_Boolean.isComplete.colname,
                       (Integer)initialValues.get(Task_Boolean.isComplete.colname));
        return taskValues;
    }

    public static ContentValues getRepeatValuesFromInitial(ContentValues initialValues)
    {
        ContentValues repeatValues = new ContentValues();
        repeatValues.put(Task_Ids.repeatId.colname,
                         (Long)initialValues.get(Task_Ids.repeatId.colname));
        repeatValues.put(Task_Int.repeatsType.colname,
                         (Integer)initialValues.get(Task_Int.repeatsType.colname));
        return repeatValues;
    }
}