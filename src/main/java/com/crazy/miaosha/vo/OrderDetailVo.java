package com.crazy.miaosha.vo;

import com.crazy.miaosha.domain.OrderInfo;

public class OrderDetailVo {

	private GoodsVo goods;
	private OrderInfo orderInfo;
	public GoodsVo getGoods() {
		return goods;
	}
	public void setGoods(GoodsVo goods) {
		this.goods = goods;
	}
	public OrderInfo getOrderInfo() {
		return orderInfo;
	}
	public void setOrderInfo(OrderInfo orderInfo) {
		this.orderInfo = orderInfo;
	}
	
	
	
}
