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
import nl.idfocus.nam.util.Sha256;

public class ActionSHA256 implements Action 
{
	private Action action;
	private Action salt;

	public ActionSHA256() 
	{
		this.action = null;
	}

	@Override
	public void addAction(Action action) throws ActionException 
	{
		if ( this.action == null )
			this.action = action;
		else if ( this.salt == null )
			if ( action instanceof ActionLiteral )
				this.salt = action;
			else
				throw new ActionException("SHA256: second argument must be a literal");
		else
			throw new ActionException("SHA256: too many arguments");
	}

	@Override
	public String[] perform(DataResponse data) 
	{
		// Use given salt value or leave it null.
		String saltStr = null;
		if ( this.salt != null )
			saltStr = this.salt.perform(data)[0];
		// Hash each resulting value from the main action
		List<String> values = new ArrayList<String>();
		for( String value : this.action.perform(data) )
			values.add( Sha256.toSaltedHashString( value, saltStr ) );
		return values.toArray(new String[values.size()]);
	}

}
