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
package nl.idfocus.nam.extension;

import java.util.logging.Logger;

import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeContextDataElementFactory;
import com.novell.nxpe.NxpeException;

public class ConditionalDataFactory implements NxpeContextDataElementFactory 
{

	private static Logger logger = Logger.getLogger( ConditionalDataFactory.class.getName() );

	/**
	 * Empty constructor to allow us to see when the factory object is created.
	 */
	public ConditionalDataFactory()
	{
		logger.info("Instantiating " + this.getClass().getName() );
	}

	/**
	 * Factory method for external condition handlers
	 *
	 * Notes: This method is initiated for each condition as defined in the
	 * policy type specification
	 * 
	 * @param strName
	 * @param iEnumerativeValue
	 * @param strParameter
	 * @throws NxpeException
	 * @return
	 */
	@Override
	public NxpeContextDataElement getInstance( String strName, int iEnumerativeValue, String strParameter) throws NxpeException
	{
		logger.info("Getting instance of ConditionalData...");
		return new ConditionalData(strName, iEnumerativeValue, strParameter);
	}

}
