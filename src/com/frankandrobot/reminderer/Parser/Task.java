package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

import com.frankandrobot.reminderer.Parser.MetaGrammarParser.Repeats;
import com.frankandrobot.reminderer.Parser.MetaGrammarParser.RepeatsEvery;

public class Task {
    static String defaultTimeStr = "9:00am";
    Calendar calendar;
    String task;
    Repeats repeats;
    RepeatsEvery repeatsEvery;
    String location;
    // helpers
    Locale locale = Locale.getDefault();
    Calendar tmpCalendar, defaultTimeCal, rightNow;
    DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    DateFormat medDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    DateFormat shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    boolean isTimeSet = false;
    int curDay;
    int curDayOfMonth;
    private boolean isDaySet;
    private boolean isDateSet;

    /*
     * ex: right now is Sunday, June 2, 10am task Sunday 9am ==> next sunday
     * task June 3 9am
     */

    public Task() {
	// initialize calendar with current date and time
	calendar = new GregorianCalendar();
	rightNow = new GregorianCalendar();
	// get default time from defaultTimeStr and assign it to defaultTimeCal
	SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
	Date defaultTime = sdf.parse(defaultTimeStr, new ParsePosition(0));
	defaultTimeCal = initCalendar(defaultTime);
	// save current day
	curDay = calendar.get(Calendar.DAY_OF_WEEK);
	curDayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
    }

    /*
     * Helper methods
     */

    /**
     * Initializes calendar with date
     * 
     * @param cal
     * @param date
     */
    static private Calendar initCalendar(Date date) {
	Calendar cal = new GregorianCalendar();
	cal.setTime(date);
	return cal;
    }

    /**
     * Copies field from calendar b into a
     * 
     * @param a
     * @param b
     * @param field
     */
    static private void copyCalendarField(Calendar a, final Calendar b,
	    int field) {
	a.set(field, b.get(field));
    }

    /**
     * All getters have to call this method
     */
    private void calculateTimeAndDate() {
	if (!isTimeSet) {
	    // set the default time in the calendar
	    copyCalendarField(calendar, defaultTimeCal, Calendar.HOUR_OF_DAY);
	    copyCalendarField(calendar, defaultTimeCal, Calendar.MINUTE);
	    isTimeSet = true;
	}
	if (!isDateSet && !isDaySet) {
	    // we're here because you didn't set a date or a day. Ex: task 8pm
	    // need to figure out if calendar date points to date in past
	    if (calendar.before(rightNow))
		// if so then point tomorrow
		calendar.add(Calendar.DAY_OF_MONTH, 1);
	} else if (isDaySet) {
	    // we're here because you set the day but not a date
	    // need to figure out if calendar date points to date in past
	    if (calendar.before(rightNow))
		// then move date to 1 week later
		calendar.add(Calendar.DAY_OF_MONTH, 7);
	}
    }

    /*
     * Setter methods for fields
     */

    public void setTask(String task) {
	this.task = new String(task);
    }

    public void setDate(Date date) {
	isDateSet = true;
	// just get the day,month,year fields from date. Ignore time fields
	tmpCalendar = initCalendar(date);
	copyCalendarField(calendar, tmpCalendar, Calendar.MONTH);
	copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_MONTH);
	copyCalendarField(calendar, tmpCalendar, Calendar.YEAR);
    }

    public void setTime(Date time) {
	isTimeSet = true;
	// just get time fields. ignore date fields
	tmpCalendar = initCalendar(time);
	copyCalendarField(this.calendar, tmpCalendar, Calendar.HOUR_OF_DAY);
	copyCalendarField(this.calendar, tmpCalendar, Calendar.MINUTE);
    }

    public void setDay(Date day) {
	isDaySet = true;
	tmpCalendar = initCalendar(day);
	copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_WEEK);
    }

    public void setNextDay(Date day) {
	isDaySet = true;
	tmpCalendar = initCalendar(day);
	// all we care about is when the input day is the same as the current
	// day
	if (tmpCalendar.get(Calendar.DAY_OF_WEEK) == curDay) {
	    calendar.set(Calendar.DAY_OF_MONTH, curDayOfMonth + 7);
	} else
	    copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_WEEK);
    }

    /*
     * Start of database methods
     */

    public String getTaskForDb() {
	return new String(task);
    }

    public long getDateForDb() {
	calculateTimeAndDate();
	return calendar.getTimeInMillis();
    }

    public int getDayForDb() {
	calculateTimeAndDate();
	return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /*
     * Start of methods used for displaying dates in user's locale
     */

    public String getTask() {
	return new String(task);
    }

    public Date getDateObj() {
	calculateTimeAndDate();
	return calendar.getTime();
    }

    public String getLocaleDate() {
	calculateTimeAndDate();
	return medDateFormat.format(getDateObj());
    }

    public String getLocaleTime() {
	calculateTimeAndDate();
	return shortTimeFormat.format(getDateObj());
    }

    public String getLocaleDay() {
	calculateTimeAndDate();
	return calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG,
		locale);
    }

    public String toString() {
	String out = "";
	out += "Task: " + ((task == null) ? "n/a" : task) + "\n";
	out += "Date: " + getLocaleDate() + "\n";
	out += "Time: " + getLocaleTime() + "\n";
	out += "Day: " + getLocaleDay() + "\n";
	out += "Repeats: " + ((repeats == null) ? "n/a" : repeats.name())
		+ "\n";
	out += "RepeatsEvery: "
		+ ((repeatsEvery == null) ? "n/a" : repeatsEvery.name()) + "\n";
	out += "Location: " + ((location == null) ? "n/a" : location) + "\n";
	return out;
    }

}