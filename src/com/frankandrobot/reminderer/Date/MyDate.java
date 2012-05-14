package com.frankandrobot.reminderer.Date;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * @author uri
 * 
 *         Apparently, the Date class is deprecated. We're supposed to use the
 *         Calendar class. This is a wrapper for this class. I don't know how
 *         much space a Calendar takes up but we're using only 1 Calendar for
 *         all MyDate objects. It's also thread-safe.
 */
/**
 * @author uri
 *
 */
/**
 * @author uri
 *
 */
/**
 * @author uri
 *
 */
public class MyDate {

    private static Calendar defaultTime = new GregorianCalendar();
    private Calendar mDate;

    /**
     * Initialize mDate with current date and time
     */
    MyDate() {
	mDate = new GregorianCalendar();
    }

    /**
     * Set the year, month, day and use the default time
     * 
     * @param year
     * @param month
     * @param day
     */
    MyDate(int year, int month, int day) {
	mDate = new GregorianCalendar();
	getDefaultTime();
	mDate.set(year, month, day);
    }

    /**
     * Sets hour and min and current date. Hour is in 24-hour format.
     * 
     * @param hour
     * @param min
     */
    MyDate(int hour, int min) {
	mDate = new GregorianCalendar();
	mDate.set(Calendar.HOUR_OF_DAY, hour);
	mDate.set(Calendar.MINUTE, min);
    }

    /**
     * Sets the day using the default time
     * 
     * @param day
     */
    MyDate(int day) {
	mDate = new GregorianCalendar();
	getDefaultTime();
	// find next day
	mDate.set(Calendar.DAY_OF_WEEK, day);
    }


    //TODO
    //constructor that takes a string
    
    /**
     * Convenience function: sets hour and min using the default time 
     */
    private void getDefaultTime() {
	mDate.set(Calendar.HOUR_OF_DAY, defaultTime.get(Calendar.HOUR_OF_DAY));
	mDate.set(Calendar.MINUTE, defaultTime.get(Calendar.MINUTE));
    }

//    public protected int getCrap() {
 //   	    return 2;
  //  	}
    void addHour(int hour) {
	mDate.add(Calendar.HOUR, hour);
    }
}
