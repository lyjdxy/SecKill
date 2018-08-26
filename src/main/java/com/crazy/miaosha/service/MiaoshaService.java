package com.crazy.miaosha.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Random;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.crazy.miaosha.domain.MiaoshaOrder;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.domain.OrderInfo;
import com.crazy.miaosha.redis.MiaoshaKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.util.MD5Util;
import com.crazy.miaosha.util.UUIDUtil;
import com.crazy.miaosha.vo.GoodsVo;

@Service
public class MiaoshaService {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private RedisService redisService;

	@Transactional
	//减库存，下订单，写入秒杀订单
	public OrderInfo miaosha(MiaoshaUser user, GoodsVo goodsVo) {
		boolean rediceSuccess = goodsService.reduceStock(goodsVo.getId());
		if(rediceSuccess){
			return orderService.createOrder(user,goodsVo);
		}else{
			//设置区分排队和结束的标记
			setGoodsOver(goodsVo.getId());
			return null;
		}
		
	}


	//问题：如何区分 排队中 和 秒杀失败
	//解决：在秒杀业务操作中增加标记。如果当不能生产订单的时候（对应库存为0秒杀结束）标记为true-->isOver
	public long getMiaoshaResult(Long userId, long goodsId) {
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(userId, goodsId);
		if(order != null){
			return order.getOrderId();
		}else{
			boolean isOver = getGoodsOver(goodsId);
			if(isOver){//根据标记区分
				return -1;
			}else{
				return 0;//还没结束，正在排队
			}
		}
 	}
	
	//这个标记就放到redis中
	private void setGoodsOver(long goodsId) {
		redisService.set(MiaoshaKey.isGoodsOver, ""+goodsId, true);
	}

	private boolean getGoodsOver(long goodsId) {
		return redisService.exists(MiaoshaKey.isGoodsOver, ""+goodsId);//注意每次库存变化的时候这个key值也要改
		//return redisService.get(MiaoshaKey.isGoodsOver, ""+goodsId, boolean.class);
	}


	public void reset(List<GoodsVo> goodsList) {
		goodsService.resetStock(goodsList);
		orderService.deleteOrders();
	}


	public boolean checkMiaoshaPath(String path, Long userId, long goodsId) {
		if(path == null){
			return false;
		}
		String savePath = redisService.get(MiaoshaKey.getMiaoshaPath, ""+userId+"_"+goodsId,String.class);
		return path.equals(savePath);
	}

	public String createMiaoshaPath(MiaoshaUser user, long goodsId) {
		String path = MD5Util.md5(UUIDUtil.uuid() + "123456");
		redisService.set(MiaoshaKey.getMiaoshaPath, ""+user.getId()+"_"+goodsId, path);
		return path;
	}


	public BufferedImage createMiaoshaVerifyCode(MiaoshaUser user, long goodsId) {
		if(user == null || goodsId <= 0){
			return null;
		}
		int width = 80;
		int height = 32;
		//create image 
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
		Graphics g = image.getGraphics();
		//set
		g.setColor(new Color(0xDCDCDC));
		g.fillRect(0, 0, width, height);
		g.setColor(Color.black);
		g.drawRect(0, 0, width - 1, height - 1);
		//create random oval
		Random random = new Random();
		for(int i = 0; i<50; i++){
			int x = random.nextInt(width);
			int y = random.nextInt(height);
			g.drawOval(x, y, 0, 0);//干扰点
		}
		String verifyCode = createVerifyCode(random);//获得随机的计算式
		g.setColor(new Color(0, 100, 0));
		g.setFont(new Font("Candara", Font.BOLD, 24));
		g.drawString(verifyCode, 8, 24);
		g.dispose();
		//把验证码计算的结果存到redis中
		int rnd = calc(verifyCode);//获得计算式的结果
		redisService.set(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+"_"+goodsId, rnd);
		return image;
	}

	private static char[] ops = new char[]{'+', '-', '*'};
	
	private String createVerifyCode(Random random) {//获得随机的计算式
		int num1 = random.nextInt(10);
		int num2 = random.nextInt(10);
		int num3 = random.nextInt(10);
		char op1 = ops[random.nextInt(3)];
		char op2 = ops[random.nextInt(3)];
		String exp = "" + num1 + op1 + num2 + op2 + num3;
		return exp;
	}

	private static int calc(String exp) {//获得计算式的结果
		try{
			ScriptEngineManager manager = new ScriptEngineManager();//jdk6
			ScriptEngine engine = manager.getEngineByName("JavaScript");
			return (Integer) engine.eval(exp);
		}catch(Exception e){
			e.printStackTrace();
			return 0;
		}
	}
	public static void main(String[] args) {
		System.out.println(calc("1+8-20"));
	}


	public boolean checkVerifyCode(MiaoshaUser user, long goodsId, int verifyCode) {
		if(user == null || goodsId <= 0){
			return false;
		}
		Integer codeOld = redisService.get(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+"_"+goodsId, int.class);
		if(codeOld == null || codeOld - verifyCode != 0){
			return false;
		}
		//每次验证，无论成功失败都要把验证码从redis中删掉，防止再次使用
		redisService.delete(MiaoshaKey.getMiaoshaVerifyCode, user.getId()+"_"+goodsId);
		return true;
	}




}
