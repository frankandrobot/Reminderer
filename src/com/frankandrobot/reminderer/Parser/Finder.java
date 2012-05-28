package com.frankandrobot.reminderer.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frankandrobot.reminderer.Parser.MetaGrammarParser.GrammarContext;

public class Finder {
	Pattern p;
	Matcher m;
	
	Finder() {}

	Finder(String expr) {
		p = Pattern.compile("[ \t]*" + expr);
	}

	/**
	 * Finds the compiled pattern in the context
	 * If it finds it at the beginning of the context, return true
	 * Otherwise return false;
	 * @param context
	 * @return
	 */
	boolean find(GrammarContext context) {
		m = p.matcher(context.getContext());
		if (m.find())
			return (m.start() == 0) ? true : false;
		return false;
	}

	int start() {
		return m.start();
	}

	int end() {
		return m.end();
	}
}