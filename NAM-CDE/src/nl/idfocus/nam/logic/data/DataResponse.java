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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

public class DataResponse implements Iterable<AttributeValue>
{
	private Attributes primaryAttributes;
	private Map<String,Attributes> secondaryAttributes;

	public DataResponse()
	{
		this.primaryAttributes = null;
		this.secondaryAttributes = new HashMap<String, Attributes>();
	}

	public Attribute getRelatedAttribute(String dnValue, String dnLookupAttr) 
	{
		if ( secondaryAttributes.containsKey(dnValue) && secondaryAttributes.get(dnValue) != null  )
			return secondaryAttributes.get(dnValue).get(dnLookupAttr);
		return null;
	}

	public Attribute getAttribute(String attributeName) 
	{
		if ( primaryAttributes != null && attributeName != null )
			return primaryAttributes.get(attributeName);
		return null;
	}

	public void addAttributes(Attributes primaryAttrs) 
	{
		if ( this.primaryAttributes == null )
			primaryAttributes = primaryAttrs;
	}

	public void addAttributes(String dnValue, Attributes secondaryAttrs) 
	{
		if ( ! secondaryAttributes.containsKey(dnValue) )
			secondaryAttributes.put(dnValue, secondaryAttrs);
	}

	public void addAttribute(Attribute updateAttr) 
	{
		if ( this.primaryAttributes != null )
			primaryAttributes.put(updateAttr);
	}

	@Override
	public Iterator<AttributeValue> iterator() 
	{
		return new ValueIterator();
	}

	class ValueIterator implements Iterator<AttributeValue>
	{

		private int count = 0;
		private int max = 0;
		private NamingEnumeration<?> primary;
		private NamingEnumeration<?> secondary;
		private Iterator<String> secondaryKeys;
		private String secondaryKey;

		public ValueIterator() 
		{
			max = primaryAttributes.size();
			for ( String key : secondaryAttributes.keySet() )
			{
				if ( secondaryAttributes.get(key) != null )
					max += secondaryAttributes.get(key).size();
			}
			primary = primaryAttributes.getAll();
			secondaryKeys = secondaryAttributes.keySet().iterator();
			secondaryKey  = "";
			secondary     = new BasicAttributes().getAll();
		}

		@Override
		public boolean hasNext() 
		{
			if ( count < max )
				return true;
			return false;
		}

		@Override
		public AttributeValue next() 
		{
			if ( count < primaryAttributes.size() )
			{
				count++;
				try
				{
					return new AttributeValue( "PRINCIPAL", (Attribute)primary.next() );
				} 
				catch (NamingException e) 
				{
					return new AttributeValue( "PRINCIPAL", new BasicAttribute( "ERROR", e.getExplanation() ) );
				}
			}
			else if ( count < max )
			{
				count++;
				try
				{
					if ( secondary.hasMore() )
					{
						return new AttributeValue( secondaryKey, (Attribute)secondary.next() );
					}
					else
					{
						secondaryKey = secondaryKeys.next();
						// FIXME potential NPE when key holds null value
						secondary = secondaryAttributes.get(secondaryKey).getAll();
						return new AttributeValue( secondaryKey, (Attribute)secondary.next() );
					}
				} 
				catch (NamingException e) 
				{
					return new AttributeValue( "SECONDARY", new BasicAttribute( "ERROR", e.getExplanation() ) );
				}
			}
			// hasNext() would have returned false by now
			return null;
		}

		@Override
		public void remove() {
			// TODO Auto-generated method stub
			
		}
		
	}
}
