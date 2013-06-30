package com.frankandrobot.reminderer.parser;

import java.util.LinkedList;

abstract public class GrammarRule
{
    /**
     *     // commands: commands commands | NULL
     * @return
     */
    public static class Commands implements IGrammarRule<Task>
    {
        private IGrammarRule<Task> command = new Command();

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
    public static class Command implements IGrammarRule<Task>
    {
        private LinkedList<IGrammarRule> llRules = new LinkedList<IGrammarRule>();

        public Command()
        {
            /*if (time() != null || date() != null || next() != null
                    || repeats() != null || repeatsEvery() != null
                    || location() != null)*/

            llRules.add(new RepeatsEvery());
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
     // repeatsEvery: "repeats" "every" S
     // S: timeDuration | dayParser | "hour" | "day" | "week" | "month" | "year"
     */
    public static class RepeatsEvery implements IGrammarRule<Task>
    {
        private Finder repeats = new Finder("repeats");
        private Finder every = new Finder("every");
        private LinkedList<RepeatsToken> llTokens = new LinkedList<RepeatsToken>();

        public RepeatsEvery()
        {
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

        public String value()
        {
            return token.value();
        }
    }
}
