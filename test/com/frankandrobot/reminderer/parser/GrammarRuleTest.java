package com.frankandrobot.reminderer.parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.R;
import com.frankandrobot.reminderer.datastructures.Task;
import com.frankandrobot.reminderer.datastructures.TaskCalendar;

import org.junit.Before;
import org.junit.Test;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import mockit.Mock;
import mockit.MockUp;
import mockit.Mocked;
import mockit.NonStrictExpectations;

import static com.frankandrobot.reminderer.datastructures.Task.*;
import static com.frankandrobot.reminderer.parser.GrammarRule.RepeatsToken.*;

public class GrammarRuleTest
{
    @Mocked
    Context context;

    @Mocked
    Resources resources;

    final SimpleDateFormat sdfFull = new SimpleDateFormat("M/d/yyyy HH:mm:ss");

    @Before
    public void init()
    {
        new MockUp<Resources>()
        {
            @Mock
            public String[] getStringArray (int id)
            {
                if (id == R.array.time_format)
                    return new String[]
                            {
                                    "h:mma",
                                    "H:mm",
                                    "ha",
                                    "hha",
                                    "hmma",
                                    "hhmma"
                            };
                if (id == R.array.date_format)
                    return new String[]
                            {
                            "M/d",
                            "MMM d"
                            };
                return null;
            }

        };

        new NonStrictExpectations() {{
            context.getResources();
            result = resources;
        }};

        new MockUp<TaskCalendar>()
        {
            @Mock
            private Calendar getCalendar()
            {
                Calendar calendar = Calendar.getInstance();
                //Monday 10am
                calendar.setTime(sdfFull.parse("7/1/2013 10:00:00", new ParsePosition(0)));
                return calendar;
            }

        };
    }

    @Test
    public void testDesc()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
    }

    @Test
    public void testTime()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world at 8pm";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/1/2013 20:00:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));

        string = "hello world at 7:05am";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 07:05:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));

        string = "hello world 7:05am";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 07:05:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));
    }

    @Test
    public void testDateRule()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world June 1"; // past
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("6/1/2014 09:00:00");

        string = "hello world on June 1, 2013"; //past
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("6/1/2013 09:00:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));

        string = "hello world Aug 1";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("8/1/2013 09:00:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));
    }

    @Test
    public void testNextRule()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world next Monday";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/8/2013 09:00:00");

        string = "hello world next Tuesday";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");

    }

    @Test
    public void testRepeats()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world repeats every hour";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.HOUR == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats every day";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.DAY == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats every week";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.WEEK == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats every month";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.MONTH == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats every year";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.YEAR == Type.valueOf(task.get(Task_String.repeatsType)));

    }

    @Test
    public void testRepeatsEvery()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world repeats hourly";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.HOUR == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats daily";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.DAY == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats weekly";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.WEEK == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats monthly";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.MONTH == Type.valueOf(task.get(Task_String.repeatsType)));

        string = "hello world repeats yearly";
        task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(Type.YEAR == Type.valueOf(task.get(Task_String.repeatsType)));

    }
}
