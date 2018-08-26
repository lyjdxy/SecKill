package com.crazy.miaosha.redis;

public class OrderKey extends BaseKeyPrefix{

	
	private OrderKey(String prifix) {
		super(prifix);
	}
	
	public static OrderKey getMiaoshaOrderByUidGid = new OrderKey("moug");
	
}
