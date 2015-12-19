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

import java.io.UnsupportedEncodingException;
import java.security.Key;
import java.security.spec.KeySpec;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Wrapper class to enable AES 128-bit encoding and decoding. <br/>
 * @author IDFocus B.V. (mvreijn@idfocus.nl)
 * @author Michael Remijan (mjremijan@yahoo.com)
 */
public class Aes128 
{

	private static final String IV   = "aSh.ghec@ith*vue";
	private String PASSWORD          = "ye2oB5jaN7feec3d";
	private static final String SALT = "Ac.Jip#Ub+";

	public Aes128( String key ) 
	{
		PASSWORD = key;
	}

	public byte[] encryptAndEncode(String raw) 
	{
		try {
			Cipher c = getCipher(Cipher.ENCRYPT_MODE);
			return c.doFinal(getBytes(raw));
		} catch (Throwable t) {
			throw new RuntimeException(t);
		}
	}

	public byte[] decodeAndDecrypt(String encrypted) throws Exception 
	{
		byte[] decodedValue = Base64.decode(getBytes(encrypted));
		Cipher c = getCipher(Cipher.DECRYPT_MODE);
		return c.doFinal(decodedValue);
	}

	private byte[] getBytes(String str) throws UnsupportedEncodingException 
	{
		return str.getBytes("UTF-8");
	}

	private Cipher getCipher(int mode) throws Exception 
	{
		Cipher c = Cipher.getInstance("AES/CBC/PKCS5Padding");
		byte[] iv = getBytes(IV);
		c.init(mode, generateKey(), new IvParameterSpec(iv));
		return c;
	}

	/**
	 * Generate a 128-bit AES key with the given password and salt inputs. 
	 * @return
	 * @throws Exception
	 */
	private Key generateKey() throws Exception 
	{
		SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");
		char[] password = PASSWORD.toCharArray();
		byte[] salt = getBytes(SALT);

		KeySpec spec   = new PBEKeySpec(password, salt, 65536, 128);
		SecretKey key  = factory.generateSecret(spec);
		byte[] encoded = key.getEncoded();
		return new SecretKeySpec(encoded, "AES");
	}

}