package com.frankandrobot.reminderer.parser;

import android.content.Context;
import android.content.res.Resources;

import com.frankandrobot.reminderer.datastructures.Task;

import org.junit.Before;
import org.junit.Test;

import mockit.Mocked;
import mockit.NonStrictExpectations;

public class GrammarRuleTest
{
    @Mocked
    Context context;

    @Mocked
    Resources resources;

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
    }

    @Test
    public void testEvery()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world";
        Task task = grammar.parse(string);
        System.out.println(task);

        string = "hello world repeats every hour";
        task = grammar.parse(string);
        System.out.println(task);
    }

    @Test
    public void testTime()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar(context);

        String string = "hello world";
        Task task = grammar.parse(string);
        System.out.println(task);

        string = "hello world at 8pm";
        task = grammar.parse(string);
        System.out.println(task);
    }
}
