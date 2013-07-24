package com.frankandrobot.reminderer.datastructures;

import org.junit.Before;
import org.junit.Test;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import mockit.Mock;
import mockit.MockUp;

import static org.junit.Assert.assertTrue;

public class TaskCalendarTest
{
    final SimpleDateFormat sdfFull = new SimpleDateFormat("M/d/yyyy HH:mm:ss");

    @Before
    public void setUp() throws Exception
    {
        new MockUp<TaskCalendar>()
        {
            @Mock private Calendar getCalendar()
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(sdfFull.parse("7/1/2013 10:00:00", new ParsePosition(0))); //Monday 10am
                return calendar;
            }

        };
    }

    @Test
    public void testSetDate() throws Exception
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/d/yyyy");

        //set to June 29 1979 (past)
        TaskCalendar calendar = new TaskCalendar();
        ReDate date = new ReDate(sdfDate.parse("6/29/1979"));
        calendar.setDate(date);
        System.out.println(calendar.getDate());
        assertTrue(sdfFull.format(calendar.getDate()).contains("6/29/1979"));

        //set to June 29 (no year)
        calendar = new TaskCalendar();
        date = new ReDate(sdfDate.parse("6/29/1979")).setYearSet(false); //ignore year
        calendar.setDate(date);
        //this is before NOW, so should return June 29, 2014
        System.out.println(calendar.getDate());
        assertTrue(sdfFull.format(calendar.getDate()).contains("6/29/2014"));

        //set to July 1 (no year)
        calendar = new TaskCalendar();
        date = new ReDate(sdfDate.parse("7/1/2013")).setYearSet(false);
        calendar.setDate(date);
        System.out.println(calendar.getDate());
        //since you didn't set the time, it defaults to 9am
        // July 1st 9am < July 1st 10am, so its in the past
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/1/2014"));

        //set to Jan 1, 2015
        calendar = new TaskCalendar();
        date = new ReDate(sdfDate.parse("1/1/2015"));
        calendar.setDate(date);
        System.out.println(calendar.getDate());
        assertTrue(sdfFull.format(calendar.getDate()).contains("1/1/2015"));
    }

    @Test
    public void testSetTime() throws Exception
    {
        final SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

        //set to Sun (before)
        TaskCalendar calendar = new TaskCalendar();
        Date date = (new ReDate(sdfTime.parse("8:00:00")));
        calendar.setTime(new ReDate(date));
        //this is before NOW, so should go to next day
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/2/2013 08:00:00"));

        //set to July 1 11am (after)
        calendar = new TaskCalendar();
        date = (new ReDate(sdfTime.parse("11:00:00")));
        calendar.setTime(new ReDate(date));
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/1/2013 11:00:00"));

    }

    @Test
    public void testSetDay() throws Exception
    {
        final SimpleDateFormat sdfDay = new SimpleDateFormat("EEE");

        //set to Sun (before)
        TaskCalendar calendar = new TaskCalendar();
        calendar.setDay((new ReDate(sdfDay.parse("Sun"))));
        //this is before NOW, so should go to next Sunday
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/7/2013 09:00:00"));

        //set to Mon 9:00 (before)
        calendar = new TaskCalendar();
        calendar.setDay((new ReDate(sdfDay.parse("Mon"))));
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/8/2013 09:00:00"));
    }

    @Test
    public void testSetNextDay() throws Exception
    {
        final SimpleDateFormat sdfDay = new SimpleDateFormat("EEE");

        //set to Sun (before)
        TaskCalendar calendar = new TaskCalendar();
        calendar.setNextDay((new ReDate(sdfDay.parse("Sun"))));
        //this is before NOW, so should go to next Sunday
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/7/2013 09:00:00"));

        //set to Mon (today)
        calendar = new TaskCalendar();
        calendar.setNextDay((new ReDate(sdfDay.parse("Mon"))));
        //this is today, so should go to next
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/8/2013 09:00:00"));

        //set to Tues 9:00 (after)
        calendar = new TaskCalendar();
        calendar.setNextDay((new ReDate(sdfDay.parse("Tue"))));
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/2/2013 09:00:00"));
    }

    @Test
    public void testAll() throws Exception
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE");
        SimpleDateFormat sdfTime = new SimpleDateFormat("HH:mm:ss");

        //set to Sun (before)
        TaskCalendar calendar = new TaskCalendar();
        calendar.setDay((new ReDate(sdfDay.parse("Sun"))));
        calendar.setTime((new ReDate(sdfTime.parse("10:00:00"))));
        //this is before NOW, so should go to next Sunday
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/7/2013 10:00:00"));

        calendar = new TaskCalendar();
        calendar.setDate(new ReDate(sdfDate.parse("7/3/2013")));
        calendar.setTime((new ReDate(sdfTime.parse("11:00:00"))));
        System.out.println(sdfFull.format(calendar.getDate()));
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/3/2013 11:00:00"));

    }

    @Test
    public void testDayDoesNotMatchDate() throws Exception
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/d/yyyy");
        SimpleDateFormat sdfDay = new SimpleDateFormat("EEE");

        TaskCalendar calendar = new TaskCalendar();
        calendar.setDay((new ReDate(sdfDay.parse("Tue"))));
        calendar.setDate(new ReDate(sdfDate.parse("7/3/2013")));
        System.out.println(sdfFull.format(calendar.getDate()));
        //should use date
        assertTrue(sdfFull.format(calendar.getDate()).contains("7/3/2013 09:00:00"));
    }
}
