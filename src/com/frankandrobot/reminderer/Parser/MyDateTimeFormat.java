package com.frankandrobot.reminderer.Parser;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.frankandrobot.reminderer.R;

import android.content.Context;
import android.content.res.Resources;

public interface MyDateTimeFormat {

	/**
	 * Looks for match at start of string. If found, returns String array. First
	 * element contains the match. Second element contains the remaining string.
	 * Otherwise returns null. Don't forget to set the context!
	 * 
	 * @param input
	 * @return
	 */
	public String[] find(final String input);

	/**
	 * Returns a date if the input string represents a date. Otherwise, returns
	 * null.
	 * 
	 * @param input
	 * @return
	 */
	public Date parse(final String input);

	public void setContext(Context context);

	/**
	 * Looks for dates only (02/12, Monday, Jun 2, etc)
	 * 
	 * @author uri
	 * 
	 */
	public class DateFormat implements MyDateTimeFormat {
		Resources resources;
		DateFormatStrategy parser;

		public DateFormat() {
			parser = new DateFormatStrategy.BruteForce(
					new DateFormatStrategy.DateInstance());
		}

		public DateFormat(Context context) {
			parser = new DateFormatStrategy.BruteForce(context,
					new DateFormatStrategy.DateInstance());

		}

		public String[] find(final String input) {
			return parser.find(input);

		}

		public void setContext(Context context) {
			parser.initialize(context);
		}

		public Date parse(String input) {
			return parser.parse(input);
		}
	}

	/**
	 * Looks for time only
	 * 
	 * @author uri
	 * 
	 */
	public class TimeFormat implements MyDateTimeFormat {
		Resources resources;
		DateFormatStrategy parser;

		public TimeFormat() {
			parser = new DateFormatStrategy.BruteForce(
					new DateFormatStrategy.TimeInstance());
		}

		public TimeFormat(Context context) {
			parser = new DateFormatStrategy.BruteForce(context,
					new DateFormatStrategy.TimeInstance());

		}

		public String[] find(final String input) {
			return parser.find(input);

		}

		public void setContext(Context context) {
			parser.initialize(context);

		}

		public Date parse(String input) {
			return parser.parse(input);
		}
	}

	/**
	 * Looks for day names (defined in the user's locale)
	 * 
	 * @author uri
	 * 
	 */
	static public class DayFormat implements MyDateTimeFormat {
		SimpleDateFormat sdf = new SimpleDateFormat("EEE");
		ParsePosition pos = new ParsePosition(0);
		Date result;

		/**
		 * Looks at the start of a string for a day (defined in the user's
		 * locale). If found, returns String array containing the match (1st
		 * elem) and the remaining string (2nd elem). Otherwise, returns null.
		 * 
		 * @param input
		 * @return
		 */
		public String[] find(final String input) {
			result = null;
			result = sdf.parse(input, pos);
			if (result == null)
				return null;
			return new String[] { sdf.format(result),
					input.substring(pos.getIndex()) };
		}

		public void setContext(Context context) {
			// dont do anything, context not needed
		}

		public Date parse(String input) {
			return sdf.parse(input,new ParsePosition(0));
		}
	}
}
