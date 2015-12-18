package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionReplace implements Action 
{

	private final List<Action> actions;

	public ActionReplace() 
	{
		this.actions = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) throws ActionException
	{
		if ( this.actions.size() < 3 )
			this.actions.add( action );
		else
			throw new ActionException( "Replace: too many arguments" );
	}

	@Override
	public String[] perform( DataResponse attrs ) 
	{
		List<String> values = new ArrayList<String>();
		if ( this.actions.size() == 3 )
		{
			String[] source  = this.actions.get(0).perform(attrs);
			String[] find    = this.actions.get(1).perform(attrs);
			String[] replace = this.actions.get(2).perform(attrs);
			if ( find.length == 1 && replace.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						values.add( value.replace( find[0] , replace[0] ) );
					}
					catch ( NullPointerException e ) {}
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
