package com.crazy.miaosha.redis;

public class MiaoshaKey extends BaseKeyPrefix{
	
	private MiaoshaKey(int expireSeconds, String prifix) {
		super(expireSeconds, prifix);
	}
	
	public static MiaoshaKey isGoodsOver = new MiaoshaKey(0, "goods_over");
	public static MiaoshaKey getMiaoshaPath = new MiaoshaKey(60, "miaosha_path");
	public static MiaoshaKey getMiaoshaVerifyCode = new MiaoshaKey(240, "verify");
	
}
