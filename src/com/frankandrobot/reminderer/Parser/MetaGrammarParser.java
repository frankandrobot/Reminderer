package com.frankandrobot.reminderer.Parser;

import java.util.Date;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.frankandrobot.reminderer.Parser.GrammarInterpreter.Expression;

import android.content.Context;

/**
 * This class parses the meta grammar
 * 
 * @author uri
 * 
 * 
 */

public class MetaGrammarParser {
    static public class GrammarContext {
	int pos;
	String original;
	String context;

	GrammarContext(String input) {
	    original = new String(input);
	    context = new String(input);
	    pos = 0;
	}

	String gobble(Finder token) {
	    return gobble(token.end());
	}

	String gobble(int i) {
	    pos += i;
	    pos = Math.min(pos, original.length());
	    context = original.substring(pos);
	    return getContext();
	}

	String getOriginal() {
	    return original;
	}

	String getContext() {
	    return context;
	}

	String setPos(int i) {
	    pos = i;
	    context = original.substring(pos);
	    return getContext();
	}

	int getPos() {
	    return pos;
	}
    }

    static public class ParsingException extends Exception {
	private static final long serialVersionUID = 1L;

    }

    static enum Repeats {
	// TODO replace this with XML
	Hourly("hourly"), Daily("daily"), Weekly("weekly"), Monthly("monthly"), Yearly(
		"yearly");

	private Finder token;

	Repeats(String occ) {
	    this.token = new Finder(occ);
	}

	boolean find(GrammarContext context) {
	    return token.find(context);
	}

	void gobble(GrammarContext context) {
	    context.gobble(token);
	}

	String value() {
	    return token.value();
	}
    }

    static enum RepeatsEvery {
	// TODO replace this with XML
	Hourly("hour"), Daily("day"), Weekly("week"), Monthly("month"), Yearly(
		"year");

	private Finder token;

	RepeatsEvery(String occ) {
	    this.token = new Finder(occ);
	}

	boolean find(GrammarContext context) {
	    return token.find(context);
	}

	void gobble(GrammarContext context) {
	    context.gobble(token);
	}

	String value() {
	    return token.value();
	}
    }

    Task task = new Task();
    GrammarContext context;
    Context androidContext;
    GrammarInterpreter grammar;
    LinkedList<GrammarInterpreter.Command> commands;

    Finder lBracket, rBracket, lParens, rParens;
    Finder nextWhiteSpace, whiteSpace;

    boolean locationRecursion = false; // hack to prevent infinite recursion

    public MetaGrammarParser() {
	lBracket = new Finder("\\[");
	rBracket = new Finder("\\]");
	lParens = new Finder("\\(");
	rParens = new Finder("\\)");
	nextWhiteSpace = new Finder("[^ \t]*[ \t]+");
	whiteSpace = new Finder("[ \t]+");
	commands = new LinkedList<GrammarInterpreter.Command>();
    }

    public void setGrammarContext(String input) {
	context = new GrammarContext(input);
    }

    public void setAndroidContext(Context context) {
	androidContext = context;
	grammar = new GrammarInterpreter(androidContext);
    }

    // expr: task | task commands
    public Task parse(String input) {
	task = new Task();
	context = new GrammarContext(input.trim());
	int curPos = 0;
	while (commands() == null) { // current pos is not a command so
	    // gobble the token
	    if (nextWhiteSpace.find(context)) {
		context.gobble(nextWhiteSpace);
		curPos = context.getPos();
	    } else {
		curPos = context.getOriginal().length();
		break;
	    }
	}
	// did we find a task?
	String taskString = context.getOriginal().substring(0, curPos);
	if (taskString.trim().equals(""))
	    return null;
	context.setPos(curPos);
	task.task = taskString;
	return commands();
    }

    // commands: command commands | NULL
    Task commands() {
	// save position
	int curPos = context.getPos();
	// command commands
	if (command() != null && commands() != null)
	    return task;
	else
	    context.setPos(curPos);
	// NULL
	if (context.getContext() == null
		|| context.getContext().trim().equals(""))
	    return task;
	return null;
    }

    // command: taskTime | date | next | repeats | location #list of commands
    Task command() {
	int curPos = context.getPos();
	if (time() != null || date() != null || next() != null
		|| repeats() != null || repeatsEvery() != null
		|| location() != null) {
	    return task;
	} else {
	    context.setPos(curPos);
	    return null;
	}
    }

    // taskTime: timeParser | "at" timeParser
    Task time() {
	int curPos = context.getPos();
	// TODO - pull out
	GrammarClasses.Time timeParser = new GrammarClasses.Time(androidContext);
	Finder at = new Finder("at");
	Finder on = new Finder("on");
	if (at.find(context)) // "at" found
	    context.gobble(at);
	else if (on.find(context)) // "on" found
	    context.gobble(on);
	// gobble whitespace
	if (whiteSpace.find(context))
	    context.gobble(whiteSpace);
	if (timeParser.find(context)) {
	    Date time = timeParser.parse(context);
	    task.setTime(time);
	    return task;
	} else
	    context.setPos(curPos);
	return null;
    }

    // date: dateParser | "on" dateParser
    Task date() {
	int curPos = context.getPos();
	// TODO - pull out
	GrammarClasses.Date dateParser = new GrammarClasses.Date(androidContext);
	GrammarClasses.Day dayParser = new GrammarClasses.Day(androidContext);
	Finder at = new Finder("at");
	Finder on = new Finder("on");
	if (at.find(context)) // "at" found
	    context.gobble(at);
	else if (on.find(context)) // "on" found
	    context.gobble(on);
	// gobble whitespace
	if (whiteSpace.find(context))
	    context.gobble(whiteSpace);
	if (dateParser.find(context)) {
	    Date date = dateParser.parse(context);
	    task.setDate(date);
	    return task;
	} else if (dayParser.find(context)) {
	    Date day = dayParser.parse(context);
	    task.setDay(day);
	    return task;
	}
	else context.setPos(curPos);
	return null;
    }

    // next: "next" dayParser
    Task next() {
	// TODO - pull out
	Finder next = new Finder("next");
	if (!next.find(context)) // "next" not found
	    return null;
	int curPos = context.getPos();
	context.gobble(next);
	// eat whitespace
	if (whiteSpace.find(context))
	    context.gobble(whiteSpace);
	GrammarClasses.Day dayParser = new GrammarClasses.Day(androidContext);
	if (!dayParser.find(context)) {
	    context.setPos(curPos);
	    return null;
	}
	Date day = dayParser.parse(context);
	task.setNextDay(day);
	return task;
    }

    // repeats: "repeats" occurrence
    // occurrence: "hourly" | "daily" | "weekly" | "monthly" | "yearly"
    Task repeats() {
	int curPos = context.getPos();
	Finder repeats = new Finder("repeats");
	if (repeats.find(context)) {
	    context.gobble(repeats); // "repeats" found
	    for (Repeats token : Repeats.values())
		if (token.find(context)) { // one of hourly, daily, etc found
		    token.gobble(context);
		    task.repeats = token;
		    return task;
		}
	}
	context.setPos(curPos);
	return null;
    }

    // repeatsEvery: "repeats" "every" S
    // S: timeDuration | dayParser | "hour" | "day" | "week" | "month" | "year"
    Task repeatsEvery() {
	int curPos = context.getPos();
	Finder repeats = new Finder("repeats");
	Finder every = new Finder("every");
	if (!repeats.find(context)) { // repeat not found
	    context.setPos(curPos);
	    return null;
	}
	context.gobble(repeats);
	// get hour, day, week, month, year
	if (!every.find(context)) { // every not found
	    context.setPos(curPos);
	    return null;
	}
	context.gobble(every);
	for (RepeatsEvery token : RepeatsEvery.values())
	    if (token.find(context)) { // one of hourly, daily, etc found
		token.gobble(context);
		task.repeatsEvery = token;
		return task;
	    }
	// search for day
	// TODO pull out
	// TODO day
	GrammarClasses.Day dayParser = new GrammarClasses.Day(androidContext);
	// if (dayParser.find(context))
	// TODO taskTime duration
	return null;
    }

    // location: "at" locationString
    Task location() {
	if (locationRecursion)
	    return null;
	Finder at = new Finder("at");
	if (!at.find(context))
	    return null;
	int curPos = context.getPos();
	context.gobble(at);
	int len = context.getPos();
	String location = "";
	// enter recursion
	locationRecursion = true;
	while (command() == null) {
	    // gobble token
	    if (nextWhiteSpace.find(context)) {
		context.gobble(nextWhiteSpace);
		// get gobbled string
		location += context.getOriginal().substring(len,
			context.getPos());
		// update len
		len = context.getPos();
	    }// if no whitespace found then we've reached end
	}
	// exit recursion
	locationRecursion = false;
	// check that location isnt null
	if (location.trim().equals("")) {
	    context.setPos(curPos);
	    return null;
	}
	task.location = location;
	return task;
    }
}