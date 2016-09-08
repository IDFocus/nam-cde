package nl.idfocus.test.nam.nxpe;

import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeException;
import com.novell.nxpe.NxpeInformationContext;
import com.novell.nxpe.NxpeParameterList;
import com.novell.nxpe.NxpeResponseContext;

public class TextNxpeContextDataElement implements NxpeContextDataElement
{
	private String name;
	private int iname;
	private String value;

	public TextNxpeContextDataElement( String name, int iname, String value )
	{
		this.name = name;
		this.iname = iname;
		this.value = value;
	}

	public int getEnumerativeValue()
	{
		return iname;
	}

	public String getName()
	{
		return name;
	}

	public String getParameter()
	{
		return value;
	}

	public Object getValue(NxpeInformationContext arg0, NxpeResponseContext arg1)
			throws NxpeException
	{
		return null;
	}

	public void initialize(NxpeParameterList arg0) throws NxpeException
	{
	}

}
