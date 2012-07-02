package com.frankandrobot.reminderer.Parser;

import java.util.LinkedList;

import android.content.Context;

import com.frankandrobot.reminderer.Parser.GrammarParser;

/**
 * This implements the Interpreter design pattern. The GrammarParser
 * constructs objects of this interface.
 * 
 * "Generic" classes are here. Locale specific classes are in GrammarClasses.
 * 
 * @author uri
 * 
 */
public class GrammarInterpreter {
	Context context;
	static public Finder lBracket = new Finder("\\[");
	static public Finder rBracket = new Finder("\\]");
	static public Finder lParens = new Finder("\\(");
	static public Finder rParens = new Finder("\\)");
	static public Finder whiteSpace = new Finder("[ \t]+");

	/**
	 * Terminal classes are the only classes that actually use the Context
	 * 
	 * @param context
	 */
	GrammarInterpreter(Context context) {
		this.context = context;
	}

	Context getApplicationContext() { return context; }
	
	public interface Expression {

		public boolean parse(GrammarParser.GrammarContext context);

		public boolean interpret(GrammarParser.GrammarContext context);

	}

	public class Task implements Expression {
		String task;
		Commands commands;

		Task(String task, Commands commands) {
			this.task = new String(task);
			this.commands = commands;
		}

		/*
		 * Gets the task and the commands from the context
		 */
		public boolean parse(GrammarParser.GrammarContext context) {
			int curPos = 0;
			while (!commands.parse(context)) { // current pos is not a command
												// so
				// gobble the token
				if (whiteSpace.find(context)) {
					context.gobble(whiteSpace);
					curPos = context.getPos();
				} else {
					curPos = context.getOriginal().length();
					break;
				}
			}
			// did we find a task?
			String task = context.getOriginal().substring(0, curPos);
			if (task.trim().equals(""))
				return false;
			context.setPos(curPos);
			return commands.parse(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class Commands implements Expression {
		LinkedList<Command> commands;

		Commands(LinkedList<Command> commands) {
			this.commands = new LinkedList<Command>();
			for (Command command : commands) {
				this.commands.add(command);
			}
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			// save position
			int curPos = context.getPos();
			for (Command com : commands) {
				if (!com.parse(context)) {
					context.setPos(curPos);
					return false;
				}
			}
			return true;
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Command implements Expression {
		Token token;

		Command(Token token) {
			this.token = token;
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			return token.parse(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class OptionalCommand extends Command {

		OptionalCommand(Token token) {
			super(token);
			// TODO Auto-generated constructor stub
		}
		
		public boolean parse(GrammarParser.GrammarContext context) {
			int pos = context.getPos();
			if ( super.parse(context) ) return true;
			context.setPos(pos);
			return true;
		}
		
	}
	
	public class BinaryOperator implements Expression {
		Token a;
		Token b;
		Finder op;

		BinaryOperator(Token a, String op, Token b) {
			this.op = new Finder(op);
			this.a = a;
			this.b = b;
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			return a.parse(context) && op.find(context) && b.parse(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class UnaryOperator implements Expression {
		Token expr;
		Finder op;

		UnaryOperator() {
		}

		UnaryOperator(String op, Token expr) {
			this.op = new Finder(op);
			this.expr = expr;
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			return op.find(context) && expr.parse(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Token implements Expression {
		Expression token;

		// can be one of terminal or unary operator
		Token(Expression token) {
			this.token = token;
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			return token.parse(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Terminal implements Expression {
		Finder value;

		Terminal(String value) {
			this.value = new Finder(value);
		}

		public boolean parse(GrammarParser.GrammarContext context) {
			return value.find(context);
		}

		public boolean interpret(GrammarParser.GrammarContext context) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
