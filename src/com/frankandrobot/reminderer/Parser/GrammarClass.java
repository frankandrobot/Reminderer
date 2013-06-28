package com.frankandrobot.reminderer.Parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.Parser.GrammarParser.GrammarContext;
import com.frankandrobot.reminderer.R;

import java.text.ParsePosition;
import java.util.HashMap;

/**
 * The {@link GrammarClass}es when taken as a whole form a Context-Free Grammar.
 * These are just a generalization of regular expressions. See
 *
 * http://springpad.com/#!/echoes2099/notebooks/contextfreegrammars/blocks
 *
 * for details.
 *
 */
public interface GrammarClass
{
    /**
     * Finds itself in the start of the input string.
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
     * TODO
     *
     * @param inputString
     * @return
     */
    public Object parse(GrammarContext inputString);

    /**
     * Internal helper classes
     */
    class Helpers
    {
        /**
         *
         * @param inputString
         * @param parser
         * @return
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

        public static boolean find(GrammarParser.GrammarContext context,
                                   DateTimeFormat parser,
                                   ParsePosition matchPos)
        {
            String[] rslt = parser.find(context.getContext());
            if (rslt == null)
                return false;
            matchPos.setIndex(rslt[0].length());
            return true;
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

    // day | date | time | occurrence | others

    /**
     * Parses and finds days in a {@link GrammarContext} (input string)
     */
    public class Day implements GrammarClass
    {
        static DateTimeFormat df = new DateTimeFormat.DayFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Day(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext inputString)
        {
            return Helpers.parseDate(inputString, df);
        }

        public boolean find(GrammarParser.GrammarContext inputString)
        {
            return Helpers.find(inputString, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    public class Date implements GrammarClass
    {
        static DateTimeFormat df = new DateTimeFormat.DateFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Date(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext inputString)
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

    public class Time implements GrammarClass
    {
        static DateTimeFormat df = new DateTimeFormat.TimeFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Time(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext inputString)
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

        public boolean parse(GrammarParser.GrammarContext context)
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
}
