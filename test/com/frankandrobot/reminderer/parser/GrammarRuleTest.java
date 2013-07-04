package com.frankandrobot.reminderer.parser;

import android.content.Context;
import android.content.res.Resources;

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
        new NonStrictExpectations() {{
            context.getResources();
            result = resources;

            resources.getStringArray(anyInt);
            result = new String[]
                    {
                            "h:mma",
                            "H:mm",
                            "ha",
                            "hha",
                            "hmma",
                            "hhmma"
                    };
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
        assert(task.get(Task.Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
    }

    @Test
    public void testEvery()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world repeats every hour";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task.Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/2/2013 09:00:00");
        assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));
    }

    @Test
    public void testTime()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world at 8pm";
        Task task = grammar.parse(string);
        System.out.println(task);
        assert(task.get(Task.Task_String.desc).contains("hello world"));
        assert(sdfFull.format(task.get(Task.Task_Calendar.class).getDate()))
                .contains("7/1/2013 20:00:00");
        //assert(task.get(Task.Task_GrammarRule.repeats).toString().contains("hour"));
    }
}
