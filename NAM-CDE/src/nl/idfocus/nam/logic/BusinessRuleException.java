package nl.idfocus.nam.logic;

public class BusinessRuleException extends Exception 
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3925253809897299560L;

	public BusinessRuleException()
	{
		super();
	}

	public BusinessRuleException( String mesg )
	{
		super( mesg );
	}
	
	public BusinessRuleException( Throwable cause )
	{
		super( cause );
	}
}