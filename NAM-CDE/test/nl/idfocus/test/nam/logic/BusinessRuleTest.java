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

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.List;

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

import nl.idfocus.nam.logic.BusinessRule;
import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.logic.data.DataResponse;

import org.junit.Before;
import org.junit.Test;

public class BusinessRuleTest 
{
	DataResponse response;

	@Before
	public void setUp() throws Exception 
	{
		response = new DataResponse();
		Attributes attrs = TestData.getPrimaryAttributes();
		// Lennart test
		List<String> adgroups = new ArrayList<String>();
		adgroups.add( "CN=Public360,OU=Application,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=SSO_E-fakt,OU=A_Provisioning,OU=Applikationer,OU=Atkomst,OU=NordicEdge,DC=kolan,DC=org" );
		adgroups.add( "CN=GP Standard Användare,OU=Delegation,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=TS Standard Användare,OU=Terminal Service,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=SSO_SKL Ärende,OU=B_Provisioning,OU=Applikationer,OU=Atkomst,OU=NordicEdge,DC=kolan,DC=org" );
		adgroups.add( "CN=AG Kikaren,OU=Application,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=SSO_SKL Kontakt,OU=Arkiv,OU=NordicEdge,DC=kolan,DC=org" );
		adgroups.add( "CN=System - Concierge bokningar,OU=System,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_data R,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_www_vard C,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=FS Appl_sbprog C,OU=Filsystem,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS Appl_sbprogw C,OU=Filsystem,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FM Mötesbokningar sektionen för hälsa och jämnställdhet,OU=Exchange FunctionMailbox,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=DL Avd för vård & omsorg,OU=Exchange Distribution Lists,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=DL Alla på SK,OU=Exchange Distribution Lists,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=DL IT-ombud,OU=Exchange Distribution Lists,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=Deny GPO - SKL - Windows 7 - Password Reminder Popup_U,OU=Group Policy,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=TS Alla Terminal Server Användare,OU=Terminal Service,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=SKL - Mappning_G_ Gemensam Data,OU=Group Policy,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=DL Administratörer och Assistenter på SKLF,OU=Exchange Distribution Lists,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=NordicEdge_ITOmbud_SKL_Role,OU=Roles,OU=NordicEdge,DC=kolan,DC=org" );
		adgroups.add( "CN=SKL - Homepage Kikaren,OU=Group Policy,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=App - Actit Business 1.9,OU=Group Policy,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=AG Intressent Begransad Updatering,OU=Application,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=DL VO Administratörer,CN=Users,DC=kolan,DC=org" );
		adgroups.add( "CN=DL Hälsa och Jämställdhet,OU=Exchange Distribution Lists,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_avd vård & omsorg\\\\Walk and Talk C,OU=Filsystem,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS Data_Hälso- och Sjukvårdens utveckling- C,OU=Hälso- och sjukvårdens utveckling,OU=Department,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=TS Dreamweaver,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=AG XP Standard,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( null );
		adgroups.add( "CN=AppUsers,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_www_cbcpf_hansa R,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_kursmaterial C,OU=Informationsenheten,OU=Department,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS Avdelningen för vård och omsorg,OU=Filsystem,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=AG XP Business 1.6,OU=Application,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=FS data_www R,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=FS Data_Hälso- och Sjukvårdspolitik C,OU=Hälso- och sjukvårdspolitik,OU=Department,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		adgroups.add( "CN=AD Äldreomsorg- och sjukvård,OU=Groups,OU=Limbo,DC=kolan,DC=org" );
		adgroups.add( "CN=AD Alla SK Användare,OU=Filsystem,OU=Common Groups,OU=SKLF,DC=kolan,DC=org" );
		Attribute adgroupnames = new BasicAttribute( "admember" );
		for ( String group : adgroups)
			adgroupnames.add(group);
		attrs.put( adgroupnames );
		response.addAttributes( attrs );
		response.addAttributes( "cn=myManager", TestData.getManagerAttributes() );
		response.addAttributes( "cn=group1", TestData.getGroup1Attributes() );
		response.addAttributes( "cn=group2", TestData.getGroup2Attributes() );
		for ( String group : adgroups )
		{
			if ( group != null && group.equals("CN=AppUsers,OU=Groups,OU=Limbo,DC=kolan,DC=org") )
			{
				response.addAttributes( group, null );
			}
			else if ( group != null )
			{
				Attributes tmp = new BasicAttributes();
				tmp.put( "cn", group.substring(group.indexOf("=")+1, group.indexOf(",") ) );
				response.addAttributes( group, tmp );
			}
		}
	}

	@Test
	public void BasicRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_1", "condition: \"not('a'='b')\", action: \"concat('a','b')\", destination: \"myAttribute\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
			assertEquals( "Rule application incorrect", true, testRule.applies(response) );
			assertEquals( "Rule result incorrect", "ab", testRule.getResult(response)[0] );
		} catch (Exception e) {
			e.printStackTrace();
			fail( e.getMessage() );
		}
	}

