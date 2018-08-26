package com.crazy.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crazy.miaosha.domain.User;
import com.crazy.miaosha.rabbitMQ.MQSender;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.redis.UserKey;
import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.result.Result;

@Controller
public class TestController {
	
	@Autowired
	RedisService redisService;

	@RequestMapping("/hello")
	@ResponseBody
	public String hello() {
		return "hello sb";
	}

	// 创建封装返回的结果集
	@ResponseBody
	@RequestMapping("/result")
	public Result<String> testResult() {
		// return Result.success("success Result");
		return Result.error(CodeMsg.SERVICE_ERROR);
	}
	
	@RequestMapping("/thymeleaf")
	public String thymeleaf(Model model){
		model.addAttribute("username", "crazy");
		return "hello";
	}
	
	@RequestMapping("/redis/set")
	@ResponseBody
	public Result<Boolean> testRedisSet(){
		User user = new User();
		user.setId(11);
		user.setName("11111");
		boolean ret = redisService.set(UserKey.getById, ""+1, user);
		return Result.success(ret);
	}
	
	@RequestMapping("/redis/get")
	@ResponseBody
	public Result<User> testRedisGet(){
		User u1 = redisService.get(UserKey.getById, ""+1, User.class);
		return Result.success(u1);
	}
	
	@Autowired
	private MQSender sender;
	
	@RequestMapping("/mq")
	@ResponseBody
	public Result<String> testRabbitMQ(){
		sender.send("hello RabbitMQ!");
		return Result.success("消息发送成功");
	}
	
}
