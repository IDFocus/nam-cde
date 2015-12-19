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
package nl.idfocus.nam.authentication;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.NamingEnumeration;
import javax.naming.NamingException;
import javax.naming.directory.Attribute;
import javax.naming.directory.Attributes;
import javax.naming.directory.BasicAttribute;

import nl.idfocus.nam.logic.BusinessRule;
import nl.idfocus.nam.logic.RuleCache;
import nl.idfocus.nam.logic.RuleSet;
import nl.idfocus.nam.logic.data.DataRequest;
import nl.idfocus.nam.logic.data.DataResponse;
import nl.idfocus.nam.logic.data.DebugData;
import nl.idfocus.nam.util.LDAPCache;
import nl.idfocus.nam.util.LDAPCache.LDAPCacheException;
import nl.idfocus.nam.util.LogFormatter;

import com.novell.nidp.NIDPConstants;
import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.NIDPSubject;
import com.novell.nidp.authentication.AuthnConstants;
import com.novell.nidp.authentication.local.LocalAuthenticationClass;
import com.novell.nidp.authentication.local.PageToShow;
import com.novell.nidp.common.authority.UserAuthority;
import com.novell.nidp.liberty.idff.protocol.LibertyAuthnRequest;

/**
 * @author IDFocus B.V. (mvreijn@idfocus.nl)
 * @version Tested on NetIQ Access Manager 4.0.x and 4.1.x
 */
public class ConditionalData extends LocalAuthenticationClass
{
	// Logging
	private static final Logger logger = LogFormatter.getConsoleLogger( ConditionalData.class.getName() );
	private static final Level loglevel = Level.INFO;
	private static final Level dbglevel = Level.FINE;
	private static final Level errlevel = Level.SEVERE;
	// Variables
	private NIDPPrincipal local_Principal;
	private final String sessionUser;
	private final RuleSet ruleSet;
	// finals
	/**
	 * By setting this property name on the class or method, the debug mode may be enabled. 
	 */
	private final String DEBUG       = "DEBUG";
	private final String DEFAULTJSP  = "rule-debug";
	private final String RECONFIGURE = "Reconfigure";
	private final String DEFINESUSER = "DefinesUser";
	private final String AUTHNREQ    = "AuthnRequest";
	private final String SYSTEM      = "SystemAccess";
	private final String lastChangedRevision = "$LastChangedRevision: 54 $";
	private final String revision;
	private LDAPCache lcache;
	private final boolean debugmode;
	private final boolean definesUser;

	/**
	 * Instantiate the ConditionalData authentication class. <br/>
	 * The IDP executes the constructor in two ways: 
	 * <ul>
	 * <li>after applying the configuration (this is for checking the input, lacking dynamic properties)</li>
	 * <li>at authentication time (with specific dynamic properties) at which point the rulecache is finalized</li>
	 * </ul>
	 * @param props the {@link Properties} object for this class, will contain strings and objects (!)
	 * @param stores the list of user stores
	 */
	public ConditionalData( Properties props, ArrayList<UserAuthority> stores )
	{
		super( props, stores );
		this.revision = lastChangedRevision.substring( lastChangedRevision.indexOf(":")+1, lastChangedRevision.lastIndexOf("$") ).trim();
		logger.log( loglevel, "ConditionalData Authentication Class rev "+revision+" (c) IDFocus B.V. <info@idfocus.nl>" );
	    long start = System.nanoTime();
		this.debugmode = Boolean.parseBoolean( props.getProperty( DEBUG ) );
		if ( debugmode )
		{
			for ( Handler hd : logger.getHandlers() )
				hd.setLevel( dbglevel );
			logger.setLevel( dbglevel );
			logger.log( dbglevel, "$Id: ConditionalData.java 54 2015-12-18 22:01:06Z mvreijn $" );
		}
		// 
	    StringBuilder keyHash = new StringBuilder();
		// Read setup properties
		for ( Object oKey : props.keySet() )
		{
			String key = (String) oKey;
			logger.log( dbglevel, String.format("Found key %s with value %s.", key, props.getProperty(key)) );
			if ( key.equals( RECONFIGURE ) )
			{
				logger.log( loglevel, String.format("Reading reconfigure setting: %s", props.getProperty(key)) );
				if( Boolean.parseBoolean( props.getProperty(key) ) )
				{
					logger.log( dbglevel, "Clearing rule cache" );			
					RuleCache.clear();
				}
			}
			else if ( key.equals( AUTHNREQ ) )
			{
				Object value = props.get(key);
				logger.log( loglevel, String.format("Reading Authentication Request: %savailable.", (value == null ? "un" : "" ) ) );
				if ( value != null )
				{
					logger.log( dbglevel, String.format( "Authentication Request of class %s contains %s", value.getClass().getName(), value ) );
					if ( value instanceof LibertyAuthnRequest )
					{
						keyHash.append( ((LibertyAuthnRequest)value).getContractURI() );
					}
				}
			}
			else if ( key.equals( SYSTEM ) )
			{
				Object value = props.get(key);
				logger.log( loglevel, String.format("Reading System Access: %savailable.", (value == null ? "un" : "" ) ) );
				if ( value != null )
				{
					logger.log( dbglevel, String.format( "System Access of class %s contains %s", value.getClass().getName(), value ) );
				}
			}
			else if ( key.startsWith( BusinessRule.PREFIX ) )
			{
				keyHash.append(key);
				keyHash.append(props.getProperty(key));
			}
		}
		// Read definesUser setting
		logger.log( dbglevel, String.format( "Reading user identification setting: %s", props.getProperty( DEFINESUSER ) ) );
		this.definesUser = Boolean.parseBoolean( props.getProperty( DEFINESUSER, "false" ) );
		/* 
		 * FIXME 
		 * It is possible to have duplicate hash values because unique rule naming is not enforced.
		 * A hash contains the contract uri, but using the same rule names within one contract will still lead to clashes. 
		 * Imagine the following rulesets within the same contract: 
		 *  classA: RULE_1
		 * 	methodA: RULE_2
		 *  classB: RULE_1
		 * 	methodB: RULE_2
		 * The best solution would be to incorporate class and method names but these do not seem to be accessible.
		 */
		// Build SHA256 hash of the keyHash sb and compare to cached versions
		String ruleSetHash = createHashValue( keyHash.toString() );
		if ( RuleCache.contains( ruleSetHash ) )
		{
			logger.log( dbglevel, String.format( "Key %s found in cache, retrieving cached ruleset", ruleSetHash ) );			
			this.ruleSet = RuleCache.getRules( ruleSetHash );
		}
		else
		{
			logger.log( dbglevel, String.format( "Key %s not found in cache, parsing rules into ruleset", ruleSetHash ) );			
			this.ruleSet = new RuleSet( ruleSetHash );
			for ( Object oKey : props.keySet() )
			{
				String key = (String) oKey;
				if ( key.startsWith( BusinessRule.PREFIX ) )
				{
					logger.log( dbglevel, "Reading rule "+key+": "+props.getProperty(key));
					try {
						ruleSet.addRule( new BusinessRule( key , props.getProperty(key) ) );
					} catch (Exception e) {
						logger.log( errlevel, "Exception adding rule "+key+": "+e.getMessage() );
					}
				}
			}
			RuleCache.storeRules( ruleSetHash, ruleSet );			
		}
		long next = System.nanoTime();
		sessionUser = getProperty("findSessionUser");
		logger.log( loglevel, String.format( "Initialized ConditionalData in %s millis", ((next - start)/1000000L) ) );			
	}

	/**
	 * Create the hexadecimal string representation of a SHA-256 hash value from the given input String. 
	 * @param input String value
	 * @return hexadecimal String of the hash
	 */
	private String createHashValue(String input) 
	{
		try 
		{
			MessageDigest md = MessageDigest.getInstance( "SHA-256" );
			md.update( input.getBytes( "UTF-8" ) );
			byte[] hash = md.digest();
			StringBuffer hexString = new StringBuffer();

			for (int i = 0; i < hash.length; i++) {
				String hex = Integer.toHexString(0xff & hash[i]);
				if(hex.length() == 1) hexString.append('0');
				hexString.append(hex);
			}

			return hexString.toString();
		} catch (NoSuchAlgorithmException e) {
			// Unlikely
			logger.log( errlevel, "Exception creating SHA-256 hash: "+e.getMessage() );
		} catch (UnsupportedEncodingException e) {
			// Unlikely
			logger.log( errlevel, "Exception using UTF-8 encoding: "+e.getMessage() );
		}
		return null;
	}

	/**
	 * Return the authentication type of this class.
	 * @return String value from {@link AuthnConstants}
	 */
	@Override
	public String getType()
	{
		return AuthnConstants.OTHER;
	}

	/**
	 * implement doAuthenticate() in the following way:<br/>
	 * get principal's source attributes,
	 * process rules across principal's attributes,
	 * write results to destination attributes
	 * @return returns the status of the authentication process which is always AUTHENTICATED.<br/>
	 * Also saves data into the IDP LDAP cache.
	 */
	@Override
	protected int doAuthenticate()
	{
		logger.log( loglevel, "Evaluating Conditional Data rules" );
	    long start = System.nanoTime();

	    if ( lcache == null )
	    {
	    	try {
				lcache = new LDAPCache( m_Session );
			} catch (LDAPCacheException e) {
				logger.log( errlevel, String.format( "Error while accessing LDAP cache: %s, stopping rule execution flow.", e.getMessage() ) );
				e.printStackTrace();
				return AUTHENTICATED;
			}
	    }
	    // Allow debugmode to be inherited by cache mechanism
	    if ( debugmode )
	    	lcache.setLoglevel(dbglevel);

		// Get Principal.
		local_Principal = resolveUserPrincipal();

		// Get required data
		DataResponse userData = getRequiredData( ruleSet.getRequires() );
		
		// Collect debug data
		DebugData data = new DebugData();

		// Now process all rules
		for ( BusinessRule rule : ruleSet.getRules() )
		{
			logger.log( dbglevel, String.format( "Processing rule %s with data requirement: \n%s", rule.getName(), rule.requires() ) );
			if ( debugmode )
				data.addRule( rule.getName(), rule.toString() );
			if ( rule.applies( userData ) )
			{
				logger.log( dbglevel, "Rule applies" );
				// Do our stuff here
				String[] result = rule.getResult( userData );
				logger.log( dbglevel, String.format( "Storing rule result as %s with content %s.", rule.getDestination(), Arrays.toString(result) ) );
				if ( result.length == 1 )
					lcache.setValue( rule.getDestination(), result[0] );
				else
					lcache.setValue( rule.getDestination(), result );
				if ( debugmode )
				{
					data.setApplies( rule.getName() , true );
					data.setResult( rule.getName(), result );
				}
				// Update the dataresponse with the new attribute
				updateRequiredData( ruleSet.getRequires(), userData, rule, result );
			}
			else
			{
				if ( debugmode )
					data.setApplies( rule.getName() , false );
			}
		}
		/* 
		 * If the class does not define the user, use the old cache set method which stopped working in NAM 4.1
		 * Otherwise, add cache entries to the credentials and set the principal.
		 */
		if ( definesUser )
		{
			lcache.persist( m_Credentials );
			setPrincipal(local_Principal);
		} 
		else
		{
			lcache.persist();
		}
		// We're done processing.
		long next = System.nanoTime();
		logger.log( loglevel, String.format("Processed ruleset in %s millis.", ((next - start)/1000000L)) );
		// Check debug setting
		if ( isFirstCallAfterPrevMethod() && debugmode )
		{
			// Show the DEFAULTJSP with all rules and data
			data.setRequired( ruleSet.getRequires() );
			data.setRetrieved( userData );
			// prepare the actual DEFAULTJSP page
			m_PageToShow = new PageToShow( DEFAULTJSP );
			m_PageToShow.addAttribute( NIDPConstants.ATTR_URL, ( getReturnURL() != null ? getReturnURL() : m_Request.getRequestURL().toString() ) );
			m_PageToShow.addAttribute( DebugData.DEBUG_TAG, data );
			
			logger.log( loglevel, "Conditional Data showing DEBUG page.");
			return SHOW_JSP;
		}
		logger.log( loglevel, "Conditional Data done.");
		return AUTHENTICATED;
	}

	/**
	 * Find out if the current ruleset requires the rule destination attribute. <br/>
	 * If so, add to the DataResponse object for further processing. 
	 * @param req the original datarequest from the ruleset
	 * @param resp the dataresponse to update
	 * @param rule the rule whose results to add
	 * @param values the result values
	 */
	private void updateRequiredData( DataRequest req, DataResponse resp, BusinessRule rule, String[] values )
	{
		if ( req.requiredAttributes().contains( rule.getDestination() ) )
		{
			logger.log( dbglevel, String.format( "Updating dataresponse with %s values for %s.", values.length, rule.getDestination() ) );
			Attribute attr = new BasicAttribute( rule.getDestination() );
			for ( String value : values )
				attr.add(value);
			resp.addAttribute( attr );
		}
	}

	private DataResponse getRequiredData( DataRequest data )
	{
		DataResponse response = new DataResponse();
		// Get attributes for primary principal
		Attributes primaryAttrs = getAttrvalues( local_Principal.getUserIdentifier(), data.requiredAttributes() );
		response.addAttributes( primaryAttrs );
		// For each requested secondary DN, retrieve the attributes
		for ( String attrName : data )
		{
			if ( primaryAttrs.get(attrName) != null )
			{
				try
				{
					NamingEnumeration<?> dnValues = primaryAttrs.get(attrName).getAll();
					while ( dnValues.hasMore() )
					{
						String dnValue = (String) dnValues.next();
						Attributes secondaryAttrs = getAttrvalues( dnValue, data.requiredAttributes(attrName) );
						response.addAttributes( dnValue, secondaryAttrs );
					}
				}
				catch (NamingException e) 
				{
					logger.log( errlevel, String.format( "Error %s while retrieving values for %s DN value(s).", e.getExplanation(), attrName ) );
				}
			}
		}
		return response;
	}

	private Attributes getAttrvalues( String dnValue, Set<String> attrValues ) 
	{
		String[] attributes = attrValues.toArray( new String[ attrValues.size() ] );
		try {
			UserAuthority ua = local_Principal.getAuthority();
			logger.log( dbglevel, String.format("getting principal attributeset for %s object", dnValue ));
			NIDPPrincipal princ = ua.getPrincipalByUniqueName( dnValue, getCredentials() );
			Attributes attrs = ua.getAttributes( princ , attributes );
			logger.log( dbglevel, "returning attribute set");
			return attrs;
		} catch (Exception e) {
			logger.log( errlevel, String.format( "Exception '%s' encountered while retrieving attributes.", e.getMessage() ) );
		}
		return null;
	}

    private NIDPPrincipal resolveUserPrincipal()
    {
        logger.log( dbglevel, "getting principal from properties (contract)");
        NIDPPrincipal nidpprincipal = (NIDPPrincipal) m_Properties.get("Principal");

        if ( nidpprincipal == null )
        {
        	logger.log( dbglevel, "getting user from session");
            if(sessionUser != null)
            {
                if( m_Session.isAuthenticated() )
                {
                    NIDPSubject nidpsubject = m_Session.getSubject();
                    NIDPPrincipal anidpprincipal[] = nidpsubject.getPrincipals();
                    if(anidpprincipal.length == 1)
                    {
                        nidpprincipal = anidpprincipal[0];
                        logger.log( dbglevel, ( new StringBuilder() ).append("principal retrieved from authenticated session ").append( nidpprincipal.getUserIdentifier() ).toString() );
                        setPrincipal(nidpprincipal);
                    }
                }
                if(nidpprincipal == null)
                	logger.log( dbglevel, "no principal in session");
            }
        }
        else
        {
        	logger.log( dbglevel, (new StringBuilder()).append("retrieved principal from properties ").append(nidpprincipal.getUserIdentifier()).toString());
            setPrincipal(nidpprincipal);
        }
        return nidpprincipal;
    }
}
