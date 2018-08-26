package com.crazy.miaosha.redis;

public class AccessKey extends BaseKeyPrefix{

	public AccessKey(int expireSeconds, String prifix) {
		super(expireSeconds, prifix);
	}

	public static AccessKey withExpire(int expireSeconds){
		return new AccessKey(expireSeconds, "access");
	}
	
}
