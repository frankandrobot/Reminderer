package com.frankandrobot.reminderer.Date;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author uri
 * Apparently, the Date class ois deprecated. We're supposed to use the Calendar class. This is a wrapper for this class.
 * I don't know how much space a Calendar takes up but we're using only 1 Calendar for all MyDate objects.
 * It's also thread-safe.
 */
public class MyDate {
    
    private static Calendar defaultTime = new GregorianCalendar();
    private Calendar mDate;
    private static enum Month { JAN, FEB, MAR, APR, MAY, JUN, JUL, AUG, SEP, OCT, NOV, DEC };
    private static enum Day { SUN, MON, TUE, WED, THU, FRI, SAT };
    
    //used for simple thread locking
    private static Object lock = new Object(); 
    
    /**
     * Initialize mDate with current date and time
     */
    MyDate() {
	mDate = new GregorianCalendar(); 
    }
    
    /**
     * Set the year, month, day and use the current time
     * @param year
     * @param month
     * @param day
     */
    MyDate(int year, int month, int day) {
	mDate = new GregorianCalendar();
	mDate.set(year, month, day);
    }
    
    /**
     * Sets the time using current date
     * Hour is in 24-hour format
     * @param hour
     * @param min
     */
    MyDate(int hour, int min) {
	mDate = new GregorianCalendar();
	mDate.set(Calendar.HOUR_OF_DAY, hour);
	mDate.set(Calendar.MINUTE, min);
    }
    
    MyDate(int day) {
	mDate = new GregorianCalendar();
	//find next day
	mDate.set(Calendar.DAY_OF_WEEK, day);
    }
    
    void addHour(int hour) {
	mDate.add(Calendar.HOUR, hour);
    }
}

