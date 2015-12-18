package nl.idfocus.nam.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.novell.nidp.NIDPPrincipal;
import com.novell.nidp.NIDPSession;
import com.novell.nidp.common.profile.AttributeProfile;
import com.novell.nidp.common.util.GetLDAPAttributeUniqueToken;
import com.novell.nidp.common.xml.w3c.XMLBase;
import com.novell.nidp.liberty.wsc.WSC;
import com.novell.nidp.liberty.wsc.cache.WSCCacheEntry;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushed;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCache;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedCacheSet;
import com.novell.nidp.liberty.wsc.cache.pushed.WSCCachePushedQueryCallback;
import com.novell.nidp.liberty.wsc.query.WSCQLDAPToken;
import com.novell.nidp.liberty.wsc.query.WSCQSSToken;
import com.novell.nidp.liberty.wsf.idsis.ldapservice.schema.LDAPUserAttribute;
import com.novell.nidp.liberty.wsf.idsis.ssservice.schema.SSSecretEntry;
import com.novell.nidp.logging.NIDPLog;

public class LDAPCache 
{
	public static final String LDAP_CREDENTIALS_SETNAME = "LdapCredentialsSet";
	public static final String X509_CREDENTIALS_SETNAME = "X509CredentialsSet";

	private static final Logger logger   = LogFormatter.getConsoleLogger( LDAPCache.class.getName() );
	private static final Level  dbglevel = Level.FINE;
	private static final Level  loglevel = Level.INFO;

	NIDPSession nidpSession = null;
	List<WSCCacheEntry> entries;
	String setName;
	WSCCachePushed cachePushed;

	public LDAPCache( NIDPSession nidpSession, String setName ) throws LDAPCacheException
	{
		this( nidpSession );
		this.setName = setName;
	}

	public LDAPCache( NIDPSession nidpSession ) throws LDAPCacheException
	{
		this.nidpSession = nidpSession;
		if (this.nidpSession == null)
			throw new LDAPCacheException("NIDPSession can't be NULL");
		logger.log( dbglevel, "Cache enabled: "+WSC.getConfiguration().getCacheEnabled() );
		this.entries = new ArrayList<WSCCacheEntry>();
		this.cachePushed = WSCCachePushed.getInstance();
		this.setName = null;
	}

	public void setLoglevel(Level loglevel) 
	{
		for ( Handler hd : logger.getHandlers() )
			hd.setLevel( loglevel );
		logger.setLevel( loglevel );
	}

	public Map<String,String> getValues(String[] attributeNames)
	{
		HashMap<String,String> nameValuePairs = new HashMap<String,String>();

		for (String attributeName : attributeNames)
		{
			nameValuePairs.put(attributeName, getValue(attributeName));
		}
		return nameValuePairs;
	}

	public String getValue(String attribute)
	{
		if (this.nidpSession != null)
		{
			AttributeProfile profile = new AttributeProfile(this.nidpSession, null);
			profile.addLookupAttribute(GetLDAPAttributeUniqueToken.getUniqueToken(attribute));
			profile.lookup();
			return profile.getValue(GetLDAPAttributeUniqueToken.getUniqueToken(attribute));
		}

		if (NIDPLog.isLoggableAppSevere()) {
			logger.log(Level.SEVERE, "NDIPSession is NULL, Failed read user attributes from user attributes cache. Do pass proper nidpsession with LDAPAttributesCache constructor.");
		}
		return null;
	}

	public void setValues(HashMap<String, String> attributes)
	{
		for ( Map.Entry<String,String> attribute : attributes.entrySet() )
			setValue( (String)attribute.getKey(), (String)attribute.getValue() );
	}

	public void setValue(String name, String value)
	{
		logger.log(dbglevel, "Get cache for "+this.nidpSession.getID());
		WSCCachePushedCache cache = cachePushed.getCache(this.nidpSession.getID());

		LDAPUserAttribute attribute1 = new LDAPUserAttribute(name);
		String attrCacheTokenID = WSCQLDAPToken.buildUniqueId(attribute1, WSCQLDAPToken.class);

		WSCCacheEntry entry = null;
		if ((cache != null) && (name != null)) 
		{
			logger.log(dbglevel, "Find entry");
			entry = cache.findEntry(attrCacheTokenID);
			if (entry != null)
			{
				logger.log(dbglevel, "Modify existing entry: "+entry.getId() );
				entry.setDataItemValue(value);
				logger.log(dbglevel, "New value: "+entry.toString(1) );
			}
			else 
			{
				logger.log(dbglevel, "Create new entry");
				entry = new WSCCacheEntry(null, attrCacheTokenID, new XMLBase[0]);
				entry.setDataItemValue(value);
				logger.log(dbglevel, "Value: "+entry.toString(1) );
			}
		}
		else 
		{
			logger.log(dbglevel, "Add new entry to credentials list");
			entry = new WSCCacheEntry("", attrCacheTokenID, new XMLBase[0]);
			entry.setDataItemValue(value);
		}
		entries.add(entry);
	}

	public void setValue(String name, String[] value)
	{
		logger.log(dbglevel, "Get cache for "+this.nidpSession.getID());
		WSCCachePushedCache cache = cachePushed.getCache(this.nidpSession.getID());

		LDAPUserAttribute attribute1 = new LDAPUserAttribute(name);
		String attrCacheTokenID = WSCQLDAPToken.buildUniqueId(attribute1, WSCQLDAPToken.class);

		WSCCacheEntry entry = null;
		if ((cache != null) && (name != null)) 
		{
			logger.log(dbglevel, "Find entry");
			entry = cache.findEntry(attrCacheTokenID);
			if (entry != null)
			{
				logger.log(dbglevel, "Modify existing entry: "+entry.getId() );
				entry.setDataItemValue(value);
				logger.log(dbglevel, "New value: "+entry.toString(1) );
			}
			else 
			{
				logger.log(dbglevel, "Create new entry");
				entry = new WSCCacheEntry(null, attrCacheTokenID, new XMLBase[0]);
				entry.setDataItemValue(value);
				logger.log(dbglevel, "Value: "+entry.toString(1) );
			}
		}
		else 
		{
			logger.log(dbglevel, "Add new entry to credentials list");
			entry = new WSCCacheEntry("", attrCacheTokenID, new XMLBase[0]);
			entry.setDataItemValue(value);
		}
		entries.add(entry);
	}

	public void persist()
	{
		if ( entries == null )
			return;
		logger.log(dbglevel, String.format( "Persist %s cache entries in new cache set", entries.size() ) );
		if ( entries.size() == 0 )
			return;
		WSCCacheEntry[] localEntries = entries.toArray(new WSCCacheEntry[entries.size()]);
		WSCCachePushedCacheSet set = new WSCCachePushedCacheSet(localEntries, (WSCCachePushedQueryCallback)null);
		if ( setName != null )
		{
			logger.log(dbglevel, String.format( "Add Id %s to set", setName ) );
			set.setId( setName );
		}
		boolean allowOverride = WSCCachePushedCache.ALLOW_OVERRIDE;
		synchronized (cachePushed)
		{
			WSCCachePushedCache cache = cachePushed.getCache( nidpSession.getID() );
			if ( cache == null )
			{
				logger.log(dbglevel, "Add to new cache");
				cache = new WSCCachePushedCache();
				allowOverride = WSCCachePushedCache.DO_NOT_ALLOW_OVERRIDE;
			}
			cache.add(set, allowOverride);
			logger.log(dbglevel, "Add cache");
			cachePushed.addCache( nidpSession.getID(), cache );
		}
	}

	public void persist( List<WSCCacheEntry> m_Credentials )
	{
		if ( entries == null || m_Credentials == null )
			return;
		logger.log(dbglevel, String.format( "Persist %s cache entries in credential cache", entries.size() ) );
		if ( entries.size() > 0 )
			m_Credentials.addAll(entries);
	}

	public class LDAPCacheException extends Exception
	{
		/**
		 * 
		 */
		private static final long serialVersionUID = 110753913608578710L;

		public LDAPCacheException( String msg ) 
		{
			super( msg );
		}
	}

	public static String getUserNameFromCache(NIDPPrincipal princ, Properties props)
	{
		String uid = WSCQSSToken.SS_SecretEntry_LDAPCredentials_UserName.getTokenUniqueId();
		AttributeProfile profile = new AttributeProfile(princ, props);
		profile.addLookupAttribute( uid );
		profile.lookup();
		return profile.getValue( uid );
	}

	public static String getUserNameFromList( List<WSCCacheEntry> creds )
	{
		logger.log( loglevel, String.format( "Search %s cache entries for LDAP username", creds.size() ) );
		String value = "";
		String uid = WSCQSSToken.SS_SecretEntry_LDAPCredentials_UserName.getTokenUniqueId();
		for ( WSCCacheEntry entry : creds )
		{
			logger.log( loglevel, String.format( "Found entry of class %s with value %s.", entry.getClass().getName(), entry.toStringBrief(0) ) );
			if ( entry.getTokenUniqueId().equals(uid) )
			{
				XMLBase rawValue = entry.getDataItemValue();
				if ( rawValue instanceof SSSecretEntry )
					value = ((SSSecretEntry)rawValue).getEntryValue().getText();
				break;
			}
		}
		return value;
	}
}
