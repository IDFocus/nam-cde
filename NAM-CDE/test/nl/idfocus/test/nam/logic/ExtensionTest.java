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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.novell.nidp.policy.attribute.ExternalAttributeInformationContext;
import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeException;
import com.novell.nxpe.NxpeInformationContext;
import com.novell.nxpe.NxpeInformationData;
import com.novell.nxpe.NxpeInformationKey;
import com.novell.nxpe.NxpeInformationMap;
import com.novell.nxpe.NxpeParameterList;
import com.novell.nxpe.NxpeResponseContext;

import nl.idfocus.nam.extension.ConditionalData;
import nl.idfocus.nam.extension.ConditionalDataFactory;

public class ExtensionTest 
{
	ExternalAttributeInformationContext	infoctx;

	private NxpeParameterList params;
	private NxpeInformationContext context;
	private NxpeResponseContext response;

	@Before
	public void setUp() throws Exception 
	{
		params = null;
		infoctx = getInformationContext();
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testExtensionFactory() {
		try
		{
			NxpeContextDataElement lookup = new ConditionalDataFactory().getInstance("name", 0, "param");
			assertEquals(ConditionalData.class.getName(), lookup.getClass().getName());
		}
		catch (NxpeException e)
		{
			fail("Exception: " + e.getMessage());
		}
	}

	@Test
	public void testInitializeFail()
	{
		try
		{
			NxpeContextDataElement lookup = new ConditionalDataFactory().getInstance("name", 0, "param");
			lookup.initialize(null);
			fail("Expected exception but none occurred");
		}
		catch (NxpeException e)
		{
			assertEquals(NxpeException.class.getName(), e.getClass().getName());
			assertEquals("No parameters received upon initialization", e.getMessage());
		}
	}

	@Test
	public void testInitializeSuccess()
	{
		try
		{
			NxpeContextDataElement lookup = new ConditionalDataFactory().getInstance("name", 0, "param");
			lookup.initialize(getValidParamsList());
		}
		catch (NxpeException e)
		{
			fail("Exception: " + e.getMessage());
		}
	}

	private ExternalAttributeInformationContext getInformationContext()
	{
		NxpeInformationData data = new NxpeInformationData("IdNumber", 20, "176414212", -1);
		NxpeInformationMap map = new NxpeInformationMap();
		map.put(new NxpeInformationKey(data), data);
		ExternalAttributeInformationContext infoCtx = new ExternalAttributeInformationContext();
		infoCtx.setAttributeName("cakid");
		try
		{
			infoCtx.setContextData(map);
			infoCtx.setLoggingEnabled(true);
			infoCtx.setTracingEnabled(true);
			infoCtx.setAuthenticationId("authid");
		}
		catch (NxpeException e)
		{
			e.printStackTrace();
		}
		return infoCtx;
	}

	private NxpeParameterList getValidParamsList()
	{
		NxpeParameterList list = new nl.idfocus.test.nam.nxpe.TestNxpeParameterList()
				.addParameter(new nl.idfocus.test.nam.nxpe.TestNxpeParameter("SoapEndpoint", 10, "/Service/CprService.svc"))
				.addParameter(new nl.idfocus.test.nam.nxpe.TestNxpeParameter("IdNumber", 20, "176414212"));
		return list;
	}

}
