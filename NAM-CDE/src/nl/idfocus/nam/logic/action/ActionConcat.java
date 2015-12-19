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


public class ActionConcat implements Action 
{

	private final List<Action> actions;
	
	public ActionConcat() 
	{
		this.actions = new ArrayList<Action>();
	}

	@Override
	public void addAction(Action action) 
	{
		this.actions.add( action );
	}

	@Override
	public String[] perform( DataResponse attrs )
	{
		List<String> values = new ArrayList<String>();
		/* 
		 * sequence:
		 *  for each action, call perform()
		 *   attribute action will retrieve value
		 *   literal action will just return literal
		 *   others will recurse
		 */
		StringBuilder result = new StringBuilder();
		for ( Action action : this.actions )
		{
			for ( String value : action.perform( attrs ) )
			{
				result.append( value );
				// TODO if we add a break here, it just concats the first value it finds
			}
		}
		values.add(result.toString());
		return values.toArray(new String[values.size()]);
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append( "Concat: [" );
		for ( Action action : this.actions )
		{
			result.append( action.toString() );
		}
		result.append( "]" );
		return result.toString();
	}
}
