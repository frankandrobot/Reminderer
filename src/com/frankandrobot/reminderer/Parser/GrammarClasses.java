package com.frankandrobot.reminderer.Parser;

import java.util.HashMap;
import java.util.Set;

import com.frankandrobot.reminderer.R;

import android.content.res.Resources;


public interface GrammarClasses {

	public class Preposition extends GrammarInterpreter.UnaryOperator {

		Preposition(GrammarInterpreter grammarInterpreter, String preposition,
				GrammarInterpreter.Token expression) {
			grammarInterpreter.super(preposition, expression);
		}

	}

	// day | date | time | occurrence | others

	public class Day extends GrammarInterpreter.Terminal {
		static MyDateTimeFormat df = new MyDateTimeFormat.DayFormat();

		Day(GrammarInterpreter grammarInterpreter, String value) {
			grammarInterpreter.super(value); // value isnt really used
			df.setContext(grammarInterpreter.getApplicationContext());
		}

		public boolean parse(MetaGrammarParser.GrammarContext context) {
			String[] rslt = df.find(context.getContext());
			if (rslt == null)
				return false;
			int newPos = rslt[0].length();
			context.setPos(context.getPos() + newPos);
			return true;
		}

	}

	public class Date extends GrammarInterpreter.Terminal {
		static MyDateTimeFormat df = new MyDateTimeFormat.DateFormat();

		Date(GrammarInterpreter grammarInterpreter, String value) {
			grammarInterpreter.super(value); // value ist really used
			df.setContext(grammarInterpreter.getApplicationContext());
		}

		public boolean parse(MetaGrammarParser.GrammarContext context) {
			String[] rslt = df.find(context.getContext());
			if (rslt == null)
				return false;
			int newPos = rslt[0].length();
			context.setPos(context.getPos() + newPos);
			return true;
		}
	}

	public class Time extends GrammarInterpreter.Terminal {
		static MyDateTimeFormat df = new MyDateTimeFormat.TimeFormat();

		Time(GrammarInterpreter grammarInterpreter, String value) {
			grammarInterpreter.super(value); // value ist really used
			df.setContext(grammarInterpreter.getApplicationContext());
		}

		public boolean parse(MetaGrammarParser.GrammarContext context) {
			String[] rslt = df.find(context.getContext());
			if (rslt == null)
				return false;
			int newPos = rslt[0].length();
			context.setPos(context.getPos() + newPos);
			return true;
		}
	}

	public class Occurrence extends GrammarInterpreter.Terminal {
		Resources resources;
		HashMap<String,Finder> finders = new HashMap<String,Finder>();
		
		Occurrence(GrammarInterpreter grammarInterpreter, String value) {
			grammarInterpreter.super(value); // never really used
			resources = grammarInterpreter.getApplicationContext().getResources();
			finders.put("daily", getString(R.string.daily));
			finders.put("weekly", getString(R.string.weekly));
			finders.put("monthly", getString(R.string.monthly));
			finders.put("yearly", getString(R.string.yearly));
		}

		public boolean parse(MetaGrammarParser.GrammarContext context) {
			if (finders.get("daily").find(context) ) {
				context.gobble(finders.get("daily"));
				return true;
			}
			else if (finders.get("weekly").find(context)) {
				context.gobble(finders.get("weekly"));
				return true;
			}
			else if (finders.get("monthly").find(context)) {
				context.gobble(finders.get("monthly"));
				return true;
			}
			else if (finders.get("yearly").find(context)) {
				context.gobble(finders.get("yearly"));
				return true;
			}
			return false;
		}
		
		private Finder getString(int id) {
			return new Finder(resources.getString(id));
		}
	}
}
