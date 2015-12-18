package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;

public class ActionLength implements Action 
{
	private Action action;
	
	public ActionLength() 
	{
		this.action = null;
	}
	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else
			throw new ActionException("Length: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		List<String> values = new ArrayList<String>();		
		for( String value : this.action.perform(data) )
			values.add( Integer.toString( value.length() ) );
		return values.toArray(new String[values.size()]);
	}

}
