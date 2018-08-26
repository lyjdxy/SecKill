package com.crazy.miaosha.rabbitMQ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crazy.miaosha.domain.MiaoshaOrder;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.service.GoodsService;
import com.crazy.miaosha.service.MiaoshaService;
import com.crazy.miaosha.service.OrderService;
import com.crazy.miaosha.vo.GoodsVo;

@Service
public class MQReceiver {
	
	private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
	
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private MiaoshaService miaoshaService;
	
	
	/**
	 * 4种交换机Exchange模式
	 * Direct模式 
	 */
	@RabbitListener(queues=MQConfig.QUEUR)
	public void receive(String msg){
		log.info("接受端接收到："+ msg);
	}
	
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUR)
	public void receiveMiaoshaMessage(String msg){
		log.info("接受端接收到："+ msg);
		MiaoshaMessage mm = RedisService.StringToBean(msg, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		
		//秒杀优化4、出队，秒杀业务处理
		
		//判断商品库存
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goodsVo.getStockCount();
		if(stock <= 0){
			return;
		}
		
		//判断用户是否已经秒杀过
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			return;
		}
		
		//减库存，下订单
		miaoshaService.miaosha(user,goodsVo);
		
		
		log.info("出队，秒杀处理结束");
	}
}
