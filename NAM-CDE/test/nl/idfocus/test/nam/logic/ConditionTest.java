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

import javax.naming.directory.Attribute;

import nl.idfocus.nam.logic.condition.Condition;
import nl.idfocus.nam.logic.condition.ConditionAnd;
import nl.idfocus.nam.logic.condition.ConditionExpression;
import nl.idfocus.nam.logic.condition.ConditionNot;
import nl.idfocus.nam.logic.condition.ConditionOr;
import nl.idfocus.nam.logic.condition.ConditionParser;
import nl.idfocus.nam.logic.condition.ConditionRegex;
import nl.idfocus.nam.logic.data.DataResponse;

import org.junit.Before;
import org.junit.Test;

public class ConditionTest 
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

	@Test
	public void InvalidConditionTrailingTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn == 'myUsername' ) as" );
			fail("No exception encountered: "+parser.getCondition().toString() );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Trailing characters found after close at position 22", e.getMessage() );
		}
	}

	@Test
	public void InvalidConditionQuoteTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn == 'myUsername )" );
			fail("No exception encountered: "+parser.getCondition().toString() );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Missing closing quote, parsing incomplete", e.getMessage() );
		}
	}

	@Test
	public void InvalidConditionBracketTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn == 'myUsername'" );
			fail("No exception encountered: "+parser.getCondition().toString() );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Missing closing bracket, parsing incomplete", e.getMessage() );
		}
	}

	@Test
	public void InvalidAndCondition2Test() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "\\and( ( cn > 'myuser' )( sn > 'mylast' ) )" );
			fail("No exception encountered: "+parser.getCondition().toString() );
		} catch (Exception e) {
			assertEquals( "Incorrect exception: ", "Syntax error at position 1: escape character outside of condition", e.getMessage() );
		}
	}

	@Test
	public void BasicConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn == 'myUsername' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicConditionReverseTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( 'myUsername' == cn )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicConditionLiteralsTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "not( 'a' == 'b' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionNot );
			assertEquals( "Number of attributes:", 0, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicConditionFailTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn == 'myusername' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", false, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicConditionIfTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "if( cn == 'myUsername' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicLaxConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn = 'myusername' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicGtConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn >= 'myUser' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicLaxGtConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( cn > 'myuser' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicConditionActionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( 'myUsername' = 'myusername' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 0, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicNotConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "not( ( cn > 'myuser' ) )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionNot );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", false, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicNotConditionSimplifiedTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "not( cn > 'myuser' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionNot );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", false, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicAndConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "and( ( cn > 'myuser' ) ( sn > 'mylast' ) )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionAnd );
			assertEquals( "Number of attributes:", 2, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicRegexConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "regex( cn, '^my.+$' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionRegex );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void DoubleRegexConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "regex( cn, '^my.+$', '.+User.*' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionRegex );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicLengthConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "if( length( cn ) > '2' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			e.printStackTrace();
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicAndCondition2Test() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "and( ( cn > 'myuser' )( sn > 'mylast' ) )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionAnd );
			assertEquals( "Number of attributes:", 2, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicOrConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "or( ( cn > 'myuser' ), ( sn > 'myuser' ) )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionOr );
			assertEquals( "Number of attributes:", 2, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void BasicOrCondition2Test() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "or( ( cn > 'myuser' ) ( sn > 'myuser' ) )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionOr );
			assertEquals( "Number of attributes:", 2, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void DNLookupConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( dn( manager, sn ) == 'managerLastname' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Number of DN attributes:", 1, parser.getAttributes().numberOfAttributes( "manager" ) );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void DNLookupConditionFailTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( dn( manager, givenname ) == 'managerFirstname' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Number of DN attributes:", 1, parser.getAttributes().numberOfAttributes( "manager" ) );
			assertEquals( "Condition result:", false, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void DNLookupMultiValued1ConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( dn( manager, location ) = 'managerLocation1' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Number of DN attributes:", 1, parser.getAttributes().numberOfAttributes( "manager" ) );
			Attribute attr = response.getRelatedAttribute( "cn=myManager", "sn" );
			assertEquals( "Attribute value:", "managerLastname", (String)attr.get() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

	@Test
	public void DNLookupMultiValued2ConditionTest() 
	{
		try
		{
			ConditionParser parser = new ConditionParser( "( dn( manager, location ) = 'managerLocation2' )" );
			Condition cond = parser.getCondition();
			assertEquals( "Condition type: ", true, cond instanceof ConditionExpression );
			assertEquals( "Number of attributes:", 1, parser.getAttributes().numberOfAttributes() );
			assertEquals( "Number of DN attributes:", 1, parser.getAttributes().numberOfAttributes( "manager" ) );
			Attribute attr = response.getRelatedAttribute( "cn=myManager", "sn" );
			assertEquals( "Attribute value:", "managerLastname", (String)attr.get() );
			assertEquals( "Condition result:", true, cond.evaluate(response) );
		} catch (Exception e) {
			fail("Exception: "+e.getMessage() );
		}
	}

}
