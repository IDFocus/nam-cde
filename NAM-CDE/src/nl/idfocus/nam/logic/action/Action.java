package nl.idfocus.nam.logic.action;

import nl.idfocus.nam.logic.data.DataResponse;

public interface Action 
{

	void addAction( Action action ) throws ActionException;
	
	String[] perform( DataResponse data );

	String toString();
}
