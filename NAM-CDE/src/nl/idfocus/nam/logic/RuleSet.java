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
package nl.idfocus.nam.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idfocus.nam.logic.data.DataRequest;

/**
 * Class that holds a set of rule objects, its own hashcode for cache lookups, and the sum of the required data for all contained rules. 
 * @author mvreijn@idfocus.nl
 *
 */
public class RuleSet 
{
	private final String hashcode;
	private final List<BusinessRule> rules;
	private final DataRequest requires;

	public RuleSet() 
	{
		this( null );
	}

	public RuleSet( String hash )
	{
		this.hashcode = hash;
		this.rules = new ArrayList<BusinessRule>();
		this.requires = new DataRequest();
	}

	public void addRule( BusinessRule rule )
	{
		rules.add(rule);
		// Apply the default sort order for the ruleset
		Collections.sort( rules );
		this.addRequired( rule.requires() );
	}
	
	public void addRequired( DataRequest required )
	{
		requires.merge(required);
	}

	public List<BusinessRule> getRules()
	{
		return rules;
	}

	public DataRequest getRequires()
	{
		return requires;
	}
	
	public String getHash()
	{
		return this.hashcode;
	}
}
