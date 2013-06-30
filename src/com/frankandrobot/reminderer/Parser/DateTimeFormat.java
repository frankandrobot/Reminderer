package com.frankandrobot.reminderer.Parser;

import android.content.Context;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.frankandrobot.reminderer.R;

/**
 * Inspired by Java's {@link SimpleDateFormat}.
 *
 * Implementations focus on **ONE** format. For example, {@link DateFormat}
 * works on strings that contain dates (ex: 02/12, Monday, etc) only.
 */
public interface DateTimeFormat
{

    /**
     * <p>
     * Looks for match at start of string.
     * <p/>
     * If found, returns String array. First element contains the match.
     * Second element contains the remaining string.
     * Otherwise returns null.
     *
     * <b>Don't forget to first set the {@link Context}!</b>
     *
     * @param input input string
     * @return two element array if match found. Otherwise null. First element
     *         contains the match, second element the remaining string.
     */
    public String[] find(final String input);

    /**
     * Returns a date if the input string starts with a date.
     * Otherwise, returns null.
     *
     * @param input
     * @return
     */
    public Date parse(final String input);

    public void setContext(Context context);

    /**
     * Abstract class for {@link DateTimeFormat}s
     */
    abstract class GenericFormat implements DateTimeFormat
    {
        protected DateFormatStrategy parser;

        public String[] find(final String input)
        {
            return parser.find(input);
        }

        public void setContext(Context context)
        {
            parser.initialize(context);
        }

        public Date parse(String input)
        {
            return parser.parse(input);
        }
    }

    /**
     * Works on dates only (02/12, Monday, Jun 2, etc).
     *
     * Wrapper for {@link DateFormatStrategy} that works on
     * {@link DateFormatStrategy.DateInstance}s
     *
     */
    public class DateFormat extends GenericFormat
    {
        public DateFormat()
        {
            parser = new DateFormatStrategy.BruteForce(new DateFormatStrategy.DateInstance());
        }

        public DateFormat(Context context)
        {
            parser = new DateFormatStrategy.BruteForce(context,
                                                       new DateFormatStrategy.DateInstance());
        }
    }

    /**
     * Works on times only
     *
     * Wrapper for {@link DateFormatStrategy} that works on
     * {@link DateFormatStrategy.TimeInstance}s
     */
    public class TimeFormat extends GenericFormat
    {
        public TimeFormat()
        {
            parser = new DateFormatStrategy.BruteForce(
                    new DateFormatStrategy.TimeInstance());
        }

        public TimeFormat(Context context)
        {
            parser = new DateFormatStrategy.BruteForce(context,
                                                       new DateFormatStrategy.TimeInstance());
        }
    }

    /**
     * Works on day names only (as per defined in the user's locale). Ex: Monday.
     *
     */
    static public class DayFormat extends GenericFormat
    {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE");
        ParsePosition pos = new ParsePosition(0);
        Date result;

        /**
         * Looks at the start of a string for a day (defined in the user's
         * locale). If found, returns String array containing the match (1st
         * elem) and the remaining string (2nd elem). Otherwise, returns null.
         *
         * @param input
         * @return
         */
        @Override
        public String[] find(final String input)
        {
            result = null;
            pos.setIndex(0);
            result = sdf.parse(input, pos);
            if (result == null)
                return null;
            return new String[]{input.substring(0, pos.getIndex()),
                    input.substring(pos.getIndex())};
        }

        @Override
        public void setContext(Context context)
        {
            // dont do anything, context not needed
        }

        @Override
        public Date parse(String input)
        {
            return sdf.parse(input, new ParsePosition(0));
        }
    }
}
