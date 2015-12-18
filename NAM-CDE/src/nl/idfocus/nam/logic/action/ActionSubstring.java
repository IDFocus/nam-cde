package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionSubstring implements Action 
{

	private final List<Action> actions;

	public ActionSubstring() 
	{
		this.actions = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) throws ActionException
	{
		if ( this.actions.size() == 0 )
		{
			this.actions.add( action );
		}
		// FIXME substrings of length() or outcome of perform() may be arguments
		else if ( this.actions.size() < 3 )
		{
			if ( action instanceof ActionLiteral )
			{
				try
				{
					Integer.parseInt( action.perform(null)[0] );
					this.actions.add( action );
				}
				catch ( Exception e ) {
					throw new ActionException( "Substring: invalid arguments" );					
				}
			} else if ( action instanceof ActionLength )
				this.actions.add( action );
		}
		else
		{
			throw new ActionException( "Substring: too many arguments" );
		}
	}

	@Override
	public String[] perform( DataResponse attrs ) 
	{
		List<String> values = new ArrayList<String>();
		if ( this.actions.size() == 3 )
		{
			String[] source = this.actions.get(0).perform(attrs);
			String[] start  = this.actions.get(1).perform(attrs);
			String[] end    = this.actions.get(2).perform(attrs);
			if ( start.length == 1 && end.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						values.add( value.substring( Integer.parseInt( start[0] ), Integer.parseInt( end[0] ) ) );
					}
					catch ( NullPointerException e ) {}
					catch ( NumberFormatException e ) {}
				}
			}
			else
			{
				return source;
			}
		}
		else if ( this.actions.size() == 2 )
		{
			String[] source = this.actions.get(0).perform(attrs);
			String[] start  = this.actions.get(1).perform(attrs);
			if ( start.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						values.add( value.substring( Integer.parseInt( start[0] ) ) );
					}
					catch ( NullPointerException e ) {}
					catch ( NumberFormatException e ) {}
				}
			}
			else
			{
				return source;
			}
		}
		return values.toArray(new String[values.size()]);
	}

}
