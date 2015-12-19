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
package nl.idfocus.nam.logic.condition;

import nl.idfocus.nam.logic.data.DataResponse;


public class ConditionNot implements Condition 
{

	private Condition condition;
	
	public ConditionNot() 
	{
		this.condition = null;
	}

	@Override
	public boolean evaluate( DataResponse attrs ) 
	{
		if ( this.condition != null )
			return !this.condition.evaluate( attrs );
		else
			return true;
	}

	@Override
	public void addCondition(Condition condition) throws ConditionException 
	{
		if ( this.condition == null )
			this.condition = condition;
		else
			throw new ConditionException( "Not: too many arguments" );
	}

	@Override
	public String toString()
	{
		return "'Not' condition contains "+this.condition==null ? "no" : "1" + " condition(s)";
	}

}
