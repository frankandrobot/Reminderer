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
    final SimpleDateFormat now = new SimpleDateFormat("M/d/yyyy HH:mm:ss");

    @Before
    public void setUp() throws Exception
    {
        new MockUp<TaskCalendar>()
        {
            @Mock private Calendar getCalendar()
            {
                Calendar calendar = Calendar.getInstance();
                calendar.setTime(now.parse("7/1/2013 10:00:00", new ParsePosition(0))); //Monday 10am
                return calendar;
            }

        };
    }

    @Test
    public void testSetDate() throws Exception
    {
        SimpleDateFormat sdfDate = new SimpleDateFormat("M/d/yyyy");

        //set to June 29
        TaskCalendar calendar = new TaskCalendar();
        Date date = sdfDate.parse("6/29/2013");
        calendar.setDate(date);
        //this is before NOW, so should return June 29, 2014
        System.out.println(calendar.getDate());
        assertTrue(now.format(calendar.getDate()).contains("6/29/2014"));

        //set to July 1
        calendar = new TaskCalendar();
        date = sdfDate.parse("7/1/2013");
        calendar.setDate(date);
        System.out.println(calendar.getDate());
        //since you didn't set the time, it defaults to 9am
        // July 1st 9am < July 1st 10am, so its in the past
        assertTrue(now.format(calendar.getDate()).contains("7/1/2014"));

        //set to Jan 1, 2015
        calendar = new TaskCalendar();
        date = sdfDate.parse("1/1/2015");
        calendar.setDate(date);
        System.out.println(calendar.getDate());
        assertTrue(now.format(calendar.getDate()).contains("1/1/2015"));
    }

    @Test
    public void testSetTime() throws Exception
    {

    }

    @Test
    public void testSetDay() throws Exception
    {

    }

    @Test
    public void testSetNextDay() throws Exception
    {

    }}
