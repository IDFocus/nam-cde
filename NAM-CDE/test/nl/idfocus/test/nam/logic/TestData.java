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

import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;
import javax.naming.directory.BasicAttributes;

/**
 * Just a simple class to centralize the testdata
 * @author mvreijn
 *
 */
public class TestData 
{
	static Attributes getPrimaryAttributes()
	{
		Attributes primaryAttrs = new BasicAttributes();
		primaryAttrs.put( "cn", "myUsername" );
		primaryAttrs.put( "sn", "myLastname" );
		primaryAttrs.put( "givenname", "myFirstname" );
		primaryAttrs.put( "manager", "cn=myManager" );
		Attribute orglevel = new BasicAttribute( "ULCNuserOrgLevel2" );
		orglevel.add("20008177#50001469");
		orglevel.add("20008178#50001830");
		primaryAttrs.put(orglevel);
		primaryAttrs.put( "ULCNp1", "20008177" );
		Attribute groups = new BasicAttribute( "memberof" );
		groups.add( "cn=group1" );
		groups.add( "cn=group2" );		
		primaryAttrs.put( groups );
		return primaryAttrs;
	}

	static Attribute getCAKEmployee()
	{
		return new BasicAttribute( "cakInternal", "true");
	}
	
	static Attribute getCAKPartner()
	{
		return new BasicAttribute( "cakInternal", "false");
	}
	
	static Attributes getManagerAttributes()
	{
		Attributes secondaryAttrs = new BasicAttributes();
		secondaryAttrs.put( "sn", "managerLastname" );
		secondaryAttrs.put( "services", "portal|hr|hours" );
		Attribute loc = new BasicAttribute( "location" );
		loc.add( "managerLocation1" );
		loc.add( "managerLocation2" );
		loc.add( "managerLocation3" );
		secondaryAttrs.put( loc );
		return secondaryAttrs;
	}

	static Attributes getGroup1Attributes()
	{
		Attributes secondaryAttrs = new BasicAttributes();
		secondaryAttrs.put( "description", "group1Description" );
		secondaryAttrs.put( "samaccountname", "group1" );
		return secondaryAttrs;
	}
	
	static Attributes getGroup2Attributes()
	{
		Attributes secondaryAttrs = new BasicAttributes();
		secondaryAttrs.put( "description", "group2Description" );
		secondaryAttrs.put( "samaccountname", "group2" );
		return secondaryAttrs;		
	}

}
