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

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionSubstring implements Action 
{

	private final List<Action> actions;

	public ActionSubstring() 
	{
		this.actions = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) throws ActionException
	{
		if ( this.actions.size() == 0 )
		{
			this.actions.add( action );
		}
		// FIXME substrings of length() or outcome of perform() may be arguments
		else if ( this.actions.size() < 3 )
		{
			if ( action instanceof ActionLiteral )
			{
				try
				{
					Integer.parseInt( action.perform(null)[0] );
					this.actions.add( action );
				}
				catch ( Exception e ) {
					throw new ActionException( "Substring: invalid arguments" );					
				}
			} else if ( action instanceof ActionLength )
				this.actions.add( action );
		}
		else
		{
			throw new ActionException( "Substring: too many arguments" );
		}
	}

	@Override
	public String[] perform( DataResponse attrs ) 
	{
		List<String> values = new ArrayList<String>();
		if ( this.actions.size() == 3 )
		{
			String[] source = this.actions.get(0).perform(attrs);
			String[] start  = this.actions.get(1).perform(attrs);
			String[] end    = this.actions.get(2).perform(attrs);
			if ( start.length == 1 && end.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						values.add( value.substring( Integer.parseInt( start[0] ), Integer.parseInt( end[0] ) ) );
					}
					catch ( NullPointerException e ) {}
					catch ( NumberFormatException e ) {}
				}
			}
			else
			{
				return source;
			}
		}
		else if ( this.actions.size() == 2 )
		{
			String[] source = this.actions.get(0).perform(attrs);
			String[] start  = this.actions.get(1).perform(attrs);
			if ( start.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						values.add( value.substring( Integer.parseInt( start[0] ) ) );
					}
					catch ( NullPointerException e ) {}
					catch ( NumberFormatException e ) {}
				}
			}
			else
			{
				return source;
			}
		}
		return values.toArray(new String[values.size()]);
	}

}
