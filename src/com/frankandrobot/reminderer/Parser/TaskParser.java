package com.frankandrobot.reminderer.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

//expr: task commands
//commands: "[" command "]" commands | command commands | NULL
//command: "(" token tokens ")" | token tokens
//tokens: B token tokens | NULL
//token: T | "(" command ")" | "[" U "]" token | U token
//U: next | repeats | on | at
//B: "|" 

public class TaskParser {
	InputString context;
	Finder lBracket, rBracket, lParens, rParens;
	Finder whiteSpace, whiteSpaceOrEnd;
	
	public TaskParser() {
		lBracket = new Finder("\\[");
		rBracket = new Finder("\\]");
		lParens = new Finder("\\(");
		rParens = new Finder("\\)");
		whiteSpace = new Finder("[ \t]+");
		whiteSpaceOrEnd = new Finder("[ \t]+|$");
	}

	public void setContext(String input) {
		context = new InputString(input);
	}

	public boolean parse(String input) {
		context = new InputString(input.trim());
		int curPos = 0;
		while(!commands()) { //current pos is not a command so
			//gobble the token
			if (whiteSpace.find(context)) {
				context.gobble(whiteSpace);
				curPos = context.getPos();
			}
			else {
				curPos = context.getOriginal().length();
				break;
			}
		}
		//did we find a task?
		String task = context.getOriginal().substring(0, curPos);
		if ( task.trim().equals("") ) return false;
		context.setPos(curPos);
		return commands();
	}

	boolean commands() {
		// save position
		int curPos = context.getPos();
		// [ command ] commands
		if (lBracket.find(context)) {
			context.gobble(lBracket); // gobble [
			if (command() && rBracket.find(context)) {
				context.gobble(rBracket); // gobble ]
				return true;
			} else
				// reset context
				context.setPos(curPos);
		}
		// command commands
		if (command() && commands())
			return true;
		else
			context.setPos(curPos);
		// NULL
		if (context.getContext() == null || context.getContext().trim().equals(""))
			return true;
		return false;
	}

	// command: "(" token tokens ")" | token tokens
	boolean command() {
		int curPos = context.getPos();
		if (lParens.find(context)) {
			context.gobble(lParens); // gobble (
			if (token() && tokens() && rParens.find(context)) {
				context.gobble(rParens); // gobble )
				return true;
			}
		}
		context.setPos(curPos);
		if (token() && tokens())
			return true;
		context.setPos(curPos);
		return false;
	}

	// tokens: B token tokens | NULL
	boolean tokens() {
		int curPos = context.getPos();
		if (B() && token() && tokens())
			return true;
		context.setPos(curPos);
		return true;
		// if ( context.getContext().equals("") ) return true;
		// //else
		// context.setPos(curPos);
		// return false;
	}

	// token: T | "(" command ")" | "[" U "]" token | U token
	boolean token() {
		int curPos = context.getPos();
		if (T())
			return true;
		if (lParens.find(context)) {
			context.gobble(lParens); // gobble (
			if (command() && rParens.find(context)) {
				context.gobble(rParens);
				return true;
			} 
		} //else
		context.setPos(curPos);
		if (lBracket.find(context))	{
			context.gobble(lBracket);
			if ( U() && rBracket.find(context)) {
				context.gobble(rBracket);
				if (token()) return true;
			}
		}
		//else
		context.setPos(curPos);
		if ( U() && token() ) return true;
		context.setPos(curPos);
		return false;
	}

	boolean T() {
		Pattern p = Pattern
				.compile("[ \t]*day|[ \t]*tomorrow|[ \t]*today|[ \t]*date|[ \t]*time");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start() == 0) {
			context.gobble(m.end());
			return true;
		}
		return false;
	}

	// U: next | repeats | on | at
	boolean U() {
		Pattern p = Pattern.compile("[ \t]*next|[ \t]*repeats|[ \t]*on|[ \t]*at");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start()==0) {
			context.gobble(m.end());
			return true;
		}
		return false;
	}
	// B: "|"
	boolean B() {
		Pattern p = Pattern.compile("[ \t]*\\|");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start() == 0) {
			context.gobble(m.end());
			return true;
		}
		return false;
	}

}

class InputString {
	int pos;
	String original;
	String context;

	InputString(String input) {
		original = new String(input);
		context = new String(input);
		pos = 0;
	}

	String gobble(Finder token) {
		return gobble(token.end());
	}

	String gobble(int i) {
		pos += i;
		context = original.substring(pos);
		return getContext();
	}

	String getOriginal() { return original; }
	
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

class Finder {
	Pattern p;
	Matcher m;

	Finder(String expr) {
		p = Pattern.compile("[ \t]*" + expr);
	}

	boolean find(InputString context) {
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