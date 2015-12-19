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
package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.util.LogFormatter;

public class ActionParser 
{
	private static Logger logger = LogFormatter.getConsoleLogger( ActionParser.class.getName() );
	private final Level dbglevel = Level.FINE;
	private final Level errlevel = Level.SEVERE;

	private final Action action;
	private final DataRequest requiredData;

	public ActionParser( String action ) throws ActionException
	{
		logger.log( dbglevel, "Parsing action string: "+action );
		this.requiredData = new DataRequest();
		this.action = parse( action );
	}

	public Action getAction() 
	{
		return this.action;
	}

	public DataRequest getRequiredData()
	{
		return this.requiredData;
	}

	private Action parse( String action ) throws ActionException
	{
		List<Token> tokens = this.tokenize( action );
		// Traverse the list of tokens, convert to actions and stack them to form the hierarchy
		LinkedList<Action> stack = new LinkedList<Action>();
		Action result = null;
		boolean escapeNext  = false;
		boolean inLiteral   = false;
		boolean newDnLookup = false;
		String dnLookupAttr = "";
		StringBuilder currentLiteral = new StringBuilder();
		for ( Token token : tokens )
		{
			logger.log( dbglevel, "Processing token: "+token );
			if ( escapeNext )
			{
				logger.log( dbglevel, "Found "+token.type.name()+" but in ESCAPE sequence" );
				// Add to expression of current condition
				if ( !stack.isEmpty() )
				{
					if ( inLiteral )
					{
						currentLiteral.append( token.value );
					}
					else
					{
						// TODO seems to be unnecessary since attribute names cannot contain special characters
						stack.peek().addAction( new ActionAttribute( token.value ) );
						this.requiredData.addAttribute( token.value );
					}
				}
				else
				{
					logger.log( errlevel, String.format("Syntax error: escape char detected at position %s but no actions parsed yet", token.startPos) );
					throw new ActionException( String.format("Syntax error at position %s: escape character outside of action",token.startPos) );
				}
				// Set escapeNext to false
				escapeNext = false;
			}
			else if ( token.type == TokenType.OPEN )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Add to expression for now
				if ( !stack.isEmpty() )
				{
					if ( inLiteral )
						currentLiteral.append( token.value );
					// FIXME throw error on else!
				}
				else
				{
					// Start 
				}
			}
			else if ( token.type == TokenType.DNLOOKUP )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				stack.push( new ActionDnLookup() );
				// Register DN type found
				newDnLookup = true;
			}
			else if ( token.type == TokenType.CONCAT )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionConcat() );
			}
			else if ( token.type == TokenType.LOWER )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionLower() );
			}
			else if ( token.type == TokenType.UPPER )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionUpper() );
			}
			else if ( token.type == TokenType.JOIN )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionJoin() );
			}
			else if ( token.type == TokenType.SPLIT )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionSplit() );
			}
			else if ( token.type == TokenType.LENGTH )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionLength() );
			}
			else if ( token.type == TokenType.REPLACE )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionReplace() );
			}
			else if ( token.type == TokenType.SUBSTRING )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionSubstring() );
			}
			else if ( token.type == TokenType.SUBSTRAFT )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionSubstringAfter() );
			}
			else if ( token.type == TokenType.SUBSTRBEF )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionSubstringBefore() );
			}
			else if ( token.type == TokenType.TRIM )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionTrim() );
			}
			else if ( token.type == TokenType.FILTER )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Create new action for this and push()
				stack.push( new ActionFilter() );
			}
			else if ( token.type == TokenType.CLOSE )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// pop() current condition from stack and add to parent condition using peek()
				if ( stack.size() > 1 )
				{
					Action act = stack.pop();
					stack.peek().addAction( act );
				}
				else if ( stack.size() == 1 )
				{
					logger.log( dbglevel, String.format("Stack complete at end position %s of length %s",token.endPos, action.length()) );
					// this is our return moment, should check for additional tokens
					if ( token.endPos < action.length() )
					{
						// We have trailing characters, find out which tokentypes
						int pos = tokens.lastIndexOf(token);
						List<Token> trailing = tokens.subList( pos, tokens.size()-1 );
						for ( Token trail : trailing )
						{
							if ( trail.type != TokenType.SPACE )
							{
								throw new ActionException(String.format("Action: Trailing characters found after close at position %s",token.endPos) );
							}
						}
					}
					if ( result == null )
						result = stack.pop();
					else
						throw new ActionException(String.format("Action: Duplicate data at position %s",token.endPos) );
				}
				else
				{
					// TODO else generate error 
				}
			}
			else if ( token.type == TokenType.SEPARATOR )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Mark end of current <attribute-or-literal> ?
				// TODO Maybe throw error if encountered unescaped in a literal?
				if ( inLiteral )
					currentLiteral.append( token.value );
			}
			else if ( token.type == TokenType.VALUE || token.type == TokenType.CARET )
			{
				// FIXME where does CARET fit in? Literal only?
				logger.log( dbglevel, "Found "+token.type.name() );
				// Add to expression of current condition
				if ( !stack.isEmpty() )
				{
					if ( inLiteral )
					{
						logger.log( dbglevel, "Adding literal: "+token.value );
						currentLiteral.append( token.value );
					}
					else if ( stack.peek().getClass().equals( ActionDnLookup.class ) )
					{
						if ( newDnLookup )
						{
							logger.log( dbglevel, "Adding DN attribute name: "+token.value );
							// FIXME this assumes the entire attribute name has been parsed into VALUE 
							stack.peek().addAction( new ActionAttribute( token.value ) );
							this.requiredData.addAttribute( token.value );
							newDnLookup = false;
							dnLookupAttr = token.value;
						}
						else
						{
							logger.log( dbglevel, "Adding lookup attribute name: "+token.value );
							// FIXME this assumes the entire attribute name has been parsed into VALUE. 
							// 		 Also, hackish to create a literal without quotes
							stack.peek().addAction( new ActionLiteral( token.value ) );
							this.requiredData.addAttribute( dnLookupAttr, token.value );
						}
					}
					else
					{
						logger.log( dbglevel, "Adding attribute name: "+token.value );
						// FIXME this assumes the entire attribute name has been parsed into VALUE
						stack.peek().addAction( new ActionAttribute( token.value ) );
						this.requiredData.addAttribute( token.value );
					}
				}
			}
			else if ( token.type == TokenType.ESC )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// Set escapeNext to true (Note that an escape will escape a whole match instead of one char)
				escapeNext = true;
			}
			else if ( token.type == TokenType.QUOTE )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				// toggle inLiteral
				if ( inLiteral )
				{
					inLiteral = false;
					stack.peek().addAction( new ActionLiteral( currentLiteral.toString() ) );
				}
				else
				{
					inLiteral = true;
					currentLiteral = new StringBuilder();
				}
			}
			else if ( token.type == TokenType.SPACE )
			{
				logger.log( dbglevel, "Found "+token.type.name() );
				if ( inLiteral )
				{
					currentLiteral.append( token.value );
				}
			}

		}
		// The stack should now contain only one condition
		if ( stack.size() > 0 )
		{
			// throw exception (missing closing bracket)
			throw new ActionException( "Action: Missing closing bracket, parsing incomplete" );
		}
		if ( inLiteral )
		{
			// throw exception (missing closing quote)
			throw new ActionException( "Action: Missing closing quote, parsing incomplete" );
		}
		return result;
	}

	private List<Token> tokenize( String action ) throws ActionException
	{
		List<Token> tokens = new ArrayList<Token>();
		int pos = 0;
		final int end = action.length();
		Matcher m = Pattern.compile(".").matcher( action );
		// This will ignore newlines in the rules (Do I need this? Can someone copy-paste newlines in AM?)
		m.useAnchoringBounds(false);
		while (pos < end)
		{
			int cur = pos;
			m.region(pos, end);
			for (TokenType type : TokenType.values() )
			{
				if ( m.usePattern( type.getRule() ).lookingAt() )
				{
					tokens.add( new Token( type , m.start() , m.end() , action.substring( m.start(), m.end() ) ) );
					pos = m.end();
					break;
				}
			}
			// if no tokens could be matched, this would loop forever. Exit. 
			if ( cur == pos )
				throw new ActionException( String.format("Action: Syntax error at position %s: no tokens matched.", pos ) );
		}
		return tokens;
	}

	private enum TokenType
	{
		CONCAT   ("CONCAT",         "concat\\s*\\(",    1),
		SUBSTRING("SUBSTRING",      "substring\\s*\\(", 2),
		SUBSTRAFT("SUBSTRINGAFTER", "substring-after\\s*\\(",  3),
		SUBSTRBEF("SUBSTRINGBEFORE","substring-before\\s*\\(", 4),
		TRIM     ("TRIM",     "trim\\s*\\(",      5),
		REPLACE  ("REPLACE",  "replace\\s*\\(",   6),
		LOWER    ("LOWER",    "lower\\s*\\(",     7),
		UPPER    ("UPPER",    "upper\\s*\\(",     8),
		DNLOOKUP ("DN",       "dn\\s*\\(",        9),
		JOIN     ("JOIN",     "join\\s*\\(",     10),
		SPLIT    ("SPLIT",    "split\\s*\\(",    11),
		LENGTH   ("LENGTH",   "length\\s*\\(",   12),
		FILTER   ("FILTER",   "filter\\s*\\(",   13),
		OPEN     ("OPEN",     "\\(",                  20),
		QUOTE    ("QUOTE",    "'",                    21),
		VALUE    ("VALUE",    "[^\\\\^\\s^(^)^,^']+", 22),
		CARET    ("CARET",    "\\^",                  23),
		CLOSE    ("CLOSE",    "\\)",                  30),
		SEPARATOR("SEPARATOR",",",                    31),
		SPACE    ("SPACE",    "\\s+",                 32),
		ESC      ("ESC",      "\\\\",                 33),
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
