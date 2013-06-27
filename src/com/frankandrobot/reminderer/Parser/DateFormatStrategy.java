package com.frankandrobot.reminderer.Parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.R;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * Do NOT call this interface directly. Use MyDateFormat.
 *
 * @author uri
 */
public interface DateFormatStrategy
{

    /**
     * Initializes the DateFormatStrategy.
     * <p/>
     * Don't forget to set this otherwise you'll get a null ptr exception.
     * The parser uses the context to get the system resources.
     *
     * @param context
     */
    public void initialize(Context context);

    /**
     * Looks for a date at the start of the string.
     * <p/>
     * Don't forget to initialize the context first before calling this!
     *
     * @param input
     * @return the first element is the match. The second element is the
     *         remaining string. Otherwise, returns null.
     */
    public String[] find(final String input);

    /**
     * Gets a data from the input string.
     *
     * @param input
     * @return a date if the input string represents a date. Otherwise returns
     *         null.
     */
    public Date parse(final String input);

    public interface DateFormatInstance
    {
        DateFormat getInstance(int style);
    }

    /**
     * As of 06/26/2013, the only implementation of {@link DateFormatStrategy}.
     * <p/>
     * As its name implies, parses strings and finds matches using brute force.
     * <p/>
     * There are four types of date formats to match:
     * - long format (built-in)
     * - med format (built-in)
     * - short format (built-in)
     * - custom ({@link SimpleDateFormat})
     * <p/>
     * The algorithm tries to find a match using each format in order.
     */
    public class BruteForce implements DateFormatStrategy
    {
        private DateFormatInstance dateFormat;
        private DateFormat longFormatter, medFormatter, shortFormatter;
        private SimpleDateFormat simpleDateFormatter;
        private int customFormatResourceId;
        private DateStringPair dateStringPair;
        private String[] customFormatPattern;
        private ParsePosition pos;
        private Resources resources;

        public BruteForce(DateFormatInstance dateFormat)
        {
            this.dateFormat = dateFormat;
            setFormatResourceId();
        }

        public BruteForce(Context context, DateFormatInstance dateFormat)
        {
            this.dateFormat = dateFormat;
            setFormatResourceId();
            initialize(context);
        }

        public void initialize(Context context)
        {
            // setup formatters
            longFormatter = dateFormat.getInstance(DateFormat.LONG);
            medFormatter = dateFormat.getInstance(DateFormat.MEDIUM);
            shortFormatter = dateFormat.getInstance(DateFormat.SHORT);
            simpleDateFormatter = new SimpleDateFormat();
            pos = new ParsePosition(0);
            // setup resources
            resources = context.getResources();
        }

        /**
         * Tries to parse the input using the given formatter
         * <p/>
         * side effects: pos gets updated to end of match of date
         * <p/>
         *
         * @param input
         * @param pos
         * @param formatter
         * @return the formatted Date and String representation (if any)
         * otherwise returns null
         */
        static private DateStringPair parseAndFormat(String input,
                                                     ParsePosition pos,
                                                     DateFormat formatter)
        {
            pos.setIndex(0);
            DateStringPair ds = new DateStringPair();
            ds.date = formatter.parse(input, pos);
            if (ds.date == null)
                return null;
            ds.dateString = input.substring(0, pos.getIndex());
            // formatter.format(ds.date);
            return ds;
        }

        private void setFormatResourceId()
        {
            if (dateFormat instanceof DateInstance)
                customFormatResourceId = R.array.date_format;
            if (dateFormat instanceof TimeInstance)
                customFormatResourceId = R.array.time_format;
        }

        public String[] find(final String input)
        {
            dateStringPair = null;
            // first try to match the long format
            dateStringPair = parseAndFormat(input, pos, longFormatter);
            // then try the med format
            if (dateStringPair == null)
                dateStringPair = parseAndFormat(input, pos, medFormatter);
            // then try the short format
            if (dateStringPair == null)
                dateStringPair = parseAndFormat(input, pos, shortFormatter);
            // if DateFormat failed try the simpleDateFormatter
            if (dateStringPair == null)
            {
                customFormatPattern = resources
                        .getStringArray(customFormatResourceId);
                for (int i = 0; i < customFormatPattern.length; i++)
                {
                    simpleDateFormatter.applyPattern(customFormatPattern[i]);
                    dateStringPair = parseAndFormat(input, pos,
                                                    simpleDateFormatter);
                    if (dateStringPair != null)
                    {
                        break;
                    }
                }
            }
            // if its still null then everything failed so return null
            if (dateStringPair == null)
                return null;
            return new String[]{dateStringPair.dateString,
                    input.substring(pos.getIndex())};

        }

        public Date parse(String input)
        {
            Date date;
            // first try to match the long format
            pos.setIndex(0);
            date = longFormatter.parse(input, pos);
            // then try the med format
            if (date == null)
            {
                pos.setIndex(0);
                date = medFormatter.parse(input, pos);
            }
            // then try the short format
            if (date == null)
            {
                pos.setIndex(0);
                date = shortFormatter.parse(input, pos);
            }
            // if DateFormat failed try the simpleDateFormatter
            if (date == null)
            {
                // default parsers check for year
                // if we are here then user didnt type year so add current year
                Calendar curYear = new GregorianCalendar();
                String curYearString = ":"
                        + String.valueOf(curYear.get(Calendar.YEAR));
                customFormatPattern = resources
                        .getStringArray(customFormatResourceId);
                for (int i = 0; i < customFormatPattern.length; i++)
                {
                    simpleDateFormatter.applyPattern(customFormatPattern[i] + ":yyyy");
                    pos.setIndex(0);
                    date = simpleDateFormatter.parse(input + curYearString, pos);
                    if (date != null)
                    {
                        break;
                    }
                }
            }
            return date;
        }
    }

    public class DateInstance implements DateFormatInstance
    {

        public DateFormat getInstance(int style)
        {
            return DateFormat.getDateInstance(style);
        }

    }

    public class TimeInstance implements DateFormatInstance
    {

        public DateFormat getInstance(int style)
        {
            return DateFormat.getTimeInstance(style);
        }

    }
}

class DateStringPair
{
    public Date date;
    public String dateString;

    public DateStringPair()
    {
        date = null;
        dateString = null;
    }
}
