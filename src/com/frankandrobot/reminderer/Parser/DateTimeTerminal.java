package com.frankandrobot.reminderer.Parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.R;

import java.text.ParsePosition;
import java.util.HashMap;

/**
 * Implementations represent terminals that are dates and times.
 *
 */
public interface DateTimeTerminal
{
    /**
     * Finds itself in the start of the input string.
     *
     * The difference between find() and parse() is that <code>find</code>, as its name
     * implies, looks for a match while <code>parse</code> finds a match
     * and returns a Date
     *
     * Note: returns true only when a match is found in the beginning of the
     * input string. <b>It will return false even if the pattern is found
     * somewhere in the string but it is NOT the beginning.</b>
     *
     * @param inputString
     * @return
     */
    public boolean find(GrammarContext inputString);

    /**
     * Tries to parse an input string.
     *
     * If a match is found, it "gobbles" the match from the input string and
     * returns a date. Otherwise, returns null.
     *
     * The difference between find() and parse() is that <code>find</code>, as its name
     * implies, looks for a match while <code>parse</code> finds a match
     * and returns a Date
     *
     * @param inputString
     * @return
     */
    public java.util.Date parse(GrammarContext inputString);

    /**
     * Internal helper classes
     */
    class Helpers
    {
        /**
         * Parses an input string using the given {@link DateTimeFormat}.
         *
         * @param inputString the input string
         * @param parser a {@link DateTimeFormat}
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

    // day | date | time | occurrence | others

    /**
     * Parses and finds days in a {@link GrammarContext} (input string)
     */
    public class Day implements DateTimeTerminal
    {
        static DateTimeFormat df = new DateTimeFormat.DayFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Day(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return Helpers.parseDate(inputString, df);
        }

        public boolean find(GrammarContext inputString)
        {
            return Helpers.find(inputString, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    /**
     * Parses and finds dates in a {@link GrammarContext} (input string)
     */
    public class Date implements DateTimeTerminal
    {
        static DateTimeFormat df = new DateTimeFormat.DateFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Date(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return Helpers.parseDate(inputString, df);
        }

        public boolean find(GrammarContext inputString)
        {
            return Helpers.find(inputString, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    /**
     * Parses and finds times in a {@link GrammarContext} (input string)
     */
    public class Time implements DateTimeTerminal
    {
        static DateTimeFormat df = new DateTimeFormat.TimeFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Time(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarContext inputString)
        {
            return Helpers.parseDate(inputString, df);
        }

        public boolean find(GrammarContext inputString)
        {
            return Helpers.find(inputString, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    public class Occurrence extends GrammarInterpreter.Terminal
    {
        Resources resources;
        HashMap<String, Finder> finders = new HashMap<String, Finder>();

        Occurrence(GrammarInterpreter grammarInterpreter, String value)
        {
            grammarInterpreter.super(value); // never really used
            resources = grammarInterpreter.getApplicationContext()
                    .getResources();
            finders.put("daily", getString(R.string.daily));
            finders.put("weekly", getString(R.string.weekly));
            finders.put("monthly", getString(R.string.monthly));
            finders.put("yearly", getString(R.string.yearly));
        }

        public boolean parse(GrammarContext context)
        {
            if (finders.get("daily").find(context))
            {
                context.gobble(finders.get("daily"));
                return true;
            } else if (finders.get("weekly").find(context))
            {
                context.gobble(finders.get("weekly"));
                return true;
            } else if (finders.get("monthly").find(context))
            {
                context.gobble(finders.get("monthly"));
                return true;
            } else if (finders.get("yearly").find(context))
            {
                context.gobble(finders.get("yearly"));
                return true;
            }
            return false;
        }

        private Finder getString(int id)
        {
            return new Finder(resources.getString(id));
        }
    }

    public class Preposition extends GrammarInterpreter.UnaryOperator
    {

        Preposition(GrammarInterpreter grammarInterpreter, String preposition,
                    GrammarInterpreter.Token expression)
        {
            grammarInterpreter.super(preposition, expression);
        }

    }
}
