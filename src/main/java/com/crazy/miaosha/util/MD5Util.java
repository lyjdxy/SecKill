package com.crazy.miaosha.util;

import org.apache.commons.codec.digest.DigestUtils;

public class MD5Util {
	
	public static String md5(String str){
		return DigestUtils.md5Hex(str);
	}
	
	private static final String salt = "1203957asdjbuqe";
	//ǰ�˹̶���MD5����
	public static String inputPassToFormPass(String inputPass){
		String str = "" + salt.charAt(0) + inputPass + salt.charAt(4) + salt.charAt(2) + salt.charAt(5);
		return md5(str);
	}
	//��˵ڶ������salt��MD5���ܣ���Ҫʹ�ã�
	public static String FormPassToDBPass(String formPass, String salt){
		String str = "" + salt.charAt(0) + formPass + salt.charAt(4) + salt.charAt(2) + salt.charAt(5);
		return md5(str);
	}
	//���μ��ܵĽ��
	public static String inputPassToDBPass(String input, String saltDB){
		String form = inputPassToFormPass(input);
		String dbPass = FormPassToDBPass(form, saltDB);
		return dbPass;
	}
	
	public static void main(String[] args) {
		System.out.println(inputPassToFormPass("123456"));
		System.out.println(inputPassToDBPass("000000","zxcvbnm"));
	}
	
}
