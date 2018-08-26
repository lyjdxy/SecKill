package com.crazy.miaosha.redis;

public class UserKey extends BaseKeyPrefix{

	private UserKey(String prifix) {
		super(prifix);
	}
	
	public static UserKey getById = new UserKey("id");
	public static UserKey getByName = new UserKey("name");

}
