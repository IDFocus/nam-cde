package nl.idfocus.nam.logic.condition;

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ConditionAnd implements Condition 
{

	private final List<Condition> conditions;
	
	public ConditionAnd() 
	{
		this.conditions = new ArrayList<Condition>();
	}

	@Override
	public boolean evaluate( DataResponse attrs) 
	{
		for ( Condition cond : this.conditions )
		{
			if ( ! cond.evaluate( attrs ) )
				return false;
		}
		return true;
	}

	@Override
	public void addCondition(Condition condition) 
	{
		this.conditions.add( condition );
	}

	@Override
	public String toString()
	{
		return "'And' condition contains "+this.conditions.size()+" conditions";
	}
}
