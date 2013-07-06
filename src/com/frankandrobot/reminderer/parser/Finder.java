package com.frankandrobot.reminderer.parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A wrapper class for Java's {@link Pattern} and {@link Matcher} classes
 *
 * The Pattern object ignores whitespace from the beginning
 * of the input expression.
 */
class Finder
{
    private Pattern p;
    private Matcher m;
    private String val;

    public Finder(String pattern)
    {
        val = new String(pattern);
        p = Pattern.compile("[ \t]*" + pattern);
    }

    /**
     * <p>
     * Finds the pattern in the input string
     * <p/>
     * Note: returns true only when the pattern starts in the beginning of the
     * input string. <b>It will return false even if the pattern is found
     * somewhere in the string but it is NOT the beginning.</b>
     *
     * @param inputString a {@link GrammarContext}
     * @return true - if pattern found in the beginning of the context; false otherwise
     */
    public boolean find(GrammarContext inputString)
    {
        m = p.matcher(inputString.getContext());
        if (m.find())
            return (m.start() == 0) ? true : false;
        return false;
    }
    /**
     * Gets the position in the inputString where the pattern starts
     */
    public int start()
    {
        return m.start();
    }

    /**
     * Gets the position in the inputString where the pattern ends
     * @return
     */
    public int end()
    {
        return m.end();
    }

    public String toString()
    {
        return val;
    }

    public void reset()
    {
        if (m!=null) m.reset();
    }
}