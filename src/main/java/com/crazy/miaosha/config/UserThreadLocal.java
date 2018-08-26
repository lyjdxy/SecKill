package com.crazy.miaosha.config;

import com.crazy.miaosha.domain.MiaoshaUser;

/**
 * 线程安全的，每个线程都有唯一的一个ThreadLocal
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
