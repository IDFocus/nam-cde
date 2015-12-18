package nl.idfocus.nam.logic;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nl.idfocus.nam.logic.action.Action;
import nl.idfocus.nam.logic.action.ActionException;
import nl.idfocus.nam.logic.action.ActionParser;
import nl.idfocus.nam.logic.condition.Condition;
import nl.idfocus.nam.logic.condition.ConditionException;
import nl.idfocus.nam.logic.condition.ConditionParser;
import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

public class BusinessRule implements Comparable<BusinessRule>
{
	// Logging
	private static Logger logger = LogFormatter.getConsoleLogger( BusinessRule.class.getName() );
	private final static Level loglevel = Level.INFO;
	private final static Level dbglevel = Level.FINE;
	private final static Level errlevel = Level.SEVERE;
	/**
	 * The mandatory prefix for rule properties. 
	 */
	public  static final String PREFIX = "RULE_";

	private boolean valid  = true;
	private boolean active = true;
	private final String name;
	private final Condition condition;
	private final Action action;

	private DataRequest sourceAttrs;
	private String destAttr;

	public BusinessRule( String name, String rule ) throws BusinessRuleException
	{
		this.name = name;
		this.sourceAttrs = new DataRequest();
		logger.log( loglevel, "Building rule "+name );
		try 
		{
			Map<TokenType,String> parseddata = parse( rule );
			// DEBUG Log the outcome
			for ( TokenType type : parseddata.keySet() )
				logger.log( dbglevel, "Result for "+type.name()+": "+parseddata.get(type) );
			//
			this.destAttr = parseDest( parseddata.get( TokenType.DESTINATION ), false );
			if ( destAttr != null && !destAttr.isEmpty() )
				logger.log( dbglevel, String.format("Destination: ",destAttr) );
			//
			ConditionParser cParser;
			if ( parseddata.containsKey(TokenType.CONDITION) )
			{
				try {
					cParser = new ConditionParser( parseddata.get( TokenType.CONDITION ) );
				} catch (ConditionException e) {
					throw new BusinessRuleException( e );
				}
				logger.log( dbglevel, "Condition parser parsed "+parseddata.get( TokenType.CONDITION ) );
			} else {
				cParser = new ConditionParser();
				logger.log( dbglevel, "Condition parser created empty condition" );
			}
			this.condition = cParser.getCondition();
			logger.log( dbglevel, "Condition retrieved: "+condition.toString() );
			this.sourceAttrs.merge( cParser.getAttributes() );
			logger.log( dbglevel, "Attributes added: "+this.sourceAttrs.numberOfAttributes() );
			//
			ActionParser aParser;
			try {
				aParser = new ActionParser( parseddata.get(TokenType.ACTION) );
			} catch ( ActionException e ) {
				throw new BusinessRuleException( e );
			}
			logger.log( dbglevel, "Action parser parsed "+parseddata.get(TokenType.ACTION)+" into "+aParser.getAction().toString() );
			this.action = aParser.getAction();
			logger.log( dbglevel, "Action retrieved: " + (this.action == null ? "no" : "yes") );
			this.sourceAttrs.merge( aParser.getRequiredData() );
			logger.log( dbglevel, "Attributes added: "+this.sourceAttrs.numberOfAttributes() );
			//
			if ( parseddata.containsKey( TokenType.DISABLED ) )
			{
				this.active = ! Boolean.parseBoolean( parseddata.get(TokenType.DISABLED) );
			}
		} 
		catch (NullPointerException e) 
		{
			logger.log( errlevel, "Error: "+e.toString() );
			e.printStackTrace();
			// Invalid marking
			this.valid = false;
			// Probably some logging is in place here?
			throw new BusinessRuleException( "Business Rule: NPE" );
		}
		logger.log( loglevel, this.toString() );
	}

	public boolean isActive()
	{
		return this.active;
	}

	public boolean isValid()
	{
		return this.valid;
	}

	public String getName()
	{
		return this.name;
	}

	public String getDestination()
	{
		return destAttr;
	}
	
	public String[] getResult( DataResponse data )
	{
		logger.log( loglevel, this.name+": executing action" );
		String[] result = this.action.perform( data );
		logger.log( loglevel, "Result: "+Arrays.toString( result ) );
		return result;
	}

	public DataRequest requires()
	{
		if ( this.valid )
			return this.sourceAttrs;
		else
			return null;
	}

	public boolean applies( DataResponse data )
	{
		if ( !this.valid || !this.active )
			return false;
		// TODO check if all needed attributes are present?
		logger.log( loglevel, this.name+": evaluating condition" );
		boolean result =  this.condition.evaluate( data );
		logger.log( loglevel, "Result: "+result );
		return result;
		
	}

