package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;

public class ActionSplit implements Action 
{
	private Action action;
	private Action separator;

	public ActionSplit() 
	{
		this.action = null;
		this.separator = null;
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else if ( this.separator == null )
			this.separator = action;
		else
			throw new ActionException("Split: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		List<String> values = new ArrayList<String>();
		String[] sep = this.separator.perform(data);
		if ( sep.length == 1 )
		{
			String pat = java.util.regex.Pattern.quote( sep[0] );
			for( String value : this.action.perform(data) )
				values.addAll( Arrays.asList( value.split( pat ) ) );
		}
		else
		{
			return this.action.perform(data);
		}
		return values.toArray(new String[values.size()]);
	}

}
