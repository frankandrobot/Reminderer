package com.frankandrobot.reminderer.parser;

import android.content.Context;

import com.frankandrobot.reminderer.parser.datetime.DateTimeFormat;

import java.text.ParsePosition;

abstract public class DateTimeTerminal
{
    /**
     * Internal helper classes
     */
    public static class AbstractDateTimeTerminal
    {

        /**
         * Parses an input string using the given {@link com.frankandrobot.reminderer.parser.datetime.DateTimeFormat}.
         *
         * @param inputString the input string
         * @param parser      a {@link com.frankandrobot.reminderer.parser.datetime.DateTimeFormat}
         * @return null if no match is found; otherwise, the matched date
         */
        public static java.util.Date parseDate(GrammarContext inputString,
                                               DateTimeFormat parser)
        {
            String[] rslt = parser.find(inputString.getContext());
            if (rslt == null)
                return null;
            inputString.gobble(rslt[0].length() + 1);
            return parser.parse(rslt[0]);

        }

        /**
         * Looks for a match using the given {@link DateTimeFormat} in the input
         * string at the given position.
         *
         * @param inputString
         * @param parser
         * @param matchPos
         * @return
         */
        public static boolean find(GrammarContext inputString,
                                   DateTimeFormat parser,
                                   ParsePosition matchPos)
        {
            String[] rslt = parser.find(inputString.getContext());
            if (rslt == null)
                return false;
            matchPos.setIndex(rslt[0].length());
            return true;
        }

    }

    /**
     * Parses and finds days in a {@link com.frankandrobot.reminderer.parser.GrammarContext} (input string)
     */
    public static class Day implements ITerminal<java.util.Date>
    {
        static DateTimeFormat df = new DateTimeFormat.DayFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Day(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.parseDate(
                    inputString,
                    df);
        }

        public boolean find(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.find(inputString,
                                                 df,
                                                 matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    /**
     * Parses and finds dates in a {@link com.frankandrobot.reminderer.parser.GrammarContext} (input string)
     */
    public static class Date implements ITerminal<java.util.Date>
    {
        static DateTimeFormat df = new DateTimeFormat.DateFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Date(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.parseDate(
                    inputString,
                    df);
        }

        public boolean find(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.find(inputString,
                                                 df,
                                                 matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    /**
     * Parses and finds times in a {@link com.frankandrobot.reminderer.parser.GrammarContext} (input string)
     */
    public static class Time implements ITerminal
    {
        static DateTimeFormat df = new DateTimeFormat.TimeFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Time(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.parseDate(
                    inputString,
                    df);
        }

        public boolean find(GrammarContext inputString)
        {
            return AbstractDateTimeTerminal.find(inputString,
                                                 df,
                                                 matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }
}
