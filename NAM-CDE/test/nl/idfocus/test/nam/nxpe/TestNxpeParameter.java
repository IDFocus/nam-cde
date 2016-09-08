package nl.idfocus.test.nam.nxpe;

import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeException;
import com.novell.nxpe.NxpeParameter;

public class TestNxpeParameter implements NxpeParameter
{
	private String name;
	private int iname;
	private String value;

	public TestNxpeParameter( String name, int iname, String value )
	{
		this.name = name;
		this.iname = iname;
		this.value = value;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + iname;
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		TestNxpeParameter other = (TestNxpeParameter) obj;
		if (iname != other.iname)
			return false;
		return true;
	}

	public NxpeContextDataElement getContextDataElement() throws NxpeException
	{
		return new TextNxpeContextDataElement(name, iname, value);
	}

	public int getContextDataElementEnumerativeValue() throws NxpeException
	{
		return iname;
	}

	public String getContextDataElementName() throws NxpeException
	{
		return name;
	}

	public int getEnumerativeValue() throws NxpeException
	{
		return iname;
	}

	public int getForceDataRead() throws NxpeException
	{
		return 0;
	}

	public String getName() throws NxpeException
	{
		return name;
	}

	public String getValue() throws NxpeException
	{
		return value;
	}

}
