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

import java.util.HashMap;
import java.util.Map;

public class RuleCache 
{
	private static final Map<String,RuleSet> ruleSets;

	/**
	 * A protected constructor ensures no direct instantiation is possible
	 */
	protected RuleCache() {}

	/**
	 * Static initialization to create the data store on first class access
	 */
	static
	{
		ruleSets = new HashMap<String, RuleSet>();
	}

	public static void storeRules( String key, RuleSet newrules )
	{
		synchronized( ruleSets )
		{
			ruleSets.put( key, newrules );
		}
	}
	
	public static void clear()
	{
		synchronized ( ruleSets ) 
		{
			ruleSets.clear();			
		}
	}

	public static RuleSet getRules( String key )
	{
		return ruleSets.get(key);
	}

	public static boolean contains( String key ) 
	{
		return ruleSets.containsKey(key);
	}

}
