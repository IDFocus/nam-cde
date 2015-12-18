package nl.idfocus.nam.logic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import nl.idfocus.nam.logic.data.DataRequest;

/**
 * Class that holds a set of rule objects, its own hashcode for cache lookups, and the sum of the required data for all contained rules. 
 * @author mvreijn
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
