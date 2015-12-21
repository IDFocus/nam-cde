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
package nl.idfocus.test.nam.logic;

import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novell.nxpe.NxpeInformationContext;
import com.novell.nxpe.NxpeParameterList;
import com.novell.nxpe.NxpeResponseContext;

public class ExtensionTest 
{
	private NxpeParameterList params;
	private NxpeInformationContext context;
	private NxpeResponseContext response;

	@Before
	public void setUp() throws Exception 
	{
		params = null;
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

}
