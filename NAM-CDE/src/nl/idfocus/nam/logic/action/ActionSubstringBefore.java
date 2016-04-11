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


public class ActionSubstringBefore implements Action 
{

	private final List<Action> actions;

	public ActionSubstringBefore() 
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
		else if ( this.actions.size() < 2 )
		{
			if (! ( action instanceof ActionSplit || action instanceof ActionLength ) )
				this.actions.add( action );
			else
				throw new ActionException( "Substring-before: invalid arguments" );
		}
		else
		{
			throw new ActionException( "Substring-before: too many arguments" );
		}
	}

	@Override
	public String[] perform( DataResponse attrs ) 
	{
		List<String> values = new ArrayList<String>();
		if ( this.actions.size() == 2 )
		{
			String[] source = this.actions.get(0).perform(attrs);
			String[] end  = this.actions.get(1).perform(attrs);
			if ( end.length == 1 )
			{
				for ( String value : source )
				{
					try
					{
						int position = value.indexOf( end[0] );
						if( position > -1 )
							values.add( value.substring( 0, position ) );
					}
					catch ( NullPointerException e ) {}
					catch ( IndexOutOfBoundsException e ) {}
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
