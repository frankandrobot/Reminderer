package com.frankandrobot.reminderer.parser;

import org.junit.Test;

public class GrammarRuleTest
{
    @Test
    public void test()
    {
        ContextFreeGrammar grammar = new ContextFreeGrammar();

        String string = "hello world";
        Task task = grammar.parse(string);
        System.out.println(task);

        string = "hello world repeats every hour";
        task = grammar.parse(string);
        System.out.println(task);
    }
}
