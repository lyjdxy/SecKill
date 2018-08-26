package com.crazy.miaosha.rabbitMQ;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MQConfig {
	public static final String QUEUR = "queue";

	public static final String MIAOSHA_QUEUR = "miaosha.queue";
	
	@Bean
	public Queue queue(){
		return new Queue(QUEUR, true);
	}
	@Bean
	public Queue Miaosha_queue(){
		return new Queue(MIAOSHA_QUEUR, true);
	}
	
}
