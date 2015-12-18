/**
 * 
 */
package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;

/**
 * @author mvreijn
 *
 */
public class ActionUpper implements Action 
{
	private Action action;
	
	public ActionUpper() 
	{
		this.action = null;
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else
			throw new ActionException("Upper: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		List<String> values = new ArrayList<String>();
		for( String value : this.action.perform(data) )
			values.add( value.toUpperCase() );
		return values.toArray(new String[values.size()]);
	}

}
