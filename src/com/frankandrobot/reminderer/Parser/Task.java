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
    // AlarmManager just needs a taskTime to schedule the alarm
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

    public Task() {
	// initialize calendar with current date and time
	calendar = new GregorianCalendar();
	// get default time from defaultTimeStr - bah very convoluted
	SimpleDateFormat sdf = new SimpleDateFormat("h:mma");
	Date defaultTime = sdf.parse(defaultTimeStr, new ParsePosition(0));
	defaultTimeCal = initCalendar(defaultTime); 
	// set the default time in the calendar
	copyCalendarField(calendar,defaultTimeCal,Calendar.HOUR_OF_DAY);
	copyCalendarField(calendar,defaultTimeCal,Calendar.MINUTE);
	// date is now set to current date and default time
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
	// just get time fields. ignore date fields
	tmpCalendar = initCalendar(time);
	copyCalendarField(this.calendar, tmpCalendar, Calendar.HOUR_OF_DAY);
	copyCalendarField(this.calendar, tmpCalendar, Calendar.MINUTE);
    }

    public void setDay(Date day) {
	tmpCalendar = initCalendar(day);
	// dunno why i have to do this:
	// if day == calendar day, then do NOT update
	if (calendar.get(Calendar.DAY_OF_WEEK) == tmpCalendar
		.get(Calendar.DAY_OF_WEEK))
	    return;
	copyCalendarField(calendar, tmpCalendar, Calendar.DAY_OF_WEEK);
    }

    public void setNextDay(Date day) {
	tmpCalendar = initCalendar(day);
	if (calendar.get(Calendar.DAY_OF_WEEK) == tmpCalendar
		.get(Calendar.DAY_OF_WEEK)) {
	    calendar.add(Calendar.DAY_OF_WEEK, 7);
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
	return calendar.getTimeInMillis();
    }

    public int getDayForDb() {
	return calendar.get(Calendar.DAY_OF_WEEK);
    }

    /*
     * Start of methods used for displaying dates in user's locale
     */

    public String getTask() {
	return new String(task);
    }

    public Date getDateObj() {
	return calendar.getTime();
    }

    public String getLocaleDate() {
	return medDateFormat.format(getDateObj());
    }

    public String getLocaleTime() {
	return shortTimeFormat.format(getDateObj());
    }

    public String getLocaleDay() {
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