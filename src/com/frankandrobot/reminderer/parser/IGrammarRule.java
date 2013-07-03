package com.frankandrobot.reminderer.parser;

/**
 * Implementations represent terminals that are dates and times.
 *
 */
public interface IGrammarRule<T>
{
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
}
