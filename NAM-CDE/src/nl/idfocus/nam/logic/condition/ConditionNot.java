package nl.idfocus.nam.logic.condition;

import nl.idfocus.nam.logic.data.DataResponse;


public class ConditionNot implements Condition 
{

	private Condition condition;
	
	public ConditionNot() 
	{
		this.condition = null;
	}

	@Override
	public boolean evaluate( DataResponse attrs ) 
	{
		if ( this.condition != null )
			return !this.condition.evaluate( attrs );
		else
			return true;
	}

	@Override
	public void addCondition(Condition condition) throws ConditionException 
	{
		if ( this.condition == null )
			this.condition = condition;
		else
			throw new ConditionException( "Not: too many arguments" );
	}

	@Override
	public String toString()
	{
		return "'Not' condition contains "+this.condition==null ? "no" : "1" + " condition(s)";
	}

}
