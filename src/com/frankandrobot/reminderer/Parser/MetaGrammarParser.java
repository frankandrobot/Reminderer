package com.frankandrobot.reminderer.Parser;

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
	
	GrammarContext context;
	Context androidContext;
	GrammarInterpreter grammar;
	LinkedList<GrammarInterpreter.Command> commands;
	
	Finder lBracket, rBracket, lParens, rParens;
	Finder whiteSpace, whiteSpaceOrEnd;
	
	public MetaGrammarParser() {
		lBracket = new Finder("\\[");
		rBracket = new Finder("\\]");
		lParens = new Finder("\\(");
		rParens = new Finder("\\)");
		whiteSpace = new Finder("[ \t]+");
		whiteSpaceOrEnd = new Finder("[ \t]+|$");
		commands = new LinkedList<GrammarInterpreter.Command>();
	}

	public void setGrammarContext(String input) {
		context = new GrammarContext(input);
	}

	public void setAndroidContext(Context context) {
		androidContext = context;
		grammar = new GrammarInterpreter(androidContext);
	}
	
	Expression parse(String input) {
		context = new GrammarContext(input.trim());
		int curPos = 0;
		while(commands()==null) { //current pos is not a command so
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
		if ( task.trim().equals("") ) return null;
		context.setPos(curPos);
		return grammar.new Task(task,commands());
	}

	GrammarInterpreter.Commands commands() {
		// save position
		int curPos = context.getPos();
		// [ command ] commands
		if (lBracket.find(context)) {
			context.gobble(lBracket); // gobble [
			if (command() && rBracket.find(context)) {
				context.gobble(rBracket); // gobble ]
				commands.add(new OptionalCommand())
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
	GrammarInterpreter.Expression token() {
		int curPos = context.getPos();
		GrammarInterpreter.Terminal t = T();
		if (t != null)
			return grammar.new Token(t);
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

	GrammarInterpreter.Terminal T() {
		//TODO 
		//This is temporary. The terminals should be stored in a locale-dependent XML file
		Pattern p = Pattern
				.compile("[ \t]*day|[ \t]*tomorrow|[ \t]*today|[ \t]*date|[ \t]*time");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start() == 0) {
			//get actual terminal
			String terminal = context.getContext().substring(0,m.end()).trim();
			//remove term from context
			context.gobble(m.end());
			return grammar.new Terminal(terminal);
		}
		return null;
	}

	// U: next | repeats | on | at
	GrammarInterpreter.UnaryOperator U() {
		//TODO
		//Ditto
		Pattern p = Pattern.compile("[ \t]*next|[ \t]*repeats|[ \t]*on|[ \t]*at");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start()==0) {
			//get actual op
			String op = context.getContext().substring(0,m.end()).trim();
			//remove op from context
			context.gobble(m.end());
			return grammar.new UnaryOperator(op,null);
		}
		return null;
	}
	
	// B: "|"
	GrammarInterpreter.BinaryOperator B() {
		//TODO
		//The | is NOT defined in XML. However, there may be other binary ops defined in XML1
		Pattern p = Pattern.compile("[ \t]*\\|");
		Matcher m = p.matcher(context.getContext());
		if (m.find() && m.start() == 0) {
			context.gobble(m.end());
			return grammar.new BinaryOperator(null,"|",null);
		}
		return null;
	}

}