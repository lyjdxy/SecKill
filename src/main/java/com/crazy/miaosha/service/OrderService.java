package com.crazy.miaosha.service;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crazy.miaosha.dao.OrderDao;
import com.crazy.miaosha.domain.MiaoshaOrder;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.domain.OrderInfo;
import com.crazy.miaosha.redis.OrderKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.vo.GoodsVo;

@Service
public class OrderService {

	@Autowired
	private OrderDao orderDao;
	
	@Autowired
	private RedisService redisService;
	
	public OrderInfo getOrderById(long orderId) {
		return orderDao.getOrderById(orderId);
	}

	public MiaoshaOrder getMiaoshaOrderByUserIdAndGoodsId(long userId,long goodsId) {
		//return orderDao.getMiaoshaOrderByUserIdAndGoodsId(userId,goodsId);
		return redisService.get(OrderKey.getMiaoshaOrderByUidGid, ""+userId+"_"+goodsId, MiaoshaOrder.class);
	}

	public OrderInfo createOrder(MiaoshaUser user, GoodsVo goodsVo) {
		//创建order_info表的记录
		OrderInfo orderInfo = new OrderInfo();
		orderInfo.setCreateDate(new Date());
		orderInfo.setDeliveryAddrId(0L);
		orderInfo.setGoodsCount(1);
		orderInfo.setGoodsId(goodsVo.getId());
		orderInfo.setGoodsName(goodsVo.getGoodsName());
		orderInfo.setGoodsPrice(goodsVo.getMiaoshaPrice());
		orderInfo.setOrderChannel(1);
		orderInfo.setStatus(0);
		orderInfo.setUserId(user.getId());
		orderDao.insertOrder(orderInfo);
		
		//创建miaosha_order表的记录
		MiaoshaOrder mo = new MiaoshaOrder();
		mo.setGoodsId(goodsVo.getId());
		mo.setOrderId(orderInfo.getId());
		mo.setUserId(user.getId());
		orderDao.insertMiaoshaOrder(mo);
		
		//订单详情写入缓存
		redisService.set(OrderKey.getMiaoshaOrderByUidGid, ""+user.getId()+"_"+goodsVo.getId(), mo);
		
		return orderInfo;
	}

	public void deleteOrders() {
		orderDao.deleteOrders();
		orderDao.deleteMiaoshaOrders();
	}
	
}
