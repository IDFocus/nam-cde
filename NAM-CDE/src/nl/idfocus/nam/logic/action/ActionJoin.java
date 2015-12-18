package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;

public class ActionJoin implements Action 
{
	private Action action;
	private Action separator;

	public ActionJoin() 
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
			throw new ActionException("Join: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		List<String> values = new ArrayList<String>();
		String[] sep = new String[]{""};
		if ( this.separator != null )
			sep = this.separator.perform(data);
		if ( sep.length == 1 )
		{
			StringBuilder finalvalue = new StringBuilder();
			String[] rawvalues = this.action.perform(data);
			for( int i=0; i<rawvalues.length; i++  )
			{
				finalvalue.append( rawvalues[i] );
				if ( i < rawvalues.length -1 )
					finalvalue.append( sep[0] );
			}
			values.add( finalvalue.toString() );
		}
		else
		{
			return this.action.perform(data);
		}
		return values.toArray(new String[values.size()]);
	}

}
