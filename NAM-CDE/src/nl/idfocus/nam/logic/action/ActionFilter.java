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
	private final List<Pattern> patterns;
	
	public ActionFilter() 
	{
		logger.log( loglevel, "Filter: initializing" );
		this.action = null;
		this.patterns = new ArrayList<Pattern>();
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else if ( action instanceof ActionLiteral )
		{
			String[] res = action.perform( new DataResponse() );
			if ( res.length > 0 )
				addRegex( res[0] );
		}
		else
			throw new ActionException("Filter: only one generic action and one or more literals are expected");
	}

	private void addRegex(String regex) throws ActionException
	{
		logger.log(loglevel, "Regex: adding regex: "+regex );
		try
		{
			this.patterns.add( Pattern.compile(regex) );
		} catch (PatternSyntaxException e) {
			throw new ActionException( String.format( "Filter: %s: %s.", e.getPattern(), e.getMessage() ) );
		}
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		logger.log(loglevel, "Filter: starting perform()");
		List<String> result = new ArrayList<String>();
		if ( data != null )
		{
			String[] values = this.action.perform(data);
			for ( String value : values )
			{
//				logger.log(loglevel, "Filter: evaluating "+value);				
				boolean matches = false;
				for ( Pattern pat : this.patterns )
				{
					if ( pat.matcher(value).find() )
					{
						logger.log(loglevel, String.format("Filter: %s matched %s.", value, pat.toString() ) );				
						matches = true;
					}
				}
				if ( matches )
					result.add(value);
			}
		}
		logger.log(loglevel, "Filter: returning: "+result.size()+" values." );
		return result.toArray( new String[result.size()] );
	}

}
