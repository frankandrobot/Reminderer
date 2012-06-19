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
    Calendar tmpCalendar, defaultTimeCal;
    DateFormat shortDateFormat = DateFormat.getDateInstance(DateFormat.SHORT);
    DateFormat medDateFormat = DateFormat.getDateInstance(DateFormat.MEDIUM);
    DateFormat shortTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT);
    SimpleDateFormat sdfDate = new SimpleDateFormat("MM/dd/yyyy");
    SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm");
    boolean isTimeSet = false;
    int curDay;

    /*
     * ex: right now is Sunday, June 2, 10am task Sunday 9am ==> next sunday
     * task June 3 9am
     */

    public Task() {
	// initialize calendar with current date and time
	calendar = new GregorianCalendar();
	// get default time from defaultTimeStr and assign it to defaultTimeCal
	SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
	Date defaultTime = sdf.parse(defaultTimeStr, new ParsePosition(0));
	defaultTimeCal = initCalendar(defaultTime);
	// save current day
	curDay = calendar.get(Calendar.DAY_OF_WEEK);
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
    private void calculateTime() {
	if (!isTimeSet) {
	    // set the default time in the calendar
	    copyCalendarField(calendar, defaultTimeCal, Calendar.HOUR_OF_DAY);
	    copyCalendarField(calendar, defaultTimeCal, Calendar.MINUTE);
	    isTimeSet = true;
	}
    }

    /*
     * Setter methods for fields
     */

    public void setTask(String task) {
	this.task = new String(task);
    }

    public void setDate(Date date) {
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
	tmpCalendar = initCalendar(day);
	copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_WEEK);
    }

    public void setNextDay(Date day) {
	tmpCalendar = initCalendar(day);
	// all we care about is when the input day is the same as the current
	// day
	if (tmpCalendar.get(Calendar.DAY_OF_WEEK) == curDay) {
	    int dayOfMonth = tmpCalendar.get(Calendar.DAY_OF_MONTH);
	    calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth + 7);
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
	calculateTime();
	return calendar.getTimeInMillis();
    }

    public int getDayForDb() {
	calculateTime();
	return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /*
     * Start of methods used for displaying dates in user's locale
     */

    public String getTask() {
	return new String(task);
    }

    public Date getDateObj() {
	calculateTime();
	return calendar.getTime();
    }

    public String getLocaleDate() {
	calculateTime();
	return medDateFormat.format(getDateObj());
    }

    public String getLocaleTime() {
	calculateTime();
	return shortTimeFormat.format(getDateObj());
    }

    public String getLocaleDay() {
	calculateTime();
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