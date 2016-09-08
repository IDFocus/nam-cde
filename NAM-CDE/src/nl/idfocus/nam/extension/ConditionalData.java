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
package nl.idfocus.nam.extension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.Attributes;
import javax.naming.directory.SearchControls;
import javax.naming.ldap.Control;
import javax.naming.ldap.InitialLdapContext;
import javax.naming.ldap.LdapContext;

import nl.idfocus.nam.logic.BusinessRule;
import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.util.LogFormatter;

import com.novell.nxpe.NxpeContextDataElement;
import com.novell.nxpe.NxpeException;
import com.novell.nxpe.NxpeInformationContext;
import com.novell.nxpe.NxpeParameter;
import com.novell.nxpe.NxpeParameterList;
import com.novell.nxpe.NxpeResponseContext;
import com.novell.nxpe.NxpeResult;

/**
 * Custom Attribute Extension for NetIQ Access Manager IDPs
 * @author IDFocus B.V. (mvreijn@idfocus.nl)
 * @version Tested on NetIQ Access Manager 4.0.x to 4.2.x
 */
public class ConditionalData implements NxpeContextDataElement
{
	// NxpeContextDataElement values
	private final String				strName;
	private final int					iEnumerativeValue;
	private final String				strParameter;
	private final String				version					= ConditionalData.class.getPackage()
			.getImplementationVersion();
	// Constants
	private static final String			DEFAULT_AUTHENTICATION	= "simple";

	// Parameter IDs
	private static final int			FIELD_DEBUG_ID			= 1;
	private static final int			FIELD_LDAP_SERVER_ID	= 10;
	private static final int			FIELD_IGNORE_LDAPERR_ID	= 11;
	private static final int			FIELD_USER_DN_ID		= 20;
	private static final int			FIELD_USER_PASS_ID		= 21;
	private static final int			FIELD_FIRST_RULE		= 30;
	private static final int			FIELD_LAST_RULE			= 99;

	// Parameter default values
	private static final String			FIELD_SERVER_DEFAULT	= "ldaps://localhost:636";

	private NxpeParameterList			configurationValues;

	// Parameter default values
	private String						ldapServer;
	private String						ldapAuthentication;
	private SearchControls				searchControls;
	private boolean						ignoreInvalidLdapHost;
	private boolean						debugMode;

	private final List<BusinessRule>	ruleSet;

	private static Logger				logger					= LogFormatter
			.getConsoleLogger(ConditionalData.class.getName());
	private Level						loglevel				= Level.INFO;
	private Level						dbglevel				= Level.FINE;

	public ConditionalData(String strName, int iEnumerativeValue, String strParameter)
			throws NxpeException
	{
		logger.log(loglevel,
				"ConditionalData Extension " + version + " (c) IDFocus B.V. <info@idfocus.nl>");

		this.strName = strName;
		this.iEnumerativeValue = iEnumerativeValue;
		this.strParameter = strParameter;
		this.ignoreInvalidLdapHost = false;
		this.ruleSet = new ArrayList<>();
	}

	/**
	 * Mandatory for interface
	 */
	@Override
	public int getEnumerativeValue()
	{
		return iEnumerativeValue;
	}

	/**
	 * Mandatory for interface
	 */
	@Override
	public String getName()
	{
		return strName;
	}

	/**
	 * Mandatory for interface
	 */
	@Override
	public String getParameter()
	{
		return strParameter;
	}

	@Override
	public void initialize(NxpeParameterList params) throws NxpeException
	{
		if ( params == null )
			throw new NxpeException("No parameters received upon initialization");
		this.configurationValues = params;
		// Read setup properties
		Iterator<NxpeParameter> itr = params.iterator();
		while (itr.hasNext())
		{
			NxpeParameter param = itr.next();
			logger.log(loglevel, "Parameter: " + param.getName() + " id: "
					+ param.getEnumerativeValue() + " value: " + param.getValue());
			if (param.getEnumerativeValue() >= FIELD_FIRST_RULE
					&& param.getEnumerativeValue() <= FIELD_LAST_RULE)
			{
				try
				{
					ruleSet.add(new BusinessRule(param.getName(), param.getValue()));
				}
				catch (Exception e)
				{
					logger.log(loglevel, e.getClass().getName() + " adding rule "
							+ param.getEnumerativeValue() + ": " + e.getMessage(), e);
				}
			}
			else if (param.getEnumerativeValue() == FIELD_DEBUG_ID)
			{
				this.debugMode = Boolean.parseBoolean(param.getValue());
			}
			else if (param.getEnumerativeValue() == FIELD_IGNORE_LDAPERR_ID)
			{
				this.ignoreInvalidLdapHost = Boolean.parseBoolean(param.getValue());
			}
		}

		if (debugMode)
			dbglevel = Level.INFO;

		ldapServer = FIELD_SERVER_DEFAULT;
		ldapAuthentication = DEFAULT_AUTHENTICATION;

		// setup search controls
		searchControls = new SearchControls();
		searchControls.setTimeLimit(0);
		searchControls.setReturningObjFlag(true);
		searchControls.setSearchScope(SearchControls.OBJECT_SCOPE);

	}

	@Override
	public Object getValue(NxpeInformationContext informationCtx, NxpeResponseContext responseCtx)
			throws NxpeException
	{
		logger.log(loglevel, "ConditionalData Attribute Extension " + version
				+ " (c) IDFocus B.V. <info@idfocus.nl>");

		// Get LDAP parameters for query
		logger.log(dbglevel, "Calling getLDAPUserDN");
		String strLDAPUserDN = getProperty(informationCtx, FIELD_USER_DN_ID, null);

		logger.log(dbglevel, "Calling setProviderURL");
		ldapServer = getProperty(informationCtx, FIELD_LDAP_SERVER_ID, FIELD_SERVER_DEFAULT);

		logger.log(dbglevel, "Calling getLdapPassword");
		String strPassword = getProperty(informationCtx, FIELD_USER_PASS_ID, null);

		// Get attributes for this principal by iterating over the rules
		DataRequest required = new DataRequest();
		for (BusinessRule rule : ruleSet)
		{
			logger.log(dbglevel, " ## querying rule: " + rule.getName() + " ##");
			logger.log(dbglevel, " ## rule requires " + rule.requires());
			required.merge(rule.requires());
		}
		logger.log(dbglevel, " ## total set of attributes: " + required);
		DataResponse attrs = getRequiredData(strLDAPUserDN, strPassword, required);
		// Apply rules, return all data
		// TODO find out which return types are supported
		List<String> results = new ArrayList<>();
		for (BusinessRule rule : ruleSet)
		{
			logger.log(dbglevel, " ## evaluating rule: " + rule.getName() + " ##");
			if (rule.applies(attrs))
			{
				logger.log(dbglevel, " ## rule applies ## ");
				// Do our stuff here
				results.addAll(Arrays.asList(rule.getResult(attrs)));
			}
		}
		logger.log(loglevel,
				"Rule returned " + results.size() + " value" + (results.size() == 1 ? "." : "s."));
		// TODO think about the best way to do this - throwing an exception
		// removes the attribute apparently
		return results.isEmpty() ? "" : results.get(0);
	}

	/**
	 * Retrieve a property from the informationcontext. If no value can be
	 * found, return the default given.
	 *
	 * @param informationContext
	 *
	 * @throws com.novell.nxpe.NxpeException
	 */
	private String getProperty(NxpeInformationContext informationContext, int fieldID,
			String defaultValue) throws NxpeException
	{
		NxpeParameter param;
		String returnString = null;
		Object value;

		if ((param = configurationValues.getParameter(fieldID)) != null)
		{
			if ((value = informationContext.getData(param)) != null)
			{
				if (value instanceof String)
				{
					returnString = (String) value;
				}
				else if (value instanceof String[] && ((String[]) value).length > 0)
				{
					returnString = ((String[]) value)[0];
				}
				else
				{
					returnString = defaultValue;
				}
			}
		}
		else
		{
			returnString = defaultValue;
		}
		logger.log(dbglevel, "Parameter " + fieldID + " from context: " + returnString);
		return returnString;
	}

	private DataResponse getRequiredData(String userdn, String password, DataRequest data)
			throws NxpeException
	{
		DataResponse response = new DataResponse();
		// Get attributes for primary principal
		Attributes primaryAttrs = getAttrValues(userdn, password, data.requiredAttributes());
		response.addAttributes(primaryAttrs);
		// For each requested secondary DN, retrieve the attributes
		for (String attrName : data)
		{
			if (primaryAttrs.get(attrName) != null)
			{
				try
				{
					String dnValue = (String) primaryAttrs.get(attrName).get();
					Attributes secondaryAttrs = getAttrValues(userdn, password, dnValue,
							data.requiredAttributes(attrName));
					response.addAttributes(attrName, secondaryAttrs);
				}
				catch (NamingException e)
				{
				}
			}
		}
		return response;
	}

	private Attributes getAttrValues(String userDn, String password, String targetDn,
			Set<String> attributes) throws NxpeException
	{
		String[] attrs = attributes.toArray(new String[attributes.size()]);
		LdapContext ldapContext = null;
		Attributes userAttrs = null;
		try
		{
			logger.log(dbglevel, "Calling newInitialLdapContext");
			ldapContext = getLdapConnection(userDn, password);

			logger.log(dbglevel, "Searching LDAP");
			userAttrs = ldapContext.getAttributes(targetDn, attrs);
			logger.log(dbglevel, "...search result contains " + userAttrs.size() + " attributes");
		}
		catch (NamingException e)
		{
			logger.log(loglevel, "NamingException retrieving attributes: " + e.getExplanation());
			if (!ignoreInvalidLdapHost)
				throw new NxpeException(NxpeResult.ErrorDataUnavailable, e);
		}
		finally
		{
			if (ldapContext != null)
			{
				try
				{
					ldapContext.close();
				}
				catch (NamingException e)
				{
					logger.log(dbglevel,
							"NamingException clsoing connection: " + e.getExplanation(), e);
				}
			}
		}
		return userAttrs;
	}

	private Attributes getAttrValues(String userDn, String password, Set<String> attributes)
			throws NxpeException
	{
		return this.getAttrValues(userDn, password, userDn, attributes);
	}

	/**
	 *
	 * @param principal
	 * @param credentials
	 *
	 * @return
	 *
	 * @throws javax.naming.NamingException
	 */
	private LdapContext getLdapConnection(String principal, String credentials)
			throws NamingException
	{
		Hashtable<String, String> environment = new Hashtable<>();
		LdapContext ldapContext;

		environment.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.ldap.LdapCtxFactory");
		environment.put(Context.PROVIDER_URL, ldapServer);

		if (ldapServer.startsWith("ldaps://"))
		{
			environment.put(Context.SECURITY_PROTOCOL, "ssl");
			environment.put("java.naming.ldap.factory.socket",
					"nl.idfocus.nam.util.CleanSocketFactory");
		}

		environment.put(Context.SECURITY_AUTHENTICATION, ldapAuthentication);
		environment.put(Context.SECURITY_PRINCIPAL, principal);
		environment.put(Context.SECURITY_CREDENTIALS, credentials);

		environment.put(Context.REFERRAL, "follow");

		ldapContext = new InitialLdapContext(environment, (Control[]) null);

		return ldapContext;

	}

}
