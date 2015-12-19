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
package nl.idfocus.nam.logic.action;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;

import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;


public class ActionAttribute implements Action 
{
	private static final Logger logger = LogFormatter.getConsoleLogger( ActionAttribute.class.getName() );
	private final Level loglevel = Level.FINE;

	private final String attributeName;

	public ActionAttribute( String value ) 
	{
		this.attributeName = value;
	}

	@Override
	public void addAction( Action action ) throws ActionException
	{
		throw new ActionException( "Attribute: no further actions can be nested" );
	}

	@Override
	public String[] perform( DataResponse attrs ) 
	{
		logger.log( loglevel, "Processing "+attributeName );
		List<String> values = new ArrayList<String>();
		if ( attrs != null )
		{
			Attribute attr = attrs.getAttribute( this.attributeName );
			if ( attr != null )
			{
				try {
					NamingEnumeration<?> rawvalues = attr.getAll();
					while( rawvalues.hasMore() )
						values.add((String)rawvalues.next());
				} 
				catch (NamingException e) {}
				catch (NoSuchElementException e) {}
				catch (ClassCastException e) {}
			}
		}
		logger.log( loglevel, "Found " + values.size() + " value" + (values.size() == 1 ? "" : "s" ) );
		return values.toArray(new String[values.size()]);
	}

	@Override
	public String toString()
	{
		return "Attribute: ["+attributeName+"]";
	}
}
