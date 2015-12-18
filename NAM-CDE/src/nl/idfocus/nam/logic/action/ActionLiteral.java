package nl.idfocus.nam.logic.action;

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionLiteral implements Action 
{

	private final String literalValue;

	public ActionLiteral( String value ) 
	{
		this.literalValue = value;
	}

	@Override
	public void addAction(Action action) throws ActionException
	{
		throw new ActionException( "Literal: no further actions can be nested" );
	}

	@Override
	public String[] perform(DataResponse attrs) 
	{
		return new String[]{ this.literalValue };
	}

	@Override
	public String toString()
	{
		return "Literal: ["+this.literalValue+"]";
	}
}
