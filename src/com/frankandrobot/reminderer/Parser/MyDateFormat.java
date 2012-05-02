package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.frankandrobot.reminderer.R;

import android.content.Context;
import android.content.res.Resources;

public class MyDateFormat {
    DateFormat longFormatter, medFormatter, shortFormatter;
    SimpleDateFormat simpleDateFormatter;
    Date date;
    String dateString;
    String[] customFormatPattern;
    ParsePosition pos;
    static Object lock = new Object();
    Resources resources;

    public MyDateFormat(Context context) {
	//setup formatters
	longFormatter = DateFormat.getDateInstance(DateFormat.LONG);
	medFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
	shortFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
	pos = new ParsePosition(0);
	simpleDateFormatter = new SimpleDateFormat();
	//setup resources
	resources = context.getResources();
    }

    public String[] parse(final String input) {
	synchronized (lock) {
	    pos.setIndex(0);
	    date = null;
	    dateString = null;
	    // first try to match the long format
	    parseAndFormat(input, pos, date, longFormatter, dateString);
	    // then try the med format
	    if (date == null) {
		parseAndFormat(input, pos, date, medFormatter, dateString);
	    }
	    // then try the short format
	    if (date == null) {
		parseAndFormat(input, pos, date, shortFormatter, dateString);
	    }
	    // if DateFormat failed try the simpleDateFormatter
	    customFormatPattern = resources.getStringArray(R.array.date_format);
	    for(int i=0; i<customFormatPattern.length; i++) {
		simpleDateFormatter.applyPattern(customFormatPattern[i]);
		parseAndFormat(input, pos, date, simpleDateFormatter, dateString);
		if ( date != null) break;
	    }
	    // if its still null then everything failed so return null
	    if (date == null) return null;
	    return new String[] {
		    dateString,
		    input.substring(pos.getIndex())
	    };
	}
    }

    /**
     * Tries to parse the input using the given formatter
     * 
     * side effects: (1) pos gets updated to end of match, (2) date is updated
     * with valid Date object, (3) dateString is equal to string representation
     * of date
     * 
     * @param input
     * @param pos
     * @param date
     * @param formatter
     * @param dateString
     */
    static private void parseAndFormat(String input, ParsePosition pos,
	    Date date, DateFormat formatter, String dateString) {
	date = formatter.parse(input, pos);
	if (date != null)
	    dateString = formatter.format(date);
    }
}
