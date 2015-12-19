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

import java.util.ArrayList;
import java.util.List;

import nl.idfocus.nam.logic.data.DataResponse;


public class ConditionAnd implements Condition 
{

	private final List<Condition> conditions;
	
	public ConditionAnd() 
	{
		this.conditions = new ArrayList<Condition>();
	}

	@Override
	public boolean evaluate( DataResponse attrs) 
	{
		for ( Condition cond : this.conditions )
		{
			if ( ! cond.evaluate( attrs ) )
				return false;
		}
		return true;
	}

	@Override
	public void addCondition(Condition condition) 
	{
		this.conditions.add( condition );
	}

	@Override
	public String toString()
	{
		return "'And' condition contains "+this.conditions.size()+" conditions";
	}
}
