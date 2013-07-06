package com.frankandrobot.reminderer.datastructures;

import android.os.Parcel;
import android.os.Parcelable;

import com.frankandrobot.reminderer.parser.GrammarRule;

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
public class Task extends DataStructure implements Parcelable
{
    public static final Parcelable.Creator<Task> CREATOR = new Parcelable.Creator<Task>()
    {
        public Task createFromParcel(Parcel p)
        {
            return new Task(p);
        }

        public Task[] newArray(int size)
        {
            return new Task[size];
        }
    };

    // variables

    public enum Task_String implements Field<String>
    {
        desc
        ,location
        ,repeatsType
    }

    public class Task_Calendar implements Field<TaskCalendar> {}

    private int id;

    public Task()
    {
        set(Task_Calendar.class, new TaskCalendar());
    }

    public Task(Parcel p)
    {
        set(Task_Calendar.class, new TaskCalendar());
        get(Task_Calendar.class).setTimeInMillis(p.readLong());
        set(Task_String.desc, p.readString());
        id = p.readInt();
    }

    // ////////////////////////////////////////////////////////////////////////////
    // ////////////////////////////////////////////////////////////////////////////
    /*
     * Start of database methods - convenience functions
     */

    /**
     * Gets id
     *
     * @return
     */
    public int getIdForDb()
    {
        return getId();
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

    public int getId()
    {
        return id;
    }

    public void setId(final int id)
    {
        this.id = id;
    }

    /*
     * Parcelable API
     */

/*
    public String toString()
    {
        String out = "";

        out += "Task: " + (get(Task_Desc.class) == null ? "n/a" : get(Task_Desc.class)) + "\n";
        out += "Date: " + getLocaleDate() + "\n";
        out += "Time: " + getLocaleTime() + "\n";
        out += "Day: " + getLocaleDay() + "\n";

        out += "Repeats: " + ((get(Task_GrammarRule.repeats) == null)
                ? "n/a" : get(Task_GrammarRule.repeats).value()) + "\n";
        */
/*out += "RepeatsEveryRule: "
		+ ((repeatsEvery == null) ? "n/a" : repeatsEvery.name()) + "\n";
*//*

        out += "Location: " + ((location == null) ? "n/a" : location) + "\n";
        return out;
    }
*/

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags)
    {
        // TODO finish writing Task Parcelable - add every enum
        /*set(Task_Calendar.class, new TaskCalendar());
        get(Task_Calendar.class).setTimeInMillis(p.readLong());
        set(Task_Desc.class, p.readString());
        id = p.readInt();*/

        p.writeLong(getTimeInMillis());
        p.writeLong(System.currentTimeMillis());
        p.writeString(get(Task_String.desc));
        p.writeInt(id);
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
}