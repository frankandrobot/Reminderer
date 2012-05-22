package com.frankandrobot.reminderer.Parser;

import java.util.LinkedList;
import com.frankandrobot.reminderer.Parser.MetaGrammarParser.Context;

/**
 * This implements the Interpreter design pattern. OThe MetaGrammarParser
 * constructs objects of this interface
 * 
 * @author uri
 * 
 */
public interface GrammarInterpreter {

	public boolean parse(Context context);

	public boolean interpret(Context context);

	static MetaGrammarParser g = new MetaGrammarParser();
	static public Finder lBracket = new Finder("\\[");
	static public Finder rBracket = new Finder("\\]");
	static public Finder lParens = new Finder("\\(");
	static public Finder rParens = new Finder("\\)");
	static public Finder whiteSpace = new Finder("[ \t]+");

	public class Task implements GrammarInterpreter {
		String task;
		Commands commands;

		Task(Commands commands) {
			this.commands = commands;
		}

		/*
		 * Gets the task and the commands from the context
		 */
		public boolean parse(Context context) {
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

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class Commands implements GrammarInterpreter {
		LinkedList<Command> commands;

		Commands(LinkedList<Command> commands) {
			this.commands = new LinkedList<Command>();
			for (Command command : commands) {
				this.commands.add(command);
			}
		}

		public boolean parse(Context context) {
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

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Command implements GrammarInterpreter {
		Token token;

		Command(Token token) {
			this.token = token;
		}

		public boolean parse(Context context) {
			return token.parse(context);
		}

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class BinaryOperator implements GrammarInterpreter {
		Token a;
		Token b;
		Finder op;

		BinaryOperator(Token a, String op, Token b) {
			this.op = new Finder(op);
			this.a = a;
			this.b = b;
		}

		public boolean parse(Context context) {
			return a.parse(context) && op.find(context) && b.parse(context);
		}

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}
	}

	public class UnaryOperator implements GrammarInterpreter {
		Token expr;
		Finder op;

		UnaryOperator(String op, Token expr) {
			this.op = new Finder(op);
			this.expr = expr;
		}

		public boolean parse(Context context) {
			return op.find(context) && expr.parse(context);
		}

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Token implements GrammarInterpreter {
		GrammarInterpreter token;

		// can be one of terminal or unary operator
		Token(GrammarInterpreter token) {
			this.token = token;
		}

		public boolean parse(Context context) {
			return token.parse(context);
		}

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}

	}

	public class Terminal implements GrammarInterpreter {
		Finder value;

		Terminal(String value) {
			this.value = new Finder(value);
		}

		public boolean parse(Context context) {
			return value.find(context);
		}

		public boolean interpret(Context context) {
			// TODO Auto-generated method stub
			return false;
		}

	}
}
