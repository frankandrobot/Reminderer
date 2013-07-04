package com.frankandrobot.reminderer.datastructures;

import android.annotation.SuppressLint;
import android.os.Parcel;
import android.os.Parcelable;

import com.frankandrobot.reminderer.Helpers.MultiOsSupport;
import com.frankandrobot.reminderer.parser.GrammarRule;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

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

    static String defaultTimeStr = "9:00am";

    // variables

    public enum Task_GrammarRule implements Field<GrammarRule.RepeatsToken>
    {
        repeats
    }

    public class Task_Desc implements Field<String> {}

    public enum Task_Date_Boolean implements Field<Boolean>
    {
       isTimeSet
       ,isDefaultTime
    }

    public class Task_Calendar implements Field<Calendar> {}

    int id;
    Calendar rightNow;

    String location;

    // helpers
    Locale locale = Locale.getDefault();
    Calendar tmpCalendar, defaultTimeCal;
    DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    DateFormat medDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    DateFormat shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    int curDay;
    int curDayOfMonth;
    MultiOsSupport miscSupport; // = MultiOsSupport.Factory.newInstance();

    private boolean isDaySet;

    /*
     * ex: right now is Sunday, June 2, 10am task Sunday 9am ==> next sunday
     * task June 3 9am
     */
    private boolean isDateSet;

    /*
     * Helper methods
     */

    public Task()
    {
        set(Task_Date_Boolean.isTimeSet, false);
        set(Task_Date_Boolean.isDefaultTime, false);

        // initialize calendar with current date and time
        set(Task_Calendar.class, (Calendar) new GregorianCalendar());
        rightNow = new GregorianCalendar();
        // get default time from defaultTimeStr and assign it to defaultTimeCal
        SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
        Date defaultTime = sdf.parse(defaultTimeStr, new ParsePosition(0));
        defaultTimeCal = initCalendar(defaultTime);
        // save current day
        curDay = get(Task_Calendar.class).get(Calendar.DAY_OF_WEEK);
        curDayOfMonth = get(Task_Calendar.class).get(Calendar.DAY_OF_MONTH);
        // set second/milliseconds to 0
        get(Task_Calendar.class).set(Calendar.SECOND, 0);
        get(Task_Calendar.class).set(Calendar.MILLISECOND, 0);
    }

    public Task(Parcel p)
    {
        set(Task_Calendar.class, Calendar.getInstance());
        get(Task_Calendar.class).setTimeInMillis(p.readLong());
        rightNow = Calendar.getInstance();
        rightNow.setTimeInMillis(p.readLong());
        set(Task_Desc.class, p.readString());
        set(Task_Date_Boolean.isTimeSet, p.readInt() == 1);
        isDaySet = p.readInt() == 1;
        isDateSet = p.readInt() == 1;
        id = p.readInt();
    }

    /**
     * Initializes calendar with date
     *
     * @param date
     */
    static private Calendar initCalendar(Date date)
    {
        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        return cal;
    }

    /*
     * Setter methods for fields
     */

    /**
     * Copies field from calendar b into a
     *
     * @param a
     * @param b
     * @param field
     */
    static private void copyCalendarField(Calendar a, final Calendar b,
                                          int field)
    {
        a.set(field, b.get(field));
    }

    /**
     * All getters have to call this method
     */
    private void calculateTimeAndDate()
    {
        if (!get(Task_Date_Boolean.isTimeSet))
        {
            // set the default time in the calendar
            copyCalendarField(get(Task_Calendar.class), defaultTimeCal, Calendar.HOUR_OF_DAY);
            copyCalendarField(get(Task_Calendar.class), defaultTimeCal, Calendar.MINUTE);
            set(Task_Date_Boolean.isTimeSet, true);
            set(Task_Date_Boolean.isDefaultTime, true);
        }
        if (!isDateSet && !isDaySet)
        {
            // we're here because you didn't set a date or a day. Ex: task 8pm
            // need to figure out if calendar date points to date in past
            if (get(Task_Calendar.class).before(rightNow))
                // if so then point tomorrow
                get(Task_Calendar.class).add(Calendar.DAY_OF_MONTH, 1);
        } else if (isDaySet)
        {
            // we're here because you set the day but not a date
            // need to figure out if calendar date points to date in past
            if (get(Task_Calendar.class).before(rightNow))
                // then move date to 1 week later
                get(Task_Calendar.class).add(Calendar.DAY_OF_MONTH, 7);
        }
    }

    public void setDate(ReDate date)
    {
        isDateSet = true;
        // just get the day,month,year fields from date. Ignore time fields
        tmpCalendar = initCalendar(date);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.MONTH);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.DAY_OF_MONTH);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.YEAR);
    }

    public void setTime(ReDate time)
    {
        set(Task_Date_Boolean.isTimeSet, true);
        // just get time fields. ignore date fields
        tmpCalendar = initCalendar(time);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.HOUR_OF_DAY);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.MINUTE);
    }

    public void setDay(ReDate day)
    {
        isDaySet = true;
        tmpCalendar = initCalendar(day);
        copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.DAY_OF_WEEK);
    }

    public void setNextDay(ReDate day)
    {
        isDaySet = true;
        tmpCalendar = initCalendar(day);
        // all we care about is when the input day is the same as the current
        // day
        if (tmpCalendar.get(Calendar.DAY_OF_WEEK) == curDay)
        {
            get(Task_Calendar.class).set(Calendar.DAY_OF_MONTH,
                                         curDayOfMonth + 7);
        } else
            copyCalendarField(get(Task_Calendar.class), tmpCalendar, Calendar.DAY_OF_WEEK);
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
    public String getTaskForDb()
    {
        return get(Task_Desc.class);
    }

    /**
     * Gets date/time task is due in epoch time
     *
     * @return
     */
    public long getDateTimeForDb()
    {
        return getDateTime();
    }

    /**
     * Gets day task is due
     *
     * @return
     */
    public int getDayForDb()
    {
        calculateTimeAndDate();
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

    /**
     * Gets date/time as Date object
     *
     * @return
     */
    public Date getDateObj()
    {
        calculateTimeAndDate();
        return get(Task_Calendar.class).getTime();
    }

    /**
     * Gets date in user's locale
     *
     * @return
     */
    public String getLocaleDate()
    {
        calculateTimeAndDate();
        return medDateFormat.format(getDateObj());
    }

    /**
     * Gets time in user's locale
     *
     * @return
     */
    public String getLocaleTime()
    {
        calculateTimeAndDate();
        return shortTimeFormat.format(getDateObj());
    }

    /**
     * Gets day in user's locale
     *
     * @return
     */
    @SuppressLint("NewApi")
    public String getLocaleDay()
    {
        calculateTimeAndDate();
        return get(Task_Calendar.class).getDisplayName(Calendar.DAY_OF_WEEK,
                                                       Calendar.LONG,
                                                       locale);
    }

    /*
     * Parcelable API
     */

    /**
     * Gets date/time in epoch time
     *
     * @return
     */
    public long getDateTime()
    {
        calculateTimeAndDate();
        return get(Task_Calendar.class).getTimeInMillis();
    }

    public String toString()
    {
        String out = "";

        out += "Task: " + (get(Task_Desc.class) == null ? "n/a" : get(Task_Desc.class)) + "\n";
        out += "Date: " + getLocaleDate() + "\n";
        out += "Time: " + getLocaleTime() + "\n";
        out += "Day: " + getLocaleDay() + "\n";

        out += "Repeats: " + ((get(Task_GrammarRule.repeats) == null)
                ? "n/a" : get(Task_GrammarRule.repeats).value()) + "\n";
        /*out += "RepeatsEveryRule: "
		+ ((repeatsEvery == null) ? "n/a" : repeatsEvery.name()) + "\n";
*/
        out += "Location: " + ((location == null) ? "n/a" : location) + "\n";
        return out;
    }

    public int describeContents()
    {
        return 0;
    }

    public void writeToParcel(Parcel p, int flags)
    {
        // TODO finish writing Task Parcelable - add every enum
        p.writeLong(getDateTimeForDb());
        p.writeLong(rightNow.getTimeInMillis());
        p.writeString(get(Task_Desc.class));
        p.writeInt(get(Task_Date_Boolean.isTimeSet) ? 1 : 0);
        p.writeInt(isDaySet ? 1 : 0);
        p.writeInt(isDateSet ? 1 : 0);
        p.writeInt(id);
    }

    @Override
    public <T extends DataStructure> T combine(T ds)
    {
        Calendar calendar = get(Task_Calendar.class);
        Calendar dsCalendar = ds.get(Task_Calendar.class);

        super.combine(ds);

        //override only if ds has time set
        set(Task_Calendar.class,
            !ds.get(Task_Date_Boolean.isDefaultTime) ? dsCalendar : calendar);

        return (T) this;
    }
}