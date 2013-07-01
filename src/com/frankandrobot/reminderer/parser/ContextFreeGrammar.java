package com.frankandrobot.reminderer.parser;

import android.content.Context;

/**
 * A **context free grammar** is just a generalization of regular expressions.
 *
 * Each method corresponds to a non-terminal or terminal symbol in the
 * grammar. Each terminal tries to parse the input  string---either its able to
 * parse the string or it can't. Partial parsing isn't supported.
 * Non-terminals call other terminals to parse a string.
 *
 * See
 *
 * http://springpad.com/#!/echoes2099/notebooks/contextfreegrammars/blocks
 *
 * and here
 *
 * http://ikaruga2.wordpress.com/2012/06/26/reminderer-a-grammar-parser/
 *
 * for details.
 *
 */

public class ContextFreeGrammar
{
    protected Context androidContext;

    static protected Finder lBracket, rBracket, lParens, rParens;
    static protected Finder nextWhiteSpace, whiteSpace;

    protected IGrammarRule<Task> commands;

    protected boolean locationRecursion = false; // hack to prevent infinite recursion

    static
    {
        lBracket = new Finder("\\[");
        rBracket = new Finder("\\]");
        lParens = new Finder("\\(");
        rParens = new Finder("\\)");
        nextWhiteSpace = new Finder("[^ \t]*[ \t]+");
        whiteSpace = new Finder("[ \t]+");
    }

    public ContextFreeGrammar(Context context)
    {
        commands = new GrammarRule.CommandsRule(context);
    }

    public void setAndroidContext(Context context)
    {
        androidContext = context;
    }

    // expr: task | task commands
    public Task parse(String input)
    {
        GrammarContext context = new GrammarContext(input.trim());
        int curPos = 0;

        //try to get a command
        Task task = commands.parse(context);
        while (task == null)
        {
            // current pos is not a commands so gobble the token
            if (nextWhiteSpace.find(context))
            {
                context.gobble(nextWhiteSpace);
                curPos = context.getPos();
                task = commands.parse(context);
            } else
            {
                //no more next token so put position at end of input string
                curPos = context.getOriginal().length();
                break;
            }
        }

        // did we find a task?
        String taskString = context.getOriginal().substring(0, curPos);
        if (taskString.trim().equals(""))
            return null;
        context.setPos(curPos);
        task = task == null ? new Task() : task;
        task.set(Task.Task_Desc.class, taskString);

        return task.combine(commands.parse(context));
    }
/*

    // next: "next" dayParser
    Task next()
    {
        // TODO - pull out
        Finder next = new Finder("next");
        if (!next.find(context)) // "next" not found
            return null;
        int curPos = context.getPos();
        context.gobble(next);
        // eat whitespace
        if (whiteSpace.find(context))
            context.gobble(whiteSpace);
        DateTimeTerminal.Day dayParser = new DateTimeTerminal.Day(androidContext);
        if (!dayParser.find(context))
        {
            context.setPos(curPos);
            return null;
        }
        Date day = dayParser.parse(context);
        task.setNextDay(day);
        return task;
    }

    // repeats: "repeats" occurrence
    // occurrence: "hourly" | "daily" | "weekly" | "monthly" | "yearly"
    Task repeats()
    {
        int curPos = context.getPos();
        Finder repeats = new Finder("repeats");
        if (repeats.find(context))
        {
            context.gobble(repeats); // "repeats" found
            for (Repeats token : Repeats.values())
                if (token.find(context))
                { // one of hourly, daily, etc found
                    token.gobble(context);
                    task.repeats = token;
                    return task;
                }
        }
        context.setPos(curPos);
        return null;
    }

    // repeatsEvery: "repeats" "every" S
    // S: timeDuration | dayParser | "hour" | "day" | "week" | "month" | "year"
    Task repeatsEvery()
    {
        int curPos = context.getPos();
        Finder repeats = new Finder("repeats");
        Finder every = new Finder("every");
        if (!repeats.find(context))
        { // repeat not found
            context.setPos(curPos);
            return null;
        }
        context.gobble(repeats);
        // get hour, day, week, month, year
        if (!every.find(context))
        { // every not found
            context.setPos(curPos);
            return null;
        }
        context.gobble(every);
        for (RepeatsEveryRule token : RepeatsEveryRule.values())
            if (token.find(context))
            { // one of hourly, daily, etc found
                token.gobble(context);
                task.repeatsEvery = token;
                return task;
            }
        // search for day
        // TODO pull out
        // TODO day
        DateTimeTerminal.Day dayParser = new DateTimeTerminal.Day(androidContext);
        // if (dayParser.find(context))
        // TODO taskTime duration
        return null;
    }

    // location: "at" locationString
    Task location()
    {
        if (locationRecursion)
            return null;
        Finder at = new Finder("at");
        if (!at.find(context))
            return null;
        int curPos = context.getPos();
        context.gobble(at);
        int len = context.getPos();
        String location = "";
        // enter recursion
        locationRecursion = true;
        while (commands() == null)
        {
            // gobble token
            if (nextWhiteSpace.find(context))
            {
                context.gobble(nextWhiteSpace);
                // get gobbled string
                location += context.getOriginal().substring(len,
                                                            context.getPos());
                // update len
                len = context.getPos();
            }// if no whitespace found then we've reached end
        }
        // exit recursion
        locationRecursion = false;
        // check that location isnt null
        if (location.trim().equals(""))
        {
            context.setPos(curPos);
            return null;
        }
        task.location = location;
        return task;
    }

    static enum Repeats
    {
        // TODO replace this with XML
        Hourly("hourly"), Daily("daily"), Weekly("weekly"), Monthly("monthly"), Yearly(
            "yearly");
        private Finder token;

        Repeats(String occ)
        {
            this.token = new Finder(occ);
        }

        boolean find(GrammarContext context)
        {
            return token.find(context);
        }

        void gobble(GrammarContext context)
        {
            context.gobble(token);
        }

        String value()
        {
            return token.value();
        }
    }


    static public class ParsingException extends Exception
    {
        private static final long serialVersionUID = 1L;

    }
    */
}