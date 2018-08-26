package com.crazy.miaosha.redis;

public class GoodsKey extends BaseKeyPrefix{

	public static final int TOKEN_EXPIRE = 60;
	
	private GoodsKey(int expireSeconds, String prifix) {
		super(expireSeconds, prifix);
	}
	
	public static GoodsKey getGoodsList = new GoodsKey(TOKEN_EXPIRE, "goodsList");
	public static GoodsKey getGoodsDetail = new GoodsKey(TOKEN_EXPIRE, "goodsDetail");
	public static GoodsKey getMiaoshaGoodsStock = new GoodsKey(0, "goodsStock");
	
}
