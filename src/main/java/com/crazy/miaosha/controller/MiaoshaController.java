package com.crazy.miaosha.controller;

import java.awt.image.BufferedImage;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crazy.miaosha.config.AccessLimit;
import com.crazy.miaosha.domain.MiaoshaOrder;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.domain.OrderInfo;
import com.crazy.miaosha.rabbitMQ.MQSender;
import com.crazy.miaosha.rabbitMQ.MiaoshaMessage;
import com.crazy.miaosha.redis.AccessKey;
import com.crazy.miaosha.redis.GoodsKey;
import com.crazy.miaosha.redis.MiaoshaKey;
import com.crazy.miaosha.redis.OrderKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.result.Result;
import com.crazy.miaosha.service.GoodsService;
import com.crazy.miaosha.service.MiaoshaService;
import com.crazy.miaosha.service.OrderService;
import com.crazy.miaosha.vo.GoodsVo;

@Controller
@RequestMapping("/miaosha")
public class MiaoshaController implements InitializingBean{
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private MiaoshaService miaoshaService;
	
	@Autowired
	private MQSender sender;
	
	
	//内存标记。优化redis预减库存
	private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

	@RequestMapping("/do_miaosha")
	public String doMiaosha(MiaoshaUser user,Model model,@RequestParam("goodsId")long goodsId){
		model.addAttribute("user", user);
		//判断用户
		if(user == null){
			return "login";
		}
		//判断商品库存
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goodsVo.getStockCount();
		if(stock <= 0){
			model.addAttribute("errorMsg", CodeMsg.MIAOSHA_OVER.getMsg());
			return "miaosha_fail";
		}
		//判断用户是否已经秒杀过
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			model.addAttribute("errorMsg", CodeMsg.MIAOSHA_REPEATE.getMsg());
			return "miaosha_fail";
		}
		
		//秒杀操作，与事务有关
		OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
		
		model.addAttribute("goods",goodsVo);
		model.addAttribute("orderInfo", orderInfo);
		return "order_detail";
	}
	
	//秒杀优化1、系统初始化，把各个商品的库存数量存入redis
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if(goodsList == null){
			return;
		}
		for(GoodsVo g : goodsList){
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+g.getId(), g.getStockCount());
			localOverMap.put(g.getId(), false);//初始化时，为每个商品做内存标记
		}
	}
	
	//前后端分离
	/**
	 * 秒杀操作再次做优化，使用rabbitMQ做异步下单
	 * 思路：减少数据库的访问
	 * 步骤：
	 * 		1、系统初始化，把各个商品的库存数量存入redis（实现InitializingBean接口中的afterPropertiesSet，在初始化时操作）
	 * 		2、收到请求，redis预减库存，库存不足则直接返回，否则进入3（作用：当商品库存为0时，以后的操作都不会涉及到数据库）
	 * 		3、请求入队，立即返回排队中（使用RabbitMQ消息队列）
	 * 		4、请求出队，生成订单，减少库存
	 * 		5、客户端轮询，是否秒杀成功
	 */
	@RequestMapping(value="/doMiaosha/{path}/", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> doMiaosha2(MiaoshaUser user, Model model, 
			@RequestParam("goodsId")long goodsId, 
			@PathVariable("path")String path){
		model.addAttribute("user", user);
		//判断用户
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		//判断秒杀接口参数path是否正确
		boolean check = miaoshaService.checkMiaoshaPath(path,user.getId(),goodsId);
		if(!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		//内存标记的使用
		boolean over = localOverMap.get(goodsId);
		if(over){
			return Result.error(CodeMsg.MIAOSHA_OVER);//在秒杀之前利用内存标记判断商品是否为空了
			//这样做的目的是减少redis去判断商品是否为空了的开销
		}
		
		//判断用户是否已经秒杀过
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			return Result.error(CodeMsg.MIAOSHA_REPEATE);
		}
		
		//秒杀优化2、redis预减库存
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
		if(stock < 0){
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		//秒杀优化3、入队
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		
		//秒杀优化5、客户端轮询，是否秒杀成功
		return Result.success(0);//0表示排队中
		
		//判断用户是否已经秒杀过
//		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
//		if(order != null){
//			return Result.error(CodeMsg.MIAOSHA_REPEATE);
//		}
		
		//判断商品库存
//		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//		int stock = goodsVo.getStockCount();
//		if(stock <= 0){
//			return Result.error(CodeMsg.MIAOSHA_OVER);
//		}
		
		//秒杀操作，与事务有关
//		OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
		//存在的并发问题：1，库存在高并发下会减成负数，解决：sql语句加限制
		//				 2，高并发下同一用户重复秒杀，解决：数据库表miaosha_order给user_id和goods_id加唯一索引
		
		
//		return Result.success(orderInfo);
	}

	
	/**
	 * @return  成功秒杀：orderId
	 * 			秒杀失败：-1
	 * 			排队中    ：0
	 */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> getResult(Model model, MiaoshaUser user,@RequestParam("goodsId")long goodsId){
		model.addAttribute("user", user);
		//判断用户
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}
	
	
	@RequestMapping("/reset")
	@ResponseBody
	public Result<Boolean> reset(Model model){
		System.out.println(redisService.flushDB());//flushDB慎用
		
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo g : goodsList){
			g.setStockCount(10);//所有商品库存设为10
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+g.getId(), 10);//redis也是
			localOverMap.put(g.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid, "");
		redisService.delete(MiaoshaKey.isGoodsOver, "");
		miaoshaService.reset(goodsList);
		
		return Result.success(true);
	}
	
	//接口优化2(通用注解，拦截器)
	//@AccessLimit(seconds=5, maxCount=5, needLogin=true)
	@RequestMapping(value="/path", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getMiaoshaPath(HttpServletRequest request, MiaoshaUser user, Model model, 
			@RequestParam("goodsId")long goodsId,
			@RequestParam("verifyCode")String verifyCode){
		model.addAttribute("user", user);
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		//后端判断验证码的格式是否正确
		if(verifyCode == null || verifyCode == ""){
			return Result.error(CodeMsg.VERIFYCODE_EMPTY);
		}
		int verify = 0;
		try {
			verify = Integer.parseInt(verifyCode);
		} catch (NumberFormatException e) {
			System.out.println("验证码输入错误（应该输出整数）："+e.getMessage());
			return Result.error(CodeMsg.VERIFYCODE_STYLE);
		}
		
		//接口优化1(不通用)：限流防刷(做一个记号保存到redis中)
		String uri = request.getRequestURI();
		String key = uri + user.getId();
		AccessKey ak = AccessKey.withExpire(10);
		Integer AccessCount = redisService.get(ak, key, Integer.class);
		if(AccessCount == null){
			redisService.set(ak, key, 1);//第一次访问
		}else if(AccessCount < 5){
			redisService.incr(ak, key);
		}else{
			return Result.error(CodeMsg.ACCESS_LIMIT);
		}
		
		
		//判断验证码是否正确
		boolean checkVerify = miaoshaService.checkVerifyCode(user, goodsId, verify);
		if(!checkVerify){
			return Result.error(CodeMsg.VERIFYCODE_ERROR);
		}
		
		String pathVarable = miaoshaService.createMiaoshaPath(user, goodsId);//随机生成一个拼接到秒杀地址
		
		return Result.success(pathVarable);
	}
	
	/**
	 * 生成验证码以IO返回，并将验证码结果存入redis
	 */
	@AccessLimit(seconds=5, maxCount=8, needLogin=true)
	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getVerifyCode(MiaoshaUser user, HttpServletResponse response, 
			@RequestParam("goodsId")long goodsId){
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//使用java.awt.BufferedImage
		try {
			BufferedImage image = miaoshaService.createMiaoshaVerifyCode(user, goodsId);
			OutputStream out = response.getOutputStream();
			ImageIO.write(image, "JPEG", out);
			out.flush();
			out.close();
			return null;
		} catch (Exception e) {
			e.printStackTrace();
			return Result.error(CodeMsg.MIAOSHA_ERROR);
		}
	}
}
