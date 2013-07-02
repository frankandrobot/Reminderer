package com.frankandrobot.reminderer.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A wrapper class for Java's {@link Pattern} and {@link Matcher} classes
 *
 * The Pattern object ignores whitespace from the beginning
 * of the input expression.
 */
public class Finder
{
    Pattern p;
    Matcher m;
    String val;

    Finder() {}

    Finder(String pattern)
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
    boolean find(GrammarContext inputString)
    {
        m = p.matcher(inputString.getContext());
        if (m.find())
            return (m.start() == 0) ? true : false;
        return false;
    }
    /**
     * Gets the position in the inputString where the pattern starts
     */
    int start()
    {
        return m.start();
    }

    /**
     * Gets the position in the inputString where the pattern ends
     * @return
     */
    int end()
    {
        return m.end();
    }

    public String value()
    {
        return val;
    }
}