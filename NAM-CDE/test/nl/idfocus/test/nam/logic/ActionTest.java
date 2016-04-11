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
import nl.idfocus.nam.logic.action.Action;
import nl.idfocus.nam.logic.action.ActionConcat;
import nl.idfocus.nam.logic.action.ActionDnLookup;
import nl.idfocus.nam.logic.action.ActionFilter;
import nl.idfocus.nam.logic.action.ActionJoin;
import nl.idfocus.nam.logic.action.ActionLength;
import nl.idfocus.nam.logic.action.ActionLower;
import nl.idfocus.nam.logic.action.ActionParser;
import nl.idfocus.nam.logic.action.ActionReplace;
import nl.idfocus.nam.logic.action.ActionSHA256;
import nl.idfocus.nam.logic.action.ActionSplit;
import nl.idfocus.nam.logic.action.ActionSubstring;
import nl.idfocus.nam.logic.action.ActionSubstringAfter;
import nl.idfocus.nam.logic.action.ActionSubstringBefore;
import nl.idfocus.nam.logic.action.ActionTrim;
import nl.idfocus.nam.logic.action.ActionUpper;
import nl.idfocus.nam.logic.data.DataResponse;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ActionTest 
{
	DataResponse response;

	@Before
	public void setUp() throws Exception 
	{
		response = new DataResponse();
		response.addAttributes( TestData.getPrimaryAttributes() );
		response.addAttributes( "cn=myManager", TestData.getManagerAttributes() );
		response.addAttributes( "cn=group1", TestData.getGroup1Attributes() );
		response.addAttributes( "cn=group2", TestData.getGroup2Attributes() );
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void BasicActionConcatTest() 
	{
		try {
			ActionParser parser = new ActionParser("concat( cn, '+', sn )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionConcat );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "myUsername+myLastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionConcatTest() 
	{
		try {
			ActionParser parser = new ActionParser("concat( concat( cn, givenname ), sn )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionConcat );
			assertEquals( "Number of attributes: ", 3, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "myUsernamemyFirstnamemyLastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionDNLookupTest() 
	{
		try {
			ActionParser parser = new ActionParser("dn( manager, sn )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionDnLookup );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "managerLastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionDNLookupMultivalueTest() 
	{
		try {
			ActionParser parser = new ActionParser("dn( manager, location )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionDnLookup );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Concat result: ", new String[]{ "managerLocation1", "managerLocation2", "managerLocation3" } , action.perform(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionLengthTest() 
	{
		try {
			ActionParser parser = new ActionParser("length( sn )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionLength );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "10", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionSubstringAfterTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring-after( manager, '=' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstringAfter );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "myManager", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void ComplexActionSubstringAfterTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring-after( ULCNuserOrgLevel2, concat( ULCNp1, '#' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstringAfter );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			String[] result = action.perform(response);
			assertEquals( "Number of values: ", 1, result.length );
			assertEquals( "Concat result: ", "50001469", result[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionSubstringAfterTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring-after( filter( ULCNuserOrgLevel2, concat( ULCNp1, '#' ) ), concat( ULCNp1, '#' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstringAfter );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			String[] result = action.perform(response);
			assertEquals( "Number of values: ", 1, result.length );
			assertEquals( "Concat result: ", "50001469", result[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionSubstringBeforeTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring-before( manager, '=' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstringBefore );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "cn", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionSubstringTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring( manager, '0', '3' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstring );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "cn=", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionSubstringLengthTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring( manager, '0', length( sn ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstring );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "cn=myManag", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionSubstringTest() 
	{
		try {
			ActionParser parser = new ActionParser("substring-before( substring-after( manager, '=' ), 'M' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSubstringBefore );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "my", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionReplaceTest() 
	{
		try {
			ActionParser parser = new ActionParser("replace( cn, 'my', 'Success' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionReplace );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Replace result:", "SuccessUsername", action.perform(response)[0] ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionReplaceTest() 
	{
		try {
			ActionParser parser = new ActionParser("replace( replace( cn, 'U', 'a' ), 'a', 'b')");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionReplace );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Replace result:", "mybsernbme", action.perform(response)[0] ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionJoinTest() 
	{
		try {
			ActionParser parser = new ActionParser("join( memberof, '#' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionJoin );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "cn=group1#cn=group2", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionJoinTest() 
	{
		try {
			ActionParser parser = new ActionParser("join( dn( manager, location ) , '#' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionJoin );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "managerLocation1#managerLocation2#managerLocation3", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionSplitTest() 
	{
		try {
			ActionParser parser = new ActionParser("split( manager, '=' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSplit );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "cn", action.perform(response)[0] );
			assertEquals( "Concat result: ", "myManager", action.perform(response)[1] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionSplitTest() 
	{
		try {
			ActionParser parser = new ActionParser("split( dn( manager, services ), '|' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSplit );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "portal", action.perform(response)[0] );
			assertEquals( "Concat result: ", "hr", action.perform(response)[1] );
			assertEquals( "Concat result: ", "hours", action.perform(response)[2] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionJoinSplitTest() 
	{
		try {
			ActionParser parser = new ActionParser("join( split( dn( manager, services ), '|' ), '#')");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionJoin );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Concat result: ", "portal#hr#hours", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionTrimTest() 
	{
		try {
			ActionParser parser = new ActionParser("trim( cn )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionTrim );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Trim result:", "myUsername", action.perform(response)[0] ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionFilterNoneTest() 
	{
		try {
			ActionParser parser = new ActionParser("filter( memberof, 'group' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionFilter );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Filter result:", new String[]{"cn=group1", "cn=group2" }, action.perform(response) ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionFilterOneTest() 
	{
		try {
			ActionParser parser = new ActionParser("filter( memberof, '^cn=.*1$' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionFilter );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Filter result:", new String[]{ "cn=group1" }, action.perform(response) ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionFilterDoubleTest() 
	{
		try {
			ActionParser parser = new ActionParser("filter( memberof, '1$', '2$' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionFilter );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Filter result:", new String[]{"cn=group1", "cn=group2" }, action.perform(response) ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionFilterDoubleTest() 
	{
		try {
			ActionParser parser = new ActionParser("filter( dn( manager, 'location' ) , '1$', '2$' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionFilter );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Filter result:", new String[]{"managerLocation1", "managerLocation2" }, action.perform(response) ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicActionSha256Test() 
	{
		try {
			ActionParser parser = new ActionParser("sha256( cn, 'mysaltvalue' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionSHA256 );
			assertEquals( "Number of attributes: ", 1, parser.getRequiredData().numberOfAttributes() );
			assertArrayEquals( "Sha256 result:", new String[]{ "mysaltvalue:467bf49e0ec2650cdf3ee4e00e5ebf9a13e9656e226ce7294851727d0fee28cd" }, action.perform(response) ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void ConcatActionTrimTest() 
	{
		try {
			ActionParser parser = new ActionParser("trim( concat( cn, sn, ' ' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionTrim );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Trim result:", "myUsernamemyLastname", action.perform(response)[0] ); 
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void LiteralActionTrimTest() 
	{
		try {
			ActionParser parser = new ActionParser("trim( concat( ' a', 'b ' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionTrim );
			assertEquals( "Number of attributes: ", 0, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "ab", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundActionTrimTest() 
	{
		try {
			ActionParser parser = new ActionParser("trim( concat( ' a', trim( cn ), ' ' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type:", true, action instanceof ActionTrim );
			assertEquals( "Number of attributes:", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result:", "amyUsername", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundDNActionTrimTest() 
	{
		try {
			ActionParser parser = new ActionParser("trim( concat( ' a', dn( manager, sn ), ' ' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionTrim );
			assertEquals( "Number of attributes:", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Number of dn lookups:", 1, parser.getRequiredData().numberOfAttributes( "manager" ) );
			assertEquals( "Action result: ", "amanagerLastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundDNActionFilterJoinTest() 
	{
		try {
			ActionParser parser = new ActionParser("join( filter( dn( memberof, samaccountname ), '^grou' ), '|' )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionJoin );
			assertEquals( "Number of attributes:", 1, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Number of dn lookups:", 1, parser.getRequiredData().numberOfAttributes( "memberof" ) );
			assertEquals( "Action result: ", "group1|group2", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void LiteralActionLowerTest() 
	{
		try {
			ActionParser parser = new ActionParser("lower( concat( 'A', 'b' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionLower );
			assertEquals( "Number of attributes: ", 0, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "ab", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void LiteralActionUpperTest() 
	{
		try {
			ActionParser parser = new ActionParser("upper( concat( 'A', 'b' ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionUpper );
			assertEquals( "Number of attributes: ", 0, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "AB", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void AttributeActionLowerTest() 
	{
		try {
			ActionParser parser = new ActionParser("lower( concat( givenname, ' ', sn ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionLower );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "myfirstname mylastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void AttributeActionUpperTest() 
	{
		try {
			ActionParser parser = new ActionParser("upper( concat( givenname, ' ', sn ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionUpper );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "MYFIRSTNAME MYLASTNAME", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void CompoundAttributeActionLowerTest() 
	{
		try {
			ActionParser parser = new ActionParser("lower( concat( lower( givenname ), ' ', lower( sn ) ) )");
			Action action = parser.getAction();
			assertEquals( "Action type: ", true, action instanceof ActionLower );
			assertEquals( "Number of attributes: ", 2, parser.getRequiredData().numberOfAttributes() );
			assertEquals( "Action result: ", "myfirstname mylastname", action.perform(response)[0] );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void FailActionTrailingTest() 
	{
		try {
			ActionParser parser = new ActionParser("concat( cn, '+', sn ) as");
			@SuppressWarnings("unused")
			Action action = parser.getAction();
			fail( "Action: exception expected but none thrown" );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Action: Trailing characters found after close at position 21", e.getMessage() );
		}
	}

	@Test
	public void FailActionBracketTest() 
	{
		try {
			ActionParser parser = new ActionParser("concat( cn, '+', sn ");
			@SuppressWarnings("unused")
			Action action = parser.getAction();
			fail( "Action: exception expected but none thrown" );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Action: Missing closing bracket, parsing incomplete", e.getMessage() );
		}
	}

	@Test
	public void FailActionQuoteTest() 
	{
		try {
			ActionParser parser = new ActionParser("concat( cn, '+', 'sn )");
			@SuppressWarnings("unused")
			Action action = parser.getAction();
			fail( "Action: exception expected but none thrown" );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Action: Missing closing quote, parsing incomplete", e.getMessage() );
		}
	}
}
