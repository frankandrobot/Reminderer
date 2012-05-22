package com.frankandrobot.reminderer.Parser;

import com.frankandrobot.reminderer.Parser.GrammarInterpreter.Token;
import com.frankandrobot.reminderer.Parser.GrammarInterpreter.UnaryOperator;

public class GrammarClasses {

	public class Preposition extends GrammarInterpreter.UnaryOperator {
	
		Preposition(String preposition, GrammarInterpreter.Token expression) {
			super(preposition,expression);
		}
		
	}

//	public static class Day extends GrammarInterpreter.Terminal {
//		
//	}
}
