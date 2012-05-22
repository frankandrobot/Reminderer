package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

//import com.frankandrobot.reminderer.R;

/**
 * This class enumerates the different date formats
 * 
 * To run: (1) comment out package name above (2) run javac
 * DateFormatTester.java && java DateFormatTester
 * 
 * @author uri
 * 
 */
public class DateFormatTester {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		DateFormat df = DateFormat.getTimeInstance(DateFormat.LONG);
		System.out.println("Long: " + df.format(new Date()));
		df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		System.out.println("Medium: " + df.format(new Date()));
		df = DateFormat.getTimeInstance(DateFormat.SHORT);
		System.out.println("Short: " + df.format(new Date()));
		// Long: 5:35:17 PM EDT
		// Medium: 5:35:17 PM
		// Short: 5:35 PM
		df = DateFormat.getTimeInstance(DateFormat.MEDIUM);
		try {
			Date time = df.parse("5:35:30  pm"); // this doesnt work
			time = df.parse("5:35:30pm"); // this doesnt work
		} catch (Exception e) {
			System.out.println("didnt work");
		}
		String[] timeFormats = new String[] {
				"h:mma",
				"H:mm",
				"hmma",
				"Hmm", //probably want to disable this
				"hhmma"
		};
		System.out.println(applyCustomFormat(timeFormats[0],"2:01pm"));
		System.out.println(applyCustomFormat(timeFormats[1],"13:01pm"));
		System.out.println(applyCustomFormat(timeFormats[2],"601pm"));
		System.out.println(applyCustomFormat(timeFormats[2],"1005pm"));
		System.out.println(applyCustomFormat(timeFormats[3],"1305"));
		System.out.println(applyCustomFormat(timeFormats[3],"005"));
		System.out.println(applyCustomFormat(timeFormats[4],"1005pm"));
		System.out.println(applyCustomFormat(timeFormats[4],"505pm"));
//		2:01pm matches h:mma as 2:01 PM
//		13:01pm matches H:mm as 1:01 PM
//		601pm matches hmma as 6:01 PM
//		1005pm fails to match hmma
//		1305 matches Hmm as 6:05 AM
//		005 matches Hmm as 12:05 AM
//		1005pm matches hhmma as 10:05 PM
//		505pm matches hhmma as 2:05 PM

	}

	static String applyCustomFormat(String format, String test) {
		SimpleDateFormat sdf = new SimpleDateFormat();
		sdf.applyPattern(format);
		ParsePosition pos = new ParsePosition(0);
		Date rslt = sdf.parse(test, pos);
		if (rslt==null) return test+" fails to match "+format;
		DateFormat df = DateFormat.getTimeInstance(DateFormat.SHORT);
		return test+" matches "+format+" as "+df.format(rslt);
	}
}
