package nl.idfocus.nam.logic.data;

import java.io.Serializable;

import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.BasicAttribute;

public class AttributeValue implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 30705114612499851L;

	private Attribute attr = null;
	private String dn = "";

	public AttributeValue( String dn, Attribute attr )
	{
		this.attr = attr;
		this.dn = dn;
	}
	
	public AttributeValue( String dn, String name, String value )
	{
		this.attr = new BasicAttribute(name, value);
		this.dn = dn;
	}

	@Override
	public String toString() 
	{
		try
		{
			if ( attr.get() != null )
				return String.format( "Object %20s: attribute %10s containing value %15s", dn, attr.getID(), attr.get() );
			else
				return String.format( "Object %20s: attribute %10s", dn, attr.getID() );
		}
		catch (NamingException e)
		{
			
		}
		return super.toString();
	}
}
