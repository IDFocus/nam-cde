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

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

public class DataRequest implements Iterable<String>
{
	private final Set<String> principalAttrs;
	private final HashMap<String, Set<String>> secondaryPrincipals;

	public DataRequest()
	{
		principalAttrs = new HashSet<String>();
		secondaryPrincipals = new HashMap<String, Set<String>>();
	}

	public void addAttribute( String attr )
	{
		if ( attr != null && !attr.isEmpty() )
			principalAttrs.add( attr );
	}

	public void addAttribute( String source, String attr )
	{
		if ( source != null && attr != null )
			if ( secondaryPrincipals.containsKey(source) )
			{
				secondaryPrincipals.get(source).add(attr);
			}
			else
			{
				Set<String> data = new HashSet<String>();
				data.add(attr);
				secondaryPrincipals.put( source, data );
			}
	}

	public void addAttributes( List<String> attrs )
	{
		if ( attrs != null && attrs.size() > 0 )
			principalAttrs.addAll(attrs);
	}

	public void addAttributes( String source, Set<String> attrs )
	{
		if ( source != null && attrs != null && attrs.size() > 0 )
		{
			if( secondaryPrincipals.containsKey(source) )
				secondaryPrincipals.get(source).addAll(attrs);
			else
				secondaryPrincipals.put(source, attrs);
		}
	}

	/**
	 * Retrieve the required attributes for a secondary principal whose DN is referenced in a dn() action.
	 * @param source attribute name of the DN reference
	 * @return
	 */
	public Set<String> requiredAttributes( String source )
	{
		if ( source != null && secondaryPrincipals.containsKey(source) )
			return secondaryPrincipals.get(source);
		return new HashSet<String>();
	}

	/**
	 * Retrieve the required attributes for the primary principal logging in. 
	 * @return
	 */
	public Set<String> requiredAttributes()
	{
		return principalAttrs;
	}

	public int numberOfAttributes()
	{
		return this.principalAttrs.size();
	}

	public int numberOfAttributes( String source )
	{
		if ( secondaryPrincipals.containsKey(source))
			return secondaryPrincipals.get(source).size();
		return -1;
	}

	/**
	 * Iterator that iterates over the source DN attributes in a DataRequest object. <br/>
	 * The resulting String values can be used in {@link #requiredAttributes(String)}.
	 */
	@Override
	public Iterator<String> iterator() 
	{
		return secondaryPrincipals.keySet().iterator();
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append( "Primary Principal: " )
			  .append( Arrays.toString( 
					  principalAttrs.toArray( 
							  new String[principalAttrs.size()] ) ) )
			  .append( "\n" );
		if ( secondaryPrincipals.size() > 0 )
		{
			result.append( "Secondary Principals: " )
				  .append( "\n" );
			for ( String key : secondaryPrincipals.keySet() )
			{
				result.append( "Value of " )
					  .append( key )
					  .append( ": " )
					  .append( Arrays.toString( 
							  secondaryPrincipals.get(key).toArray( 
									  new String[secondaryPrincipals.get(key).size()])));
			}
		}
		return result.toString();
	}

	/**
	 * Merge all the attribute lists of another DataRequest with this one. <br/>
	 * The use of {@link #Set} ensures unique lists of values
	 * @param another
	 */
	public void merge( DataRequest another )
	{
		this.principalAttrs.addAll( another.requiredAttributes() );
		for ( String source : another )
		{
			if ( this.secondaryPrincipals.containsKey(source) )
				this.secondaryPrincipals.get(source).addAll( another.requiredAttributes(source) );
			else
				this.secondaryPrincipals.put(source, another.requiredAttributes(source) );
		}
	}

	/**
	 * Clear all values contained in this DataRequest. Used in the static caching class. 
	 */
	public void clear() 
	{
		principalAttrs.clear();
		secondaryPrincipals.clear();
	}
}
