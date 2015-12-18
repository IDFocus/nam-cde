package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionConcat implements Action 
{

	private final List<Action> actions;
	
	public ActionConcat() 
	{
		this.actions = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) 
	{
		this.actions.add( action );
	}

	@Override
	public String[] perform( DataResponse attrs )
	{
		List<String> values = new ArrayList<String>();
		/* 
		 * sequence:
		 *  for each action, call perform()
		 *   attribute action will retrieve value
		 *   literal action will just return literal
		 *   others will recurse
		 */
		StringBuilder result = new StringBuilder();
		for ( Action action : this.actions )
		{
			for ( String value : action.perform( attrs ) )
			{
				result.append( value );
				// TODO if we add a break here, it just concats the first value it finds
			}
		}
		values.add(result.toString());
		return values.toArray(new String[values.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append( "Concat: [" );
		for ( Action action : this.actions )
		{
			result.append( action.toString() );
		}
		result.append( "]" );
		return result.toString();
	}
}
