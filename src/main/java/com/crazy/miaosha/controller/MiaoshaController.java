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
	
	
	//�ڴ��ǡ��Ż�redisԤ�����
	private Map<Long, Boolean> localOverMap = new HashMap<Long, Boolean>();

	@RequestMapping("/do_miaosha")
	public String doMiaosha(MiaoshaUser user,Model model,@RequestParam("goodsId")long goodsId){
		model.addAttribute("user", user);
		//�ж��û�
		if(user == null){
			return "login";
		}
		//�ж���Ʒ���
		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
		int stock = goodsVo.getStockCount();
		if(stock <= 0){
			model.addAttribute("errorMsg", CodeMsg.MIAOSHA_OVER.getMsg());
			return "miaosha_fail";
		}
		//�ж��û��Ƿ��Ѿ���ɱ��
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			model.addAttribute("errorMsg", CodeMsg.MIAOSHA_REPEATE.getMsg());
			return "miaosha_fail";
		}
		
		//��ɱ�������������й�
		OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
		
		model.addAttribute("goods",goodsVo);
		model.addAttribute("orderInfo", orderInfo);
		return "order_detail";
	}
	
	//��ɱ�Ż�1��ϵͳ��ʼ�����Ѹ�����Ʒ�Ŀ����������redis
	public void afterPropertiesSet() throws Exception {
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		if(goodsList == null){
			return;
		}
		for(GoodsVo g : goodsList){
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+g.getId(), g.getStockCount());
			localOverMap.put(g.getId(), false);//��ʼ��ʱ��Ϊÿ����Ʒ���ڴ���
		}
	}
	
	//ǰ��˷���
	/**
	 * ��ɱ�����ٴ����Ż���ʹ��rabbitMQ���첽�µ�
	 * ˼·���������ݿ�ķ���
	 * ���裺
	 * 		1��ϵͳ��ʼ�����Ѹ�����Ʒ�Ŀ����������redis��ʵ��InitializingBean�ӿ��е�afterPropertiesSet���ڳ�ʼ��ʱ������
	 * 		2���յ�����redisԤ����棬��治����ֱ�ӷ��أ��������3�����ã�����Ʒ���Ϊ0ʱ���Ժ�Ĳ����������漰�����ݿ⣩
	 * 		3��������ӣ����������Ŷ��У�ʹ��RabbitMQ��Ϣ���У�
	 * 		4��������ӣ����ɶ��������ٿ��
	 * 		5���ͻ�����ѯ���Ƿ���ɱ�ɹ�
	 */
	@RequestMapping(value="/doMiaosha/{path}/", method=RequestMethod.POST)
	@ResponseBody
	public Result<Integer> doMiaosha2(MiaoshaUser user, Model model, 
			@RequestParam("goodsId")long goodsId, 
			@PathVariable("path")String path){
		model.addAttribute("user", user);
		//�ж��û�
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		
		//�ж���ɱ�ӿڲ���path�Ƿ���ȷ
		boolean check = miaoshaService.checkMiaoshaPath(path,user.getId(),goodsId);
		if(!check){
			return Result.error(CodeMsg.REQUEST_ILLEGAL);
		}
		
		//�ڴ��ǵ�ʹ��
		boolean over = localOverMap.get(goodsId);
		if(over){
			return Result.error(CodeMsg.MIAOSHA_OVER);//����ɱ֮ǰ�����ڴ����ж���Ʒ�Ƿ�Ϊ����
			//��������Ŀ���Ǽ���redisȥ�ж���Ʒ�Ƿ�Ϊ���˵Ŀ���
		}
		
		//�ж��û��Ƿ��Ѿ���ɱ��
		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
		if(order != null){
			return Result.error(CodeMsg.MIAOSHA_REPEATE);
		}
		
		//��ɱ�Ż�2��redisԤ�����
		long stock = redisService.decr(GoodsKey.getMiaoshaGoodsStock, ""+goodsId);
		if(stock < 0){
			localOverMap.put(goodsId, true);
			return Result.error(CodeMsg.MIAOSHA_OVER);
		}
		
		//��ɱ�Ż�3�����
		MiaoshaMessage mm = new MiaoshaMessage();
		mm.setUser(user);
		mm.setGoodsId(goodsId);
		sender.sendMiaoshaMessage(mm);
		
		//��ɱ�Ż�5���ͻ�����ѯ���Ƿ���ɱ�ɹ�
		return Result.success(0);//0��ʾ�Ŷ���
		
		//�ж��û��Ƿ��Ѿ���ɱ��
//		MiaoshaOrder order = orderService.getMiaoshaOrderByUserIdAndGoodsId(user.getId(), goodsId);
//		if(order != null){
//			return Result.error(CodeMsg.MIAOSHA_REPEATE);
//		}
		
		//�ж���Ʒ���
//		GoodsVo goodsVo = goodsService.getGoodsVoByGoodsId(goodsId);
//		int stock = goodsVo.getStockCount();
//		if(stock <= 0){
//			return Result.error(CodeMsg.MIAOSHA_OVER);
//		}
		
		//��ɱ�������������й�
//		OrderInfo orderInfo = miaoshaService.miaosha(user,goodsVo);
		//���ڵĲ������⣺1������ڸ߲����»���ɸ����������sql��������
		//				 2���߲�����ͬһ�û��ظ���ɱ����������ݿ��miaosha_order��user_id��goods_id��Ψһ����
		
		
//		return Result.success(orderInfo);
	}

	
	/**
	 * @return  �ɹ���ɱ��orderId
	 * 			��ɱʧ�ܣ�-1
	 * 			�Ŷ���    ��0
	 */
	@RequestMapping(value="/result", method=RequestMethod.GET)
	@ResponseBody
	public Result<Long> getResult(Model model, MiaoshaUser user,@RequestParam("goodsId")long goodsId){
		model.addAttribute("user", user);
		//�ж��û�
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		long result = miaoshaService.getMiaoshaResult(user.getId(), goodsId);
		return Result.success(result);
	}
	
	
	@RequestMapping("/reset")
	@ResponseBody
	public Result<Boolean> reset(Model model){
		System.out.println(redisService.flushDB());//flushDB����
		
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		for(GoodsVo g : goodsList){
			g.setStockCount(10);//������Ʒ�����Ϊ10
			redisService.set(GoodsKey.getMiaoshaGoodsStock, ""+g.getId(), 10);//redisҲ��
			localOverMap.put(g.getId(), false);
		}
		redisService.delete(OrderKey.getMiaoshaOrderByUidGid, "");
		redisService.delete(MiaoshaKey.isGoodsOver, "");
		miaoshaService.reset(goodsList);
		
		return Result.success(true);
	}
	
	//�ӿ��Ż�2(ͨ��ע�⣬������)
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
		
		//����ж���֤��ĸ�ʽ�Ƿ���ȷ
		if(verifyCode == null || verifyCode == ""){
			return Result.error(CodeMsg.VERIFYCODE_EMPTY);
		}
		int verify = 0;
		try {
			verify = Integer.parseInt(verifyCode);
		} catch (NumberFormatException e) {
			System.out.println("��֤���������Ӧ�������������"+e.getMessage());
			return Result.error(CodeMsg.VERIFYCODE_STYLE);
		}
		
		//�ӿ��Ż�1(��ͨ��)��������ˢ(��һ���Ǻű��浽redis��)
		String uri = request.getRequestURI();
		String key = uri + user.getId();
		AccessKey ak = AccessKey.withExpire(10);
		Integer AccessCount = redisService.get(ak, key, Integer.class);
		if(AccessCount == null){
			redisService.set(ak, key, 1);//��һ�η���
		}else if(AccessCount < 5){
			redisService.incr(ak, key);
		}else{
			return Result.error(CodeMsg.ACCESS_LIMIT);
		}
		
		
		//�ж���֤���Ƿ���ȷ
		boolean checkVerify = miaoshaService.checkVerifyCode(user, goodsId, verify);
		if(!checkVerify){
			return Result.error(CodeMsg.VERIFYCODE_ERROR);
		}
		
		String pathVarable = miaoshaService.createMiaoshaPath(user, goodsId);//�������һ��ƴ�ӵ���ɱ��ַ
		
		return Result.success(pathVarable);
	}
	
	/**
	 * ������֤����IO���أ�������֤��������redis
	 */
	@AccessLimit(seconds=5, maxCount=8, needLogin=true)
	@RequestMapping(value="/verifyCode", method=RequestMethod.GET)
	@ResponseBody
	public Result<String> getVerifyCode(MiaoshaUser user, HttpServletResponse response, 
			@RequestParam("goodsId")long goodsId){
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		//ʹ��java.awt.BufferedImage
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
