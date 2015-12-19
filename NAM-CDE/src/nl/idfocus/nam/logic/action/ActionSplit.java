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
import java.util.Arrays;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;

public class ActionSplit implements Action 
{
	private Action action;
	private Action separator;

	public ActionSplit() 
	{
		this.action = null;
		this.separator = null;
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else if ( this.separator == null )
			this.separator = action;
		else
			throw new ActionException("Split: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		List<String> values = new ArrayList<String>();
		String[] sep = this.separator.perform(data);
		if ( sep.length == 1 )
		{
			String pat = java.util.regex.Pattern.quote( sep[0] );
			for( String value : this.action.perform(data) )
				values.addAll( Arrays.asList( value.split( pat ) ) );
		}
		else
		{
			return this.action.perform(data);
		}
		return values.toArray(new String[values.size()]);
	}

}
