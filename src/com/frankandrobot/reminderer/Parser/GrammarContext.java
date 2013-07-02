package com.frankandrobot.reminderer.Parser;

/**
 * A {@link GrammarContext} is just an input string.
 * <p/>
 * As the parser progresses, it "gobbles" characters from the beginning of the input
 * string.
 * <p/>
 * - {@link #pos} keeps track of the parser position
 * - {@link #original} is the original input string
 * - {@link #context} is the current string
 */
public class GrammarContext
{
    int pos;
    String original;
    String context;

    /**
     * Creates a new {@link GrammarContext} from the input string
     *
     * @param input
     */
    GrammarContext(String input)
    {
        original = new String(input);
        context = new String(input);
        pos = 0;
    }

    /**
     * Gobbles the number of matched characters in the token
     *
     * @param token
     * @return the new context
     */
    String gobble(Finder token)
    {
        return gobble(token.end());
    }

    /**
     * Gobble i characters from the current context string
     *
     * @param i
     * @return the new context
     */
    String gobble(int i)
    {
        pos += i;
        pos = Math.min(pos, original.length());
        context = original.substring(pos);
        return getContext();
    }

    String getOriginal()
    {
        return original;
    }

    String getContext()
    {
        return context;
    }

    /**
     * Jumps to the ith position in the original string
     *
     * @param i
     * @return
     */
    String setPos(int i)
    {
        pos = i;
        context = original.substring(pos);
        return getContext();
    }

    int getPos()
    {
        return pos;
    }
}
