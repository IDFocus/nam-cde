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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nl.idfocus.nam.logic.action.Action;
import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

public class ConditionRegex implements Condition 
{
	private static final Logger logger = LogFormatter.getConsoleLogger( ConditionRegex.class.getName() );
	private final Level loglevel = Level.FINE;

	private Action action;
	private final List<Pattern> patterns; 

	public ConditionRegex()
	{
		logger.log(loglevel, "Regex: initializing" );
		patterns = new ArrayList<Pattern>();
		action = null;
	}

	@Override
	public void addCondition(Condition condition) throws ConditionException 
	{
		// Cannot nest other conditions
		throw new ConditionException( "Regex: cannot nest other conditions" );
	}

	public void addRegex(String regex) throws ConditionException
	{
		logger.log(loglevel, "Regex: adding regex: "+regex );
		try
		{
			this.patterns.add( Pattern.compile(regex) );
		} catch (PatternSyntaxException e) {
			throw new ConditionException( String.format( "Regex: %s: %s.", e.getPattern(), e.getMessage() ) );
		}
	}

	public void addAction( Action action ) throws ConditionException
	{
		logger.log(loglevel, "Regex: adding action");
		if ( this.action == null )
		{
			this.action = action;
		}
		else
		{
			throw new ConditionException( "Regex: only one action may be specified" );
		}
	}

	@Override
	public boolean evaluate(DataResponse data) 
	{
		logger.log(loglevel, "Regex: starting evaluate()");
		boolean result = false;
		if ( data != null )
		{
			String[] values = this.action.perform(data);
			for ( String value : values )
			{
				for ( Pattern pat : this.patterns )
				{
					result = pat.matcher(value).matches();
				}
			}
		}
		logger.log(loglevel, "Regex: returning: "+result );
		return result;
	}

	@Override
	public String toString() 
	{
		StringBuilder msg = new StringBuilder();
		msg.append("'Regex' condition contains ");
		if( this.action != null )
			msg.append( "action: " )
			   .append( this.action.toString() );
		if ( this.patterns.size() > 0 )
			msg.append( ", " )
			   .append( patterns.size() )
			   .append( " regexes");
		return msg.toString();
	}
}
