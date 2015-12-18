package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

public class ActionDnLookup implements Action 
{
	private static final Logger logger = LogFormatter.getConsoleLogger( ActionDnLookup.class.getName() );
	private final Level loglevel = Level.FINE;
	private final Level dbglevel = Level.FINEST;

	private Action dnAttribute = null;
	private Action dnLookup = null;

	@Override
	public void addAction(Action action) throws ActionException 
	{
		// TODO think this through - can we allow any action as the dnLookup?
		if ( ! ( action instanceof ActionAttribute || action instanceof ActionLiteral ) )
			throw new ActionException( "DnLookup: invalid argument "+action );
		if ( dnAttribute == null )
			dnAttribute = action;
		else if ( dnLookup == null )
			dnLookup = action;
		else
			throw new ActionException( "DnLookup: too many arguments" );
	}

	@Override
	public String[] perform( DataResponse data ) 
	{
		List<String> values = new ArrayList<String>();
		String[] dnValues = dnAttribute.perform(data);
		// TODO If lookup is more than a literal, this goes wrong
		// when the requested secondary attribute is also present in the primary set
		String[] dnLookupAttrs = dnLookup.perform(data);
		logger.log( loglevel, "DnLookup: finding "+Arrays.toString(dnLookupAttrs)+" in "+Arrays.toString(dnValues) );
		for ( String dnValue : dnValues )
		{
			logger.log( dbglevel, "DnLookup: handling "+dnValue );
			for ( String dnLookupAttr : dnLookupAttrs )
			{
				logger.log( dbglevel, "DnLookup: getting "+dnLookupAttr );
				Attribute attr =  data.getRelatedAttribute( dnValue, dnLookupAttr );
				logger.log( dbglevel, "DnLookup: got "+dnLookupAttr );
				try
				{
					NamingEnumeration<?> rawvalues = attr.getAll();
					while( rawvalues.hasMore() )
						values.add( (String)rawvalues.next() );
				}
				catch ( NamingException e ) {}
				catch ( ClassCastException e ) {}
				catch ( NullPointerException e ) {}
			}
		}
		return values.toArray(new String[values.size()]);
	}

	@Override
	public String toString()
	{
		return "DnLookup: ["+dnAttribute+" , "+dnLookup+"]";
	}
}
