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
	 * 4�ֽ�����Exchangeģʽ
	 * Directģʽ 
	 */
	@RabbitListener(queues=MQConfig.QUEUR)
	public void receive(String msg){
		log.info("���ܶ˽��յ���"+ msg);
	}
	
	@RabbitListener(queues=MQConfig.MIAOSHA_QUEUR)
	public void receiveMiaoshaMessage(String msg){
		log.info("���ܶ˽��յ���"+ msg);
		MiaoshaMessage mm = RedisService.StringToBean(msg, MiaoshaMessage.class);
		MiaoshaUser user = mm.getUser();
		long goodsId = mm.getGoodsId();
		
		//��ɱ�Ż�4�����ӣ���ɱҵ����
		
		//�ж���Ʒ���
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goodsVo.getStockCount();
		if(stock <= 0){
			return;
		}
		
		//�ж��û��Ƿ��Ѿ���ɱ��
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			return;
		}
		
		//����棬�¶���
		miaoshaService.miaosha(user,goodsVo);
		
		
		log.info("���ӣ���ɱ�������");
	}
}
