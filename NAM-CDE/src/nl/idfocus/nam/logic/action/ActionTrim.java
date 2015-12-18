package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionTrim implements Action 
{

	private Action action;

	public ActionTrim() 
	{
		this.action = null;
	}
	
	@Override
	public void addAction(Action action) throws ActionException
	{
		if ( this.action == null )
			this.action = action;
		else
			throw new ActionException( "Trim: too many arguments" );
	}

	@Override
	public String[] perform(DataResponse attrs) 
	{
		List<String> values = new ArrayList<String>();
		for ( String value : this.action.perform( attrs ) )
			values.add( value.trim() );
		return values.toArray(new String[values.size()]);
	}

}
