package com.frankandrobot.reminderer.Parser;

import java.text.DateFormat;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.frankandrobot.reminderer.R;

import android.content.Context;
import android.content.res.Resources;

public class MyDateFormat {
    Resources resources;
    ParserStrategy parser;

    public MyDateFormat(Context context) {
	parser = new ParserStrategy.BruteForce(context);

    }

    public String[] parse(final String input) {
	return parser.parse(input);

    }

}

