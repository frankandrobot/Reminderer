package com.frankandrobot.reminderer.datastructures;

import java.util.Date;

/**
 * Wrapper for {@link Date}
 */
public class ReDate extends Date
{
    private Date date;
    private boolean isYearSet = true;

    public ReDate()
    {
        date = new Date();
    }

    public ReDate(int year, int month, int day)
    {
        date = new Date(year, month, day);
    }

    public ReDate(int year, int month, int day, int hour, int minute)
    {
        date = new Date(year, month, day, hour, minute);
    }

    public ReDate(int year,
                  int month,
                  int day,
                  int hour,
                  int minute,
                  int second)
    {
        date = new Date(year, month, day, hour, minute, second);
    }

    public ReDate(long milliseconds)
    {
        date = new Date(milliseconds);
    }

    public ReDate(String string)
    {
        date = new Date(string);
    }

    public ReDate(Date date)
    {
        if (date == null)
            throw new IllegalArgumentException("date is null");

        this.date = date;
    }

    public boolean isYearSet()
    {
        return isYearSet;
    }

    public ReDate setYearSet(boolean yearSet)
    {
        isYearSet = yearSet;
        return this;
    }

    @Override
    public boolean after(Date date)
    {
        return date.after(date);
    }

    @Override
    public boolean before(Date date)
    {
        return date.before(date);
    }

    @Override
    public int compareTo(Date date)
    {
        return date.compareTo(date);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getDate()
    {
        return date.getDate();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getDay()
    {
        return date.getDay();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getHours()
    {
        return date.getHours();
    }


    /**
     * @deprecated
     * @return
     */
    @Override
    public int getMinutes()
    {
        return date.getMinutes();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getMonth()
    {
        return date.getMonth();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getSeconds()
    {
        return date.getSeconds();
    }

    @Override
    public long getTime()
    {
        return date.getTime();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getTimezoneOffset()
    {
        return date.getTimezoneOffset();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public int getYear()
    {
        return date.getYear();
    }

    @Override
    public int hashCode()
    {
        return date.hashCode();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setDate(int day)
    {
        date.setDate(day);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setHours(int hour)
    {
        date.setHours(hour);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setMinutes(int minute)
    {
        date.setMinutes(minute);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setMonth(int month)
    {
        date.setMonth(month);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setSeconds(int second)
    {
        date.setSeconds(second);
    }

    @Override
    public void setTime(long milliseconds)
    {
        date.setTime(milliseconds);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public void setYear(int year)
    {
        date.setYear(year);
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public String toGMTString()
    {
        return date.toGMTString();
    }

    /**
     * @deprecated
     * @return
     */
    @Override
    public String toLocaleString()
    {
        return date.toLocaleString();
    }

    @Override
    public String toString()
    {
        return date.toString();
    }
}
