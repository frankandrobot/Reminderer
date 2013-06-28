package com.frankandrobot.reminderer.Parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.Parser.GrammarParser.GrammarContext;
import com.frankandrobot.reminderer.R;

import java.text.ParsePosition;
import java.util.HashMap;

/**
 * Each {@link GrammarClass} implements a...if I recall correctly...left recursive
 * grammar. A grammar is a generalization of regular expressions.
 */
public interface GrammarClass
{

    public boolean find(GrammarContext context);

    public Object parse(GrammarContext context);

    /**
     * Internal helper classes
     */
    class Helpers
    {
        public static java.util.Date parseDate(GrammarContext context,
                                               MyDateTimeFormat parser)
        {
            String[] rslt = parser.find(context.getContext());
            if (rslt == null)
                return null;
            context.gobble(rslt[0].length() + 1);
            return parser.parse(rslt[0]);

        }

        public static boolean find(GrammarParser.GrammarContext context,
                                   MyDateTimeFormat parser,
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

    public class Day implements GrammarClass
    {
        static MyDateTimeFormat df = new MyDateTimeFormat.DayFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Day(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext context)
        {
            return Helpers.parseDate(context, df);
        }

        public boolean find(GrammarParser.GrammarContext context)
        {
            return Helpers.find(context, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    public class Date implements GrammarClass
    {
        static MyDateTimeFormat df = new MyDateTimeFormat.DateFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Date(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext context)
        {
            return Helpers.parseDate(context, df);
        }

        public boolean find(GrammarContext context)
        {
            return Helpers.find(context, df, matchPos);
        }

        public int end()
        {
            return matchPos.getIndex();
        }
    }

    public class Time implements GrammarClass
    {
        static MyDateTimeFormat df = new MyDateTimeFormat.TimeFormat();
        ParsePosition matchPos = new ParsePosition(0);

        Time(Context context)
        {
            df.setContext(context);
        }

        public java.util.Date parse(GrammarParser.GrammarContext context)
        {
            return Helpers.parseDate(context, df);
        }

        public boolean find(GrammarContext context)
        {
            return Helpers.find(context, df, matchPos);
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
