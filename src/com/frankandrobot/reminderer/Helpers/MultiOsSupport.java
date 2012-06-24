package com.frankandrobot.reminderer.Helpers;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import android.os.Build;

/**
 * Simple helper classes
 * 
 * @author uri
 * 
 */
public interface MultiOsSupport {

    public String getDisplayName(Calendar cal, int field, int style,
	    Locale locale);

    public class Factory {
	static public MultiOsSupport newInstance() {
	    final int sdkVersion = Integer.parseInt(Build.VERSION.SDK);
	    MultiOsSupport helpers = null;
	    if (sdkVersion <= Build.VERSION_CODES.FROYO)
		return new FroyoHelpers();
	    else if (sdkVersion <= Build.VERSION_CODES.GINGERBREAD_MR1)
		return new GingerbreadHelpers();
	    // see
	    // http://developer.android.com/reference/android/os/Build.VERSION_CODES.html
	    else if (sdkVersion <= 13)
		return new HoneycombHelpers();
	    else if (sdkVersion <= 15)
		return new ICSHelpers();
	    return helpers;
	}
    }

    public class FroyoHelpers implements MultiOsSupport {
	
	@Override
	public String getDisplayName(Calendar cal, int field, int style,
		Locale locale) {
	    Date date = cal.getTime();
	    SimpleDateFormat sdf = null;
	    switch (field) {
	    case Calendar.DAY_OF_WEEK:
		String day = (style == Calendar.SHORT) ? "EEE" : "EEEE";
		sdf = new SimpleDateFormat(day);
		return sdf.format(date);
	    default:
		return "Not supported";
	    }
	}
    }

    public class GingerbreadHelpers implements MultiOsSupport {

	@Override
	public String getDisplayName(Calendar cal, int field, int style,
		Locale locale) {
	    return cal.getDisplayName(field, style, locale);
	}
    }

    public class HoneycombHelpers implements MultiOsSupport {
	GingerbreadHelpers gh = new GingerbreadHelpers();
	
	@Override
	public String getDisplayName(Calendar cal, int field, int style,
		Locale locale) {
	    return gh.getDisplayName(cal, field, style, locale);
	}

    }

    public class ICSHelpers implements MultiOsSupport {
	GingerbreadHelpers gh = new GingerbreadHelpers();
	
	@Override
	public String getDisplayName(Calendar cal, int field, int style,
		Locale locale) {
	    return gh.getDisplayName(cal, field, style, locale);
	}
    }

}
