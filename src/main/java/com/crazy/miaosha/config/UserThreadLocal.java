package com.crazy.miaosha.config;

import com.crazy.miaosha.domain.MiaoshaUser;

/**
 * �̰߳�ȫ�ģ�ÿ���̶߳���Ψһ��һ��ThreadLocal
 * @author dxy
 *
 */
public class UserThreadLocal {

	private static ThreadLocal<MiaoshaUser> userHolder = new ThreadLocal<MiaoshaUser>();
	
	public static void setUser(MiaoshaUser user){
		userHolder.set(user);
	}
	
	public static MiaoshaUser getUser(){
		return userHolder.get();
	}
	
}
