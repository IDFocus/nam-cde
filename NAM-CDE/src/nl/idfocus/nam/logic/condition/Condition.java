package nl.idfocus.nam.logic.condition;

import nl.idfocus.nam.logic.data.DataResponse;

public interface Condition 
{

	void addCondition( Condition condition ) throws ConditionException;

	boolean evaluate( DataResponse data );

}
