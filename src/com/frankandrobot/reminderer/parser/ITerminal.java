package com.frankandrobot.reminderer.parser;

import android.content.res.Resources;

import com.frankandrobot.reminderer.R;

import java.util.HashMap;

/**
 * Implementations represent terminals that are dates and times.
 *
 */
public interface ITerminal<T> extends IGrammarRule<T>
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
    public T parse(GrammarContext inputString);

    // day | date | time | occurrence | others

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
