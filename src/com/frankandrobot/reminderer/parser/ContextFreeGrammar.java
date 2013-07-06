package com.frankandrobot.reminderer.parser;

import android.content.Context;

import com.frankandrobot.reminderer.datastructures.Task;

/**
 * A **context free grammar** is just a generalization of regular expressions.
 *
 * Each {@link IGrammarRule} corresponds to a non-terminal or terminal symbol in the
 * grammar. Each terminal tries to parse the input string---either its able to
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
        task.set(Task.Task_String.desc, taskString);

        return task.combine(commands.parse(context));
    }
/*
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

    static public class ParsingException extends Exception
    {
        private static final long serialVersionUID = 1L;

    }
    */
}