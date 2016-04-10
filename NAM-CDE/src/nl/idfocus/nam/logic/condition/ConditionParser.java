/**
 * Copyright 2015 IDFocus B.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package nl.idfocus.nam.logic.condition;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.idfocus.nam.logic.action.Action;
import nl.idfocus.nam.logic.action.ActionAttribute;
import nl.idfocus.nam.logic.action.ActionDnLookup;
import nl.idfocus.nam.logic.action.ActionException;
import nl.idfocus.nam.logic.action.ActionLiteral;
import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.util.LogFormatter;

public class ConditionParser 
{
	private static Logger logger = LogFormatter.getConsoleLogger( ConditionParser.class.getName() );
	private final Level dbglevel = Level.FINE;
	private final Level errlevel = Level.SEVERE;
	private final DataRequest attributes;
	private final Condition condition;

	public ConditionParser()
	{
		this.attributes = new DataRequest();
		this.condition = new ConditionEmpty();
	}

	public ConditionParser( String condition ) throws ConditionException
	{
    	/* 
    	 * RULE MARKUP
    	 * 
    	 * 		destination: "attribute",						--> free choice
    	 * 		condition: "not( description ~= 'test' )",	--> nesting, operators
    	 * 		action: "substring( description, '0', '5' )"			--> nesting!
    	 * 
    	 * condition must match: /(and|or|if)\(.+\)/
    	 * 	if the contents also match, parse recursively until it does not match anymore > then it is a Term to execute
    	 */
		this.attributes = new DataRequest();
		this.condition = parse( condition );
	}

	public Condition getCondition() 
	{
		return this.condition;
	}

	public DataRequest getAttributes()
	{
		return this.attributes;
	}

	private Condition parse( String condition ) throws ConditionException
	{
		List<Token> tokens = this.tokenize( condition );
		logger.log( dbglevel, String.format("Parsed %s token%s", tokens.size(), (tokens.size() > 1 ? "s" : "" ) ) );
		// Traverse the list of tokens, convert to conditions and stack them to form the hierarchy
		LinkedList<Condition> stack = new LinkedList<Condition>();
		Condition result     = null;
		boolean escapeNext   = false;
		boolean inExpression = false;
		boolean inLiteral    = false;
		boolean inDnLookup   = false;
		String dnLookupAttr  = "";
		boolean leftDnLookup = true;
		StringBuilder currentLiteral = new StringBuilder();
		for ( Token token : tokens )
		{
			logger.log( dbglevel, String.format("Stack: %s. Processing %s", stack.size(),inExpression,token.toString() ) );
			if ( escapeNext )
			{
				// Add to expression of current condition
				if ( !stack.isEmpty() && inLiteral )
				{
					currentLiteral.append( token.value );
				}
				else
				{
					logger.log( errlevel, String.format("Syntax error: escape char detected at position %s but no conditions parsed yet", token.startPos) );
					throw new ConditionException( String.format("Syntax error at position %s: escape character outside of condition",token.startPos) );
				}
				// Set escapeNext to false
				escapeNext = false;
			}
			else if ( token.type == TokenType.OPEN )
			{
				// Create new expression condition for this and push()
				if ( stack.isEmpty() || !stack.peek().getClass().equals( ConditionExpression.class ) )
					stack.push( new ConditionExpression() );
			}
			else if ( token.type == TokenType.IF )
			{
				// Create new and condition for this and push()
				stack.push( new ConditionExpression() );
			}
			else if ( token.type == TokenType.AND )
			{
				// Create new and condition for this and push()
				stack.push( new ConditionAnd() );
			}
			else if ( token.type == TokenType.OR )
			{
				// Create new or condition for this and push()
				stack.push( new ConditionOr() );
			}
			else if ( token.type == TokenType.NOT )
			{
				// Create new not condition for this and push()
				stack.push( new ConditionNot() );
			}
			else if ( token.type == TokenType.REGEX )
			{
				// Create new regex condition for this and push()
				stack.push( new ConditionRegex() );
			}
			else if ( token.type == TokenType.DNLOOKUP )
			{
				inDnLookup = true;
			}
			else if ( token.type == TokenType.LENGTH )
			{
				// TODO	
			}
			else if ( token.type == TokenType.CLOSE )
			{
				// Double pop when in implicit expression
				if ( inExpression && stack.size() > 1 )
				{
					logger.log(dbglevel, "Performing double pop()");
					inExpression = false;
					Condition cnd = stack.pop();
					stack.peek().addCondition(cnd);
				}
				// pop() current condition from stack and add to parent condition using peek()
				if ( inDnLookup )
				{
					inDnLookup = false;
				}
				else if ( stack.size() > 1 )
				{
					Condition cnd = stack.pop();
					stack.peek().addCondition(cnd);
				}
				else if ( stack.size() == 1 )
				{
					// TODO this fails to fail when a quote has not been closed (!)
					logger.log( dbglevel, String.format("Stack complete at end position %s of length %s",token.endPos, condition.length()) );
					// this is our return moment, should check for additional tokens
					if ( token.endPos < condition.length() )
					{
						// We have trailing characters, find out which tokentypes
						int pos = tokens.lastIndexOf(token);
						List<Token> trailing = tokens.subList( pos, tokens.size()-1 );
						for ( Token trail : trailing )
						{
							if ( trail.type != TokenType.SPACE )
							{
								throw new ConditionException(String.format("Trailing characters found after close at position %s",token.endPos) );
							}
						}
					}
					if ( result == null )
						result = stack.pop();
					else
						throw new ConditionException(String.format("Duplicate data at position %s",token.endPos) );
				}
				else
				{
					// else generate error? Should never reach this code
					throw new ConditionException(String.format("Result already returned at position %s",token.endPos) );
				}
			}
			else if ( token.type == TokenType.EQUALITY )
			{
				// Add to expression of current condition and toggle left/right
				if ( !stack.isEmpty() && stack.peek().getClass().equals( ConditionExpression.class ) )
				{
					((ConditionExpression) stack.peek()).addEquality( token.value );
				}
				else
				{
					logger.log( errlevel, String.format("Syntax error: unexpected equality found at position %s", token.startPos) );
					throw new ConditionException(String.format("Unexpected equality found at position %s", token.startPos) );
				}
			}
			else if ( token.type == TokenType.VALUE || token.type == TokenType.CARET )
			{
				// FIXME check when CARET is applicable (only in literals?)
				logger.log(dbglevel, String.format("Processing %s token", token.type.name ) );
				// Add expression if not in expression or regex already
				if ( !stack.isEmpty() && !stack.peek().getClass().equals(ConditionExpression.class) && !stack.peek().getClass().equals(ConditionRegex.class) )
				{
					inExpression = true;
					stack.push( new ConditionExpression() );
				}
				// Add to expression of current condition
				if ( stack.isEmpty() )
				{
					// Error, missing condition or open
					throw new ConditionException(String.format("Missing condition or open bracket at position %s", token.startPos) );
				}
				else if ( inLiteral )
				{
					currentLiteral.append( token.value );
				}
				else if ( inDnLookup )
				{
					if ( leftDnLookup )
					{
						this.attributes.addAttribute( token.value );
						dnLookupAttr = token.value;
						leftDnLookup = false;
					}
					else
					{
						this.attributes.addAttribute( dnLookupAttr, token.value );
						// Add actions
						Action dnlookup = new ActionDnLookup();
						try {
							dnlookup.addAction( new ActionAttribute(dnLookupAttr) );
							dnlookup.addAction( new ActionLiteral( token.value ) );
						} catch (ActionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						if ( stack.peek().getClass().equals(ConditionExpression.class) )
							((ConditionExpression) stack.peek()).addAction( dnlookup );
						else if ( stack.peek().getClass().equals(ConditionRegex.class) )
							((ConditionRegex) stack.peek()).addAction(dnlookup);
					}
				}
				else
				{
					this.attributes.addAttribute( token.value );
					logger.log( dbglevel, String.format( "Adding attribute %s to %s", token.value, stack.peek().getClass().getSimpleName() ) );
					if ( stack.peek().getClass().equals(ConditionExpression.class) )
						((ConditionExpression) stack.peek()).addAction( new ActionAttribute( token.value ) );
					else if ( stack.peek().getClass().equals(ConditionRegex.class) )
						((ConditionRegex) stack.peek()).addAction( new ActionAttribute( token.value ) );
				}
			}
			else if ( token.type == TokenType.QUOTE )
			{
				// Add to expression of current condition
				if ( !stack.isEmpty() )
				{
					if ( inLiteral )
					{
						inLiteral = false;
						logger.log( dbglevel, String.format("Adding literal %s to %s", currentLiteral.toString(), stack.peek().getClass().getSimpleName() ));
						if ( stack.peek().getClass().equals(ConditionExpression.class) )
							((ConditionExpression) stack.peek()).addAction( new ActionLiteral( currentLiteral.toString() ) );
						else if ( stack.peek().getClass().equals(ConditionRegex.class) )
							((ConditionRegex) stack.peek()).addRegex( currentLiteral.toString() );
					}
					else
					{
						inLiteral = true;
						currentLiteral = new StringBuilder();
						// Add expression if not in expression or regex already
						if ( !stack.peek().getClass().equals(ConditionExpression.class) && !stack.peek().getClass().equals(ConditionRegex.class) )
						{
							inExpression = true;
							stack.push( new ConditionExpression() );
						}
					}
				}
				else
				{
					logger.log( errlevel, "Cannot add literal on empty stack" );
					// TODO throw error for empty stack or not in expression
				}
			}
			else if ( token.type == TokenType.ESC )
			{
				// Set escapeNext to true (Note that an escape will escape a whole match instead of one char)
				escapeNext = true;
			}
		}
		// The stack should now contain no more conditions
		if ( stack.size() > 0 )
		{
			// throw exception (missing closing bracket)
			throw new ConditionException( "Missing closing bracket, parsing incomplete" );
		}
		if ( inLiteral )
		{
			// throw exception (missing closing quote)
			throw new ConditionException( "Missing closing quote, parsing incomplete" );
		}
		return result;
	}

	private List<Token> tokenize( String condition ) throws ConditionException
	{
//		logger.log( dbglevel, "tokenize(): parsing "+condition);
		List<Token> tokens = new ArrayList<Token>();
		int pos = 0;
		final int end = condition.length();
		Matcher m = Pattern.compile(".").matcher( condition );
		// This will ignore newlines in the rules (Do I need this? Can someone copy-paste newlines in AM?)
		m.useAnchoringBounds(false);
		while (pos < end)
		{
			int cur = pos;
			m.region(pos, end);
			for (TokenType type : TokenType.values() )
			{
//				logger.log(dbglevel, "tokenize(): matching "+type.name+" at position "+pos );
				if ( m.usePattern( type.getRule() ).lookingAt() )
				{
					tokens.add( new Token( type , m.start() , m.end() , condition.substring( m.start(), m.end() ) ) );
					pos = m.end();
					break;
				}
			}
			// if no tokens could be matched, this would loop forever. Exit. 
			if ( cur == pos )
				throw new ConditionException( String.format("Syntax error at condition position %s: no tokens matched.", pos ) );
		}
		return tokens;
	}

	private enum TokenType
	{
		AND      ("AND",       "and\\s*\\(",  1),
		OR       ("OR",        "or\\s*\\(",   2),
		NOT      ("NOT",       "not\\s*\\(",  3),
		IF       ("IF",        "if\\s*\\(",   4),
		LENGTH	 ("LENGTH",    "length\\s*\\(",5),
		DNLOOKUP ("DN",        "dn\\s*\\(",   6),
		REGEX	 ("REGEX",     "regex\\s*\\(",7),
		OPEN     ("OPEN",      "\\(",         20),
		EQUALITY ("EQUALITY",  "[<>=~]{1,2}", 21),
		QUOTE    ("QUOTE",     "'",           22),
		VALUE    ("VALUE",     "[^\\\\^\\s^(^)^'^=^,^<^>^~]+", 23),
		CARET	 ("CARET",	   "\\^",  24),
		CLOSE    ("CLOSE",     "\\)",  25),
		SEPARATOR("SEPARATOR", ",",    26),
		SPACE    ("SPACE",     "\\s+", 27),
		ESC      ("ESC",       "\\\\", 28),
//		VALUE    ("VALUE",     "[^,^\\s]+", 23),
		;

		public final String name;
		public final String rule;
		public final int type;

		TokenType( String name, String rule, int type )
		{
			this.name = name;
			this.rule = rule;
			this.type = type;
		}

		Pattern getRule()
		{
			return Pattern.compile( this.rule );
		}
	}

	private class Token
	{
		final TokenType type;
		final int startPos;
		final int endPos;
		final String value;

		Token( TokenType type, int startPos, int endPos, String value )
		{
			this.type = type;
			this.startPos = startPos;
			this.endPos = endPos;
			this.value = value;
		}

		// Just for debugging
		@Override
		public String toString()
		{
			return String.format( "Token %2d [%2d, %2d, %9s, %12s]", type.type, startPos, endPos, type.name, value );
		}
	}

}
