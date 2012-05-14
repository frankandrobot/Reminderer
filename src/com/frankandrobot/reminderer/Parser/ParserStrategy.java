package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.frankandrobot.reminderer.R;

import android.content.Context;
import android.content.res.Resources;

public interface ParserStrategy {

	public void initialize(Context context);

	public String[] parse(final String input);

	public class BruteForce implements ParserStrategy {
		DateFormat longFormatter, medFormatter, shortFormatter;
		SimpleDateFormat simpleDateFormatter;
		DateStringPair dateStringPair;
		String[] customFormatPattern;
		ParsePosition pos;
		static Object lock = new Object();
		Resources resources;

		public BruteForce(Context context) {
			initialize(context);
		}

		public void initialize(Context context) {
			// setup formatters
			longFormatter = DateFormat.getDateInstance(DateFormat.LONG);
			medFormatter = DateFormat.getDateInstance(DateFormat.MEDIUM);
			shortFormatter = DateFormat.getDateInstance(DateFormat.SHORT);
			simpleDateFormatter = new SimpleDateFormat();
			pos = new ParsePosition(0);
			// setup resources
			resources = context.getResources();
		}

		public String[] parse(final String input) {
			synchronized (lock) {
				pos.setIndex(0);
				dateStringPair = null;
				// first try to match the long format
				dateStringPair = parseAndFormat(input, pos, longFormatter);
				// then try the med format
				if (dateStringPair == null)
					dateStringPair = parseAndFormat(input, pos, medFormatter);
				// then try the short format
				if (dateStringPair == null)
					dateStringPair = parseAndFormat(input, pos, shortFormatter);
				// if DateFormat failed try the simpleDateFormatter
				if (dateStringPair == null) {
					customFormatPattern = resources
							.getStringArray(R.array.date_format);
					for (int i = 0; i < customFormatPattern.length; i++) {
						simpleDateFormatter
								.applyPattern(customFormatPattern[i]);
						dateStringPair = parseAndFormat(input, pos,
								simpleDateFormatter);
						if (dateStringPair != null)
							break;
					}
				}
				// if its still null then everything failed so return null
				if (dateStringPair == null)
					return null;
				return new String[] { dateStringPair.dateString,
						input.substring(pos.getIndex()) };
			}

		}

		/**
		 * Tries to parse the input using the given formatter
		 * 
		 * side effects: pos gets updated to end of match of date
		 * 
		 * returns the formatted Date and String representation (if any)
		 * otherwise returns null
		 * 
		 * @param input
		 * @param pos
		 * @param formatter
		 */
		static private DateStringPair parseAndFormat(String input,
				ParsePosition pos, DateFormat formatter) {
			DateStringPair ds = new DateStringPair();
			ds.date = formatter.parse(input, pos);
			if (ds.date == null)
				return null;
			ds.dateString = formatter.format(ds.date);
			return ds;
		}
	}

}

class DateStringPair {
	public Date date;
	public String dateString;

	public DateStringPair() {
		date = null;
		dateString = null;
	}

	public DateStringPair(Date date, String string) {
		this.date = date;
		this.dateString = string;
		// string.substring(0);
	}
}
