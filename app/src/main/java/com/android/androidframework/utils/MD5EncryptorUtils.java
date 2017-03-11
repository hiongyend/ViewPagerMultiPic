package com.android.androidframework.utils;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MD5EncryptorUtils {

	public static String getMD5(String paramString) {
		char[] arrayOfChar1 = { 48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 97, 98,
				99, 100, 101, 102 };
		try {
			MessageDigest localMessageDigest = MessageDigest.getInstance("MD5");
			localMessageDigest.update(paramString.getBytes());
			byte[] arrayOfByte = localMessageDigest.digest();
			char[] arrayOfChar2 = new char[32];
			int i = 0;
			int j = 0;
			while (i < 16) {
				int k = arrayOfByte[i];
				int m = j + 1;
				arrayOfChar2[j] = arrayOfChar1[(0xF & k >>> 4)];
				j = m + 1;
				arrayOfChar2[m] = arrayOfChar1[(k & 0xF)];
				i++;
			}
			String str = new String(arrayOfChar2);
			return str;
		} catch (Exception localException) {
		}
		return "";
	}
	
	/**
	 * md5加密
	 * @param string
	 * @return
	 * @throws Exception
	 */
	public static String md5Encryption(String string) {
	    try {
			byte[] hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
			StringBuilder hex = new StringBuilder(hash.length * 2);
			for (byte b : hash) {
			    if ((b & 0xFF) < 0x10) {
			    	hex.append("0");
			    }
			    hex.append(Integer.toHexString(b & 0xFF));
			}
			return hex.toString();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	    
	    return null;
	}
}
