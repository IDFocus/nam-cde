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

import nl.idfocus.nam.logic.data.DataResponse;


public class ActionCount implements Action 
{

	private Action action;

	public ActionCount() 
	{
		this.action = null;
	}
	
	@Override
	public void addAction(Action action) throws ActionException
	{
		if ( this.action == null )
			this.action = action;
		else
			throw new ActionException( "Count: too many arguments" );
	}

	@Override
	public String[] perform(DataResponse attrs) 
	{
		String[] result = this.action.perform( attrs );
		return new String[] { String.valueOf(result.length) };
	}

}