	@Test
	public void SimpleRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_2", "action: \"concat('a','b')\", destination: \"myAttribute\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
			assertEquals( "Rule application incorrect", true, testRule.applies(response) );
			assertEquals( "Rule result incorrect", "ab", testRule.getResult(response)[0] );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void DisabledSimpleRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_3", "disabled: \"true\", action: \"concat('a','b')\", destination: \"myAttribute\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Rule should be inactive", false, testRule.isActive() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
			assertEquals( "Rule application incorrect", false, testRule.applies(response) );
			assertEquals( "Rule result incorrect", "ab", testRule.getResult(response)[0] );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void InvalidRuleOneTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_4", "disabled: \"true, action: \"concat('a','b')\", destination: \"myAttribute\"" );
			assertEquals( "Rule should be invalid", false, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void InvalidRuleTwoTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_5", "disabled: \"true\", action: concat('a','b')\", destination: \"myAttribute\"" );
			assertEquals( "Rule should be invalid", false, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void InvalidRuleThreeTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_6", "disabled: \"true\", action: \"concat('a','b')\", destination: myAttribute\"" );
			assertEquals( "Rule should be invalid", false, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "myAttribute", testRule.getDestination() );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void FredrikRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_7", "action: \"concat( 'dept: ', cn)\", destination: \"carLicense\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "carLicense", testRule.getDestination() );
			assertEquals( "Rule application incorrect", true, testRule.applies(response) );
			assertEquals( "Rule result incorrect", "dept: myUsername", testRule.getResult(response)[0] );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void SebastijanRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_8", "action: \"dn( memberof, samaccountname )\", destination: \"samaccountnames\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "samaccountnames", testRule.getDestination() );
			DataRequest data = testRule.requires();
			assertEquals( "Number of primary attributes incorrect", 1, data.numberOfAttributes() );
			assertEquals( "Number of secondary attributes incorrect", 1, data.numberOfAttributes( "memberof" ) );
			assertEquals( "Rule application incorrect", true, testRule.applies(response) );
			assertArrayEquals( "Rule result incorrect", new String[]{"group1","group2"}, testRule.getResult(response) );
		} catch (Exception e) {
			fail( e.getMessage() );
		}
	}

	@Test
	public void LennartRuleTest()
	{
		try
		{
			BusinessRule testRule = new BusinessRule( "RULE_9", "action: \"dn( admember, cn )\", destination: \"AclGroup\"" );
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "AclGroup", testRule.getDestination() );
			assertEquals( "Rule application incorrect", true, testRule.applies(response) );
			assertEquals( "Rule result incorrect", 38, testRule.getResult(response).length );
			assertEquals( "Rule result incorrect", "Public360", testRule.getResult(response)[0] );
			assertEquals( "Rule result incorrect", "FS data_avd vård & omsorg\\\\Walk and Talk C", testRule.getResult(response)[26] );
		} catch (Exception e) {
			fail( e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	@Test
	public void SeppoRuleTest1()
	{
		try
		{
			BusinessRule testRule = new BusinessRule("RULE_10", "condition: \"( ULCNuserClass = 'staff')\", action: \"( preferredName )\", destination: \"CDEfirstName\"");
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "CDEfirstName", testRule.getDestination() );
			assertEquals( 2, testRule.requires().numberOfAttributes() );
		} catch (Exception e) {
			fail( e.getClass().getName() + ": " + e.getMessage() );
		}
	}

	@Test
	public void SeppoRuleTest2()
	{
		try
		{
			BusinessRule testRule = new BusinessRule("RULE_10", "condition: \"( ULCNuserClass = 'staff')\", action: \"(preferredName)\", destination: \"CDEfirstName\"");
			assertEquals( "Rule not parsed correctly", true, testRule.isValid() );
			assertEquals( "Destination not parsed correctly", "CDEfirstName", testRule.getDestination() );
			assertEquals( 2, testRule.requires().numberOfAttributes() );
		} catch (Exception e) {
			fail( e.getClass().getName() + ": " + e.getMessage() );
		}
	}
}
