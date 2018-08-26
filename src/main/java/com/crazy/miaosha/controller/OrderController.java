package com.crazy.miaosha.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.domain.OrderInfo;
import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.result.Result;
import com.crazy.miaosha.service.GoodsService;
import com.crazy.miaosha.service.OrderService;
import com.crazy.miaosha.vo.GoodsVo;
import com.crazy.miaosha.vo.OrderDetailVo;

@Controller
@RequestMapping("/order")
public class OrderController {
	
	@Autowired
	private OrderService orderService;
	
	@Autowired
	private GoodsService goodsService;

	@ResponseBody
	@RequestMapping("/getOrderDetail")
	public Result<OrderDetailVo> getDetail(MiaoshaUser user, @RequestParam("orderId")long orderId){
		if(user == null){
			return Result.error(CodeMsg.SESSION_ERROR);
		}
		OrderInfo order = orderService.getOrderById(orderId);
		if(order == null){
			return Result.error(CodeMsg.ORDER_NOT_EXIST);
		}
		long goodsId = order.getGoodsId();
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		
		OrderDetailVo vo = new OrderDetailVo();
		vo.setGoods(goods);
		vo.setOrderInfo(order);
		
		return Result.success(vo);
	}
	
}
