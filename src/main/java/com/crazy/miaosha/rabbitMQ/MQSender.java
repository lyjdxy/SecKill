package com.crazy.miaosha.rabbitMQ;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.core.AmqpTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crazy.miaosha.redis.RedisService;

@Service
public class MQSender {
	private static Logger log = LoggerFactory.getLogger(MQSender.class);

	@Autowired
	AmqpTemplate amqpTemplate;
	
	public void send(Object msg){
		String msgStr = RedisService.beanToString(msg);
		log.info("发送端发送："+msgStr);
		amqpTemplate.convertAndSend(MQConfig.QUEUR, msgStr);
	}

	public void sendMiaoshaMessage(MiaoshaMessage mm) {
		String msgStr = RedisService.beanToString(mm);
		log.info("发送端发送："+msgStr);
		amqpTemplate.convertAndSend(MQConfig.MIAOSHA_QUEUR, msgStr);
		log.info("入队，等待处理秒杀");
	}
	
}
