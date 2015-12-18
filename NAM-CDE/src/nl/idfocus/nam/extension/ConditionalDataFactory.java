package nl.idfocus.nam.extension;

import java.util.logging.Logger;

import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeContextDataElementFactory;
import com.novell.nxpe.NxpeException;

public class ConditionalDataFactory implements NxpeContextDataElementFactory 
{

	private static Logger logger = Logger.getLogger( ConditionalDataFactory.class.getName() );

	/**
	 *
	 */
	public ConditionalDataFactory()
	{
		logger.info("Instantiating " + this.getClass().getName() );
	}

	/**
	 * Factory method for external condition handlers
	 *
	 * Notes: This method is initiated for each condition as defined in the
	 * policy type specification
	 *
	 * @throws NxpeException
	 */
	public NxpeContextDataElement getInstance( String strName, int iEnumerativeValue, String strParameter) throws NxpeException
	{
		logger.info("Getting instance of ConditionalData...");
		return ( new ConditionalData(strName, iEnumerativeValue, strParameter) );
	}

}
