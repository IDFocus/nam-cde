package nl.idfocus.nam.logic.data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DebugData implements Serializable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7945757512161191482L;

	public static final String DEBUG_TAG = "debugdata";
	private Map<String,String> rules;
	private DataRequest requiredData;
	private DataResponse userdata;
	private final Map<String,Boolean> applies;
	private final Map<String,String[]> results;

	public DebugData()
	{
		applies = new HashMap<String, Boolean>();
		results = new HashMap<String, String[]>();
		rules   = new HashMap<String, String>();
	}
	
	public Map<String,String> getRules()
	{
		return rules;
	}

	public void addRule( String ruleName, String ruleContent ) 
	{
		this.rules.put(ruleName, ruleContent);
	}

	public Set<String> getRequired()
	{
		return this.requiredData.requiredAttributes();
	}

	public Set<String> getRequired( String dn )
	{
		return this.requiredData.requiredAttributes( dn );
	}

	public void setRequired(DataRequest requiredData) 
	{
		this.requiredData = requiredData;
	}

	public DataResponse getRetrieved()
	{
		return this.userdata;
	}

	public void setRetrieved(DataResponse userData) 
	{
		this.userdata = userData;
	}

	public boolean getApplies( String name )
	{
		return this.applies.containsKey(name) ? this.applies.get(name) : false;
	}

	public void setApplies( String name, boolean result )
	{
		applies.put( name, result );
	}

	public String[] getResult( String name )
	{
		return this.results.containsKey(name) ? this.results.get(name) : new String[]{};
	}

	public void setResult( String name, String[] result )
	{
		results.put(name, result);
	}
}
