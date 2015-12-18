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
