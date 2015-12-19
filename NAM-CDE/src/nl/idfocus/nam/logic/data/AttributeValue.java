/**
 * Copyright 2015 IDFocus B.V.
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
