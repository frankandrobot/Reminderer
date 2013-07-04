package com.frankandrobot.reminderer.parser;

import android.content.Context;

import com.frankandrobot.reminderer.datastructures.ReDate;
import com.frankandrobot.reminderer.datastructures.Task;

import java.util.LinkedList;

abstract public class GrammarRule implements IGrammarRule<Task>
{
    private Context androidContext;

    public GrammarRule(Context context)
    {
        this.androidContext = context;
    }

    public Context getAndroidContext()
    {
        return androidContext;
    }

    public void setAndroidContext(Context androidContext)
    {
        this.androidContext = androidContext;
    }

    /**
     *     // commands: commands commands | NULL
     * @return
     */
    public static class CommandsRule extends GrammarRule
    {
        private IGrammarRule<Task> command;

        public CommandsRule(Context context)
        {
            super(context);

            command = new CommandRule(context);
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            // save position
            int curPos = inputString.getPos();

            // commands commands
            Task taskPartA = command.parse(inputString);
            if (taskPartA != null)
            {
                Task taskPartB = parse(inputString);
                if (taskPartB != null)
                    return taskPartA.combine(taskPartB);
            }
            inputString.setPos(curPos);

            // NULL
            if (inputString.getContext() == null
                    || inputString.getContext().trim().equals(""))
                return new Task();

            return null;
        }
    }

    /**
        // commands: taskTime | date | next | repeats | location #list of commands
     *
     */
    public static class CommandRule extends GrammarRule
    {
        private LinkedList<IGrammarRule> llRules = new LinkedList<IGrammarRule>();

        public CommandRule(Context context)
        {
            super(context);
                        /*if (time() != null || date() != null || next() != null
                    || repeats() != null || repeatsEvery() != null
                    || location() != null)*/

            llRules.add(new TimeRule(context));
            llRules.add(new DateRule(context));
            llRules.add(new NextRule(context));
            llRules.add(new RepeatsEveryRule(context));
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            int curPos = inputString.getPos();
            for(IGrammarRule<Task> rule:llRules)
            {
                Task task = rule.parse(inputString);
                if (task != null)
                {
                    return task;
                }
            }

            return null;
        }
    }

    /**
     * // taskTime: timeParser | "at" timeParser
     */
    public static class TimeRule extends GrammarRule
    {
        private Finder at = new Finder("at");
        private Finder on = new Finder("on");
        DateTimeTerminal.Time time;

        public TimeRule(Context context)
        {
            super(context);

            time = new DateTimeTerminal.Time(context);
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            at.reset();
            on.reset();

            int curPos = inputString.getPos();

            if (at.find(inputString)) // "at" found
                inputString.gobble(at);
            else if (on.find(inputString)) // "on" found
                inputString.gobble(on);

            // gobble whitespace
            if (ContextFreeGrammar.whiteSpace.find(inputString))
                inputString.gobble(ContextFreeGrammar.whiteSpace);

            ReDate taskTime = time.parse(inputString);
            if (taskTime != null)
            {
                Task task = new Task();
                task.get(Task.Task_Calendar.class).setTime(taskTime);
                return task;
            }

            inputString.setPos(curPos);
            return null;
        }
    }

    /**
     * date: dateParser | "on" dateParser
     */
    public static class DateRule extends GrammarRule
    {
        private Finder at = new Finder("at");
        private Finder on = new Finder("on");

        private DateTimeTerminal.Date dateParser;
        private DateTimeTerminal.Day dayParser;

        public DateRule(Context context)
        {
            super(context);

            dateParser =  new DateTimeTerminal.Date(context);
            dayParser = new DateTimeTerminal.Day(context);
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            at.reset();
            on.reset();

            int curPos = inputString.getPos();

            if (at.find(inputString)) // "at" found
                inputString.gobble(at);
            else if (on.find(inputString)) // "on" found
                inputString.gobble(on);

            // gobble whitespace
            if (ContextFreeGrammar.whiteSpace.find(inputString))
                inputString.gobble(ContextFreeGrammar.whiteSpace);

            if (dateParser.find(inputString))
            {
                ReDate date = dateParser.parse(inputString);
                Task task = new Task();
                task.get(Task.Task_Calendar.class).setDate(date);
                return task;
            } else if (dayParser.find(inputString))
            {
                ReDate day = dayParser.parse(inputString);
                Task task = new Task();
                task.get(Task.Task_Calendar.class).setDay(day);
                return task;
            }
            inputString.setPos(curPos);
            return null;
        }
    }

    // next: "next" dayParser
    public static class NextRule extends GrammarRule
    {
        private Finder next = new Finder("next");
        private DateTimeTerminal.Day dayParser;

        public NextRule(Context context)
        {
            super(context);

            dayParser = new DateTimeTerminal.Day(context);
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            next.reset();
            int curPos = inputString.getPos();

            if (next.find(inputString))
            {
                inputString.gobble(next);
                // eat whitespace
                if (ContextFreeGrammar.whiteSpace.find(inputString))
                    inputString.gobble(ContextFreeGrammar.whiteSpace);

                if (dayParser.find(inputString))
                {
                    ReDate day = dayParser.parse(inputString);
                    Task task = new Task();
                    task.get(Task.Task_Calendar.class).setNextDay(day);
                    return task;
                }
            }

            inputString.setPos(curPos);
            return null;
        }
    }
    /**
     // repeatsEvery: "repeats" "every" S
     // S: timeDuration | dayParser | "hour" | "day" | "week" | "month" | "year"
     */
    public static class RepeatsEveryRule extends GrammarRule
    {
        private Finder repeats = new Finder("repeats");
        private Finder every = new Finder("every");
        private LinkedList<RepeatsToken> llTokens = new LinkedList<RepeatsToken>();

        public RepeatsEveryRule(Context context)
        {
            super(context);
            //TODO replace with XML
            for(String token:new String[]{"hour", "day", "week", "month", "year"})
            {
                llTokens.add(new RepeatsToken(token));
            }
        }

        @Override
        public Task parse(GrammarContext inputString)
        {
            repeats.reset();
            every.reset();

            int curPos = inputString.getPos();

            //get repeats
            if (repeats.find(inputString))
            {
                inputString.gobble(repeats);
                //get every
                if (every.find(inputString))
                {
                    inputString.gobble(every);

                    // get hour, day, week, month, year
                    for (RepeatsToken token : llTokens)
                    {
                        RepeatsToken match = token.parse(inputString);
                        if (match != null) // one of hourly, daily, etc found
                        {
                            Task task = new Task();
                            task.set(Task.Task_GrammarRule.repeats, match);
                            return task;
                        }
                    }
                }
            }

            // search for day
            // TODO pull out
            // TODO day
            //DateTimeTerminal.Day dayParser = new DateTimeTerminal.Day(androidContext);
            // if (dayParser.find(context))
            // TODO taskTime duration

            //reset if nothing found
            inputString.setPos(curPos);
            return null;
        }
    }

    static public class RepeatsToken implements ITerminal<RepeatsToken>
    {
        private Finder token;

        RepeatsToken(String token)
        {
            this.token = new Finder(token);
        }

        @Override
        public boolean find(GrammarContext context)
        {
            return token.find(context);
        }

        @Override
        public RepeatsToken parse(GrammarContext inputString)
        {
            if (token.find(inputString))
            {
                inputString.gobble(token);
                return this;
            }
            return null;
        }

        public String toString()
        {
            return token.value();
        }
    }
}
