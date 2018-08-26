package com.crazy.miaosha.rabbitMQ;

import com.crazy.miaosha.domain.MiaoshaUser;

public class MiaoshaMessage {

	private MiaoshaUser user;
	public MiaoshaUser getUser() {
		return user;
	}
	public void setUser(MiaoshaUser user) {
		this.user = user;
	}
	public long getGoodsId() {
		return goodsId;
	}
	public void setGoodsId(long goodsId) {
		this.goodsId = goodsId;
	}
	private long goodsId;
	
}