	private String parseDest( String data, boolean mandatory ) throws BusinessRuleException 
	{
		if ( data != null && !data.isEmpty() )
		{
			return data;
		}
		else if ( mandatory )
		{
			throw new BusinessRuleException( TokenType.DESTINATION.name() + " is mandatory" );
		}
		else 
		{
			return "";
		}		
	}

	@Override
	public int compareTo( BusinessRule another ) 
	{
		return this.getName().compareTo( another.getName() );
	};

	@Override
	public String toString() 
	{
		return String.format("Rule %10s: condition %20s, action %20s, destination %10s", this.name, this.condition.toString(), this.action.toString(), this.destAttr );
	}

	private Map<TokenType,String> parse( String rule ) throws BusinessRuleException
	{
		Token previousToken = null;
		TokenType currentToken = null;
		boolean inContent = false;
		StringBuilder currentContent = new StringBuilder();
		Map<TokenType,String> parseddata = new HashMap<TokenType, String>();
		List<Token> tokens = tokenize( rule );
		for ( Token token : tokens )
		{
			logger.log( dbglevel, "Processing token: "+token );
			if      ( token.type.equals(TokenType.CONDITION) ||
					  token.type.equals(TokenType.ACTION) || 
					  token.type.equals(TokenType.DESTINATION) ||
					  token.type.equals(TokenType.DISABLED) )
			{
				if ( currentToken != null )
				{
					this.valid = false;
					logger.log( dbglevel, "Syntax error discovered at position "+(previousToken != null ? previousToken.endPos : 0 )+" near '"+currentContent.toString()+"'" );
					// try to make the best of it
					inContent = false;
					parseddata.put(currentToken, currentContent.toString());
					currentContent = new StringBuilder();
				}
				currentToken = token.type;
			}
			else if ( token.type.equals(TokenType.QUOTE) )
			{
				inContent = inContent ? false : true;
			}
			else if ( token.type.equals(TokenType.SPACE) )
			{
				if ( inContent )
					currentContent.append( token.value );
			}
			else if ( token.type.equals(TokenType.SEPARATOR) )
			{
				if ( inContent )
					currentContent.append( token.value );
				else if ( currentContent != null )
				{
					// FIXME not the best choice?
					parseddata.put( currentToken, currentContent.toString() );
					currentContent = new StringBuilder();
					currentToken = null;
				}
			}
			else if ( token.type.equals(TokenType.CONTENT) )
			{
				currentContent.append( token.value );
				if ( ! inContent )
				{
					this.valid = false;
					logger.log( errlevel, "Syntax error discovered at position "+(previousToken != null ? previousToken.endPos : 0 )+" near "+currentContent.toString() );
					// try to make the best of it
					inContent = true;
				}
			}
			previousToken = token;
		}
		// The last part is probably not ended by a separator
		if ( currentToken != null )
		{
			parseddata.put(currentToken, currentContent.toString() );
		}
		return parseddata;
	}

	private List<Token> tokenize( String rule ) throws BusinessRuleException
	{
		List<Token> tokens = new ArrayList<Token>();
		int pos = 0;
		final int end = rule.length();
		Matcher m = Pattern.compile(".").matcher( rule );
		// This will ignore newlines in the rules (Do I need this? Can someone copy-paste newlines in AM?)
		m.useAnchoringBounds(false);
		// Advance through the rule characters and detect any tokens
		while (pos < end)
		{
			int cur = pos;
			m.region(pos, end);
			for ( TokenType type : TokenType.values() )
			{
				if ( m.usePattern( type.getRule() ).lookingAt() )
				{
					tokens.add( new Token( type , m.start() , m.end() , rule.substring( m.start(), m.end() ) ) );
					pos = m.end();
					break;
				}
			}
			// if no tokens could be matched, this would loop forever. Exit. 
			if ( cur == pos )
				throw new BusinessRuleException( String.format("Syntax error at position %s: no tokens matched.", pos ) );
		}
		return tokens;
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
			return String.format( "Token %2d [%2d, %2d, %11s, %12s]", type.type, startPos, endPos, type.name, value );
		}		
	}

	private enum TokenType
	{
		ACTION      ("ACTION",      "action\\s*:",       1),
		CONDITION   ("CONDITION",   "condition\\s*:",    2),
		DESTINATION ("DESTINATION", "destination\\s*:",  3),
		DISABLED    ("STATUS",      "disabled\\s*:",     4),
		SPACE       ("SPACE",       "\\s+",              5),
		QUOTE       ("QUOTE",       "[\"]{1}",           6),
		SEPARATOR   ("SEPARATOR",   ",",                 7),
		ESCAPE      ("ESCAPE",      "\\\\",              8),
		CONTENT     ("CONTENT",     "[^\\\\^\"^\\s^,]+", 9),
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
}
