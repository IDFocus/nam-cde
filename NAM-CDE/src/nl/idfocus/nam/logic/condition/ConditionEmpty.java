package nl.idfocus.nam.logic.condition;

import nl.idfocus.nam.logic.data.DataResponse;

public class ConditionEmpty implements Condition {

	@Override
	public void addCondition(Condition condition) throws ConditionException 
	{
		// Throw exception (can never happen)
		throw new ConditionException("Cannot add conditions to empty condition");
	}

	@Override
	public boolean evaluate( DataResponse attrs ) 
	{
		// The idea of an empty condition is that it always applies and the action is always executed
		return true;
	}

	@Override
	public String toString()
	{
		return "Empty condition evaluating to 'true'";
	}
}
