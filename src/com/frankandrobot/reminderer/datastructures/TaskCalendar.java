package com.frankandrobot.reminderer.datastructures;

import android.annotation.SuppressLint;

import com.frankandrobot.reminderer.Helpers.MultiOsSupport;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

/**
 * Holds a tasks calendar.
 *
 * The task calendar is **always** a date in the future. Ex:
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
 * Task objects are created with every call of a
 * {@link com.frankandrobot.reminderer.parser.IGrammarRule}
 * so to keep memory usage down, a {@link Calendar} object is created only
 * when a getter is called (i.e., when the parser finishes parsing).
 *
 */
public class TaskCalendar extends DataStructure
{
    static private String defaultTimeStr = "9:00am";
    static private DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    static private DateFormat medDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    static private DateFormat shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    static private SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
    static private SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");

    private Calendar calendar, tmpCalendar, defaultTimeCal;
    private ReDate date, day, time;

    private Locale locale = Locale.getDefault();

    int curDay;
    long curTime;

    MultiOsSupport miscSupport; // = MultiOsSupport.Factory.newInstance();

    private boolean isNextDay;

    public TaskCalendar() {}

    private void initializeCalendars()
    {
        // initialize calendars with current date and time
        calendar = getCalendar();
        tmpCalendar = getCalendar();

        // save current date/time
        curDay = calendar.get(Calendar.DAY_OF_WEEK);
        curTime = calendar.getTimeInMillis();
        // set second/milliseconds to 0
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    /**
     * All getters have to call this method
     */
    private void calculateTimeAndDate()
    {
        if (calendar == null)
        {
            initializeCalendars();

            if (day != null)
            {
                tmpCalendar.setTime(day);
                //extract day
                copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_WEEK);
                //if setting next day, and day == curDay, set it to next week
                if (isNextDay)
                {
                    int nextDay = tmpCalendar.get(Calendar.DAY_OF_WEEK);
                    if (curDay == nextDay)
                        calendar.add(Calendar.DAY_OF_WEEK, 7);
                }
            }
            if (date != null)
            {
                tmpCalendar.setTime(date);
                //extract month, day, year
                copyCalendarField(calendar, tmpCalendar, Calendar.MONTH);
                copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_MONTH);
                if (date.isYearSet())
                    copyCalendarField(calendar, tmpCalendar, Calendar.YEAR);
            }
            if (time != null)
            {
                tmpCalendar.setTime(time);
                //extract hour, min, sec
                copyCalendarField(calendar, tmpCalendar, Calendar.HOUR);
                copyCalendarField(calendar, tmpCalendar, Calendar.MINUTE);
                copyCalendarField(calendar, tmpCalendar, Calendar.SECOND);
            }
            else //set the default time
            {
                // get default time from defaultTimeStr and assign it to defaultTimeCal
                SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
                Date defaultTime = sdf.parse(defaultTimeStr, new ParsePosition(0));
                defaultTimeCal = initCalendar(defaultTime);

                copyCalendarField(calendar, defaultTimeCal, Calendar.HOUR_OF_DAY);
                copyCalendarField(calendar, defaultTimeCal, Calendar.MINUTE);
            }
            if (date == null && day == null)
            {
                // we're here because you didn't set a date or a day. Ex: task 8pm
                // need to figure out if calendar date points to date in past
                if (calendar.getTimeInMillis() < curTime)
                    // if so then point to tomorrow
                    calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
            else if (day != null)
            {
                // we're here because you set the day but not a date
                // need to figure out if calendar date points to date in past
                if (calendar.getTimeInMillis() < curTime)
                    // then move date to 1 week later
                    calendar.add(Calendar.DAY_OF_MONTH, 7);
            }
            else if (date != null && !date.isYearSet())
            {
                // we're here because you set a year-less date but not a day
                if (calendar.getTimeInMillis() < curTime)
                    // then move date to 1 year later
                    calendar.add(Calendar.YEAR, 1);
            }

        }
    }

    /**
     * Set a date such as "June 2" or "May 1, 2000"
     *
     * @param date Date object containing the desired date
     */
    public void setDate(ReDate date)
    {
        calendar = null;
        this.date = date;
    }

    /**
     * Set a time such as "8pm"
     *
     * @param time date containing the desired time
     */
    public void setTime(ReDate time)
    {
        calendar = null;
        this.time = time;
    }

    /**
     * Set a day such as "Monday"
     *
     * @param day date containing the desired day
     */
    public void setDay(ReDate day)
    {
        calendar = null;
        this.day = day;
    }

    /**
     * Special case of setting the "next" day.
     *
     * Ex: given "buy milk next monday" and today is Monday, sets the date to
     * next Monday. (Otherwise, setNextDay is the same as setDay.)
     *
     * @param day
     */
    public void setNextDay(ReDate day)
    {
        calendar = null;
        this.day = day;
        isNextDay = true;
    }

    public Date getDate()
    {
        calculateTimeAndDate();
        return calendar.getTime();
    }

    /**
     * Gets date in user's locale
     *
     * @return
     */
    public String getLocaleDate()
    {
        calculateTimeAndDate();
        return medDateFormat.format(calendar.getTime());
    }

    /**
     * Gets time in user's locale
     *
     * @return
     */
    public String getLocaleTime()
    {
        calculateTimeAndDate();
        return shortTimeFormat.format(calendar.getTime());
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
        return calendar.getDisplayName(Calendar.DAY_OF_WEEK,
                                       Calendar.LONG,
                                       locale);
    }

    public String toString()
    {
        String out = "";

        out += "Date: " + getLocaleDate();
        out += ", Time: " + getLocaleTime();
        out += ", Day: " + getLocaleDay();

        return out;
    }

    static private Calendar getCalendar()
    {
        return new GregorianCalendar();
    }

    /**
     * Initializes calendar with date
     *
     * @param date
     */
    static private Calendar initCalendar(Date date)
    {
        Calendar cal = getCalendar();
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
}