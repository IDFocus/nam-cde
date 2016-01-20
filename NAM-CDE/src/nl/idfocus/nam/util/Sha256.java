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
package nl.idfocus.nam.util;

import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.SecureRandom;

public class Sha256 
{

	/**
	 * Generate a Sha-256 has of a given string.
	 * @param base the string to hash
	 * @return a Sha-256 hash of the string
	 */
	public static String toHashString( String base ) 
	{
	    try
	    {
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest( base.getBytes("UTF-8") );
	        StringBuffer hexString = new StringBuffer();

	        for (int i = 0; i < hash.length; i++) 
	        {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }

	        return hexString.toString();
	    } catch(Exception ex) {
	       throw new RuntimeException(ex);
	    }
	}

	/**
	 * Generate a salted Sha-256 hash of a given string with the given salt value. <br/>
	 * If the salt value is null, generate a random 30-character salt value. 
	 * @param base the string to hash
	 * @param salt the salt to add
	 * @return the Sha-256 hash of the string and salt, prepended by the plaintext salt and a colon.
	 */
	public static String toSaltedHashString( String base, String salt ) 
	{
		if ( salt == null )
			return toSaltedHashString( base );
	    try
	    {
	    	// Simply combine the salt and base
	    	String comb = salt + base;
	        MessageDigest digest = MessageDigest.getInstance("SHA-256");
	        byte[] hash = digest.digest( comb.getBytes("UTF-8") );
	        // Generate the hexadecimal string
	        StringBuffer hexString = new StringBuffer();
	        hexString.append( salt ).append(":");
	        for (int i = 0; i < hash.length; i++) 
	        {
	            String hex = Integer.toHexString(0xff & hash[i]);
	            if(hex.length() == 1) hexString.append('0');
	            hexString.append(hex);
	        }
	        return hexString.toString();
	    } catch(Exception ex) {
	       throw new RuntimeException(ex);
	    }
	}

	/**
	 * Generate a salted Sha-256 hash of a given string with a randomly generated 
	 * (30-character) salt value. 
	 * @param base the string to hash
	 * @return the Sha-256 hash of the string and salt, prepended by the plaintext salt and a colon.
	 */
	public static String toSaltedHashString( String base )
	{
		// Generate a random salt and return the salted hash
		SecureRandom rnd = new SecureRandom();
		String salt = new BigInteger( 30, rnd ).toString( 32 );
		return toSaltedHashString( base, salt );
	}
}
