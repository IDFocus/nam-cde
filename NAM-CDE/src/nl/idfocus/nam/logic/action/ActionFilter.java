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
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

public class ActionFilter implements Action 
{
	private static final Logger logger = LogFormatter.getConsoleLogger( ActionFilter.class.getName() );
	private final Level loglevel = Level.FINE;

	private Action action;
	private final List<Action> patterns;
	
	public ActionFilter() 
	{
		logger.log( loglevel, "Filter: initializing" );
		this.action = null;
		this.patterns = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else if (!( action instanceof ActionCount || action instanceof ActionLength ))
		{
			patterns.add(action);
		}
		else
			throw new ActionException("Filter: only one generic action and one or more literals are expected");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		logger.log(loglevel, "Filter: starting perform()");
		List<String> result = new ArrayList<String>();
		if ( data != null )
		{
			List<Pattern> patterns = createPatterns( data );
			String[] values = this.action.perform(data);
			for ( String value : values )
			{
				if ( matchesPatternList( value, patterns ) )
					result.add(value);
			}
		}
		logger.log(loglevel, "Filter: returning: "+result.size()+" values." );
		return result.toArray( new String[result.size()] );
	}

	private boolean matchesPatternList( String value, List<Pattern> patterns)
	{
		boolean matches = false;
		for ( Pattern pat : patterns )
		{
			if ( pat.matcher(value).find() )
			{
				logger.log(loglevel, String.format("Filter: %s matched %s.", value, pat.toString() ) );				
				matches = true;
			}
		}
		return matches;
	}

	private List<Pattern> createPatterns(DataResponse data)
	{
		List<Pattern> results = new ArrayList<Pattern>();
		for(Action pattern : patterns)
		{
			String[] values = pattern.perform(data);
			results.addAll( extractPatternValues( values, data ) );
		}
		return results;
	}

	private List<Pattern> extractPatternValues(String[] values, DataResponse data) 
	{
		List<Pattern> results = new ArrayList<Pattern>();		
		for( String value : values )
		{
			try {
				Pattern pattern = createRegex(value);
				results.add(pattern);
			} catch (ActionException e) {
				logger.log(loglevel, "Filter: error creating pattern for "+value+": "+e.getMessage() );
			}
		}
		return results;
	}

	private Pattern createRegex(String regex) throws ActionException
	{
		logger.log(loglevel, "Filter: creating regex: "+regex );
		try
		{
			return Pattern.compile(regex);
		} catch (PatternSyntaxException e) {
			throw new ActionException( String.format( "Filter: %s: %s.", e.getPattern(), e.getMessage() ) );
		}
	}

}
