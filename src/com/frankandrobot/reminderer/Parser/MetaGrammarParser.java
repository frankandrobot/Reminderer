package com.frankandrobot.reminderer.Parser;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * This class parses the meta grammar
 * 
 * @author uri
 *
 */
public class MetaGrammarParser {
	public class Context {
		int pos;
		String original;
		String context;
	
		Context(String input) {
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
	
	Context context;
	Finder lBracket, rBracket, lParens, rParens;
	Finder whiteSpace, whiteSpaceOrEnd;
	
	public MetaGrammarParser() {
		lBracket = new Finder("\\[");
		rBracket = new Finder("\\]");
		lParens = new Finder("\\(");
		rParens = new Finder("\\)");
		whiteSpace = new Finder("[ \t]+");
		whiteSpaceOrEnd = new Finder("[ \t]+|$");
	}

	public void setContext(String input) {
		context = new Context(input);
	}

	public boolean parse(String input) {
		context = new Context(input.trim());
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
		//TODO 
		//This is temporary. The terminals should be stored in a locale-dependent XML file
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
		//TODO
		//Ditto
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
		//TODO
		//The | is NOT defined in XML. However, there may be other binary ops defined in XML1
		Pattern p = Pattern.compile("[ \t]*\\|");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start() == 0) {
			context.gobble(m.end());
			return true;
		}
		return false;
	}

}