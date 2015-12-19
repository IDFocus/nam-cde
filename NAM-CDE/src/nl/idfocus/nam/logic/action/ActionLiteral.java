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


public class ActionLiteral implements Action 
{

	private final String literalValue;

	public ActionLiteral( String value ) 
	{
		this.literalValue = value;
	}

	@Override
	public void addAction(Action action) throws ActionException
	{
		throw new ActionException( "Literal: no further actions can be nested" );
	}

	@Override
	public String[] perform(DataResponse attrs) 
	{
		return new String[]{ this.literalValue };
	}

	@Override
	public String toString()
	{
		return "Literal: ["+this.literalValue+"]";
	}
}
