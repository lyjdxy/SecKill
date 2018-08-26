package com.crazy.miaosha.redis;

public interface KeyPrefix {

	int expireSeconds();
	
	String getPrefix();
	
}
