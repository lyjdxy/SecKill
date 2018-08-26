package com.crazy.miaosha.redis;

public class MiaoshaUserKey extends BaseKeyPrefix{

	public static final int TOKEN_EXPIRE = 3600*24*2;
	
	private MiaoshaUserKey(int expireSeconds, String prifix) {
		super(expireSeconds, prifix);
	}
	
	public static MiaoshaUserKey token = new MiaoshaUserKey(TOKEN_EXPIRE, "tk");//登录标识
	public static MiaoshaUserKey getById = new MiaoshaUserKey(0, "id");//验证该手机号是否存在数据库
	
}
