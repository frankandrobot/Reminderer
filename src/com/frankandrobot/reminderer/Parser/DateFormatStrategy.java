package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.frankandrobot.reminderer.R;

import android.content.Context;
import android.content.res.Resources;

/**
 * Do NOT call this interface directly. Use MyDateFormat.
 * 
 * @author uri
 * 
 */
public interface DateFormatStrategy {

	/**
	 * Don't forget to set this otherwise you'll get a null ptr exception The
	 * parser uses the context to get the system resources
	 * 
	 * @param context
	 */
	public void initialize(Context context);

	/**
	 * Looks for a date at the start of the string. If match found returns a
	 * string array. The first element is the match. The second element is the
	 * remaining string. Otherwise, returns null.
	 * 
	 * Don't forget to initialize the context first!
	 * 
	 * @param input
	 * @return
	 */
	public String[] find(final String input);

	/**
	 * Returns a date if the input string represents a date. Otherwise returns
	 * null.
	 * 
	 * @param input
	 * @return
	 */
	public Date parse(final String input);

	public class BruteForce implements DateFormatStrategy {
		DateFormatInstance dateFormat;
		DateFormat longFormatter, medFormatter, shortFormatter;
		SimpleDateFormat simpleDateFormatter;
		int customFormatResourceId;
		DateStringPair dateStringPair;
		String[] customFormatPattern;
		ParsePosition pos;
		static Object lock = new Object();
		Resources resources;

		public BruteForce(DateFormatInstance dateFormat) {
			this.dateFormat = dateFormat;
			setFormatResourceId();
		}

		public BruteForce(Context context, DateFormatInstance dateFormat) {
			this.dateFormat = dateFormat;
			setFormatResourceId();
			initialize(context);
		}

		private void setFormatResourceId() {
			if (dateFormat instanceof DateInstance)
				customFormatResourceId = R.array.date_format;
			if (dateFormat instanceof TimeInstance)
				customFormatResourceId = R.array.time_format;
		}

		public void initialize(Context context) {
			// setup formatters
			longFormatter = dateFormat.getInstance(DateFormat.LONG);
			medFormatter = dateFormat.getInstance(DateFormat.MEDIUM);
			shortFormatter = dateFormat.getInstance(DateFormat.SHORT);
			simpleDateFormatter = new SimpleDateFormat();
			pos = new ParsePosition(0);
			// setup resources
			resources = context.getResources();
		}

		public String[] find(final String input) {
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
						.getStringArray(customFormatResourceId);
				for (int i = 0; i < customFormatPattern.length; i++) {
					simpleDateFormatter.applyPattern(customFormatPattern[i]);
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
			pos.setIndex(0);
			DateStringPair ds = new DateStringPair();
			ds.date = formatter.parse(input, pos);
			if (ds.date == null)
				return null;
			ds.dateString = formatter.format(ds.date);
			return ds;
		}

		public Date parse(String input) {
			Date date = null;
			// first try to match the long format
			pos.setIndex(0);
			date = longFormatter.parse(input, pos);
			// then try the med format
			if (date == null) {
				pos.setIndex(0);
				date = medFormatter.parse(input, pos);
			}
			// then try the short format
			if (date == null) {
				pos.setIndex(0);
				date = shortFormatter.parse(input, pos);
			}
			// if DateFormat failed try the simpleDateFormatter
			if (date == null) {
				customFormatPattern = resources
						.getStringArray(customFormatResourceId);
				for (int i = 0; i < customFormatPattern.length; i++) {
					simpleDateFormatter.applyPattern(customFormatPattern[i]);
					pos.setIndex(0);
					date = simpleDateFormatter.parse(input, pos);
					if (date != null)
						break;
				}
			}
			return date;
		}
	}

	public interface DateFormatInstance {
		DateFormat getInstance(int style);
	}

	public class DateInstance implements DateFormatInstance {

		public DateFormat getInstance(int style) {
			return DateFormat.getDateInstance(style);
		}

	}

	public class TimeInstance implements DateFormatInstance {

		public DateFormat getInstance(int style) {
			return DateFormat.getTimeInstance(style);
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
