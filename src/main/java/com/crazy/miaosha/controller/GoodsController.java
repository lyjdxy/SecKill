package com.crazy.miaosha.controller;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.context.WebContext;
import org.thymeleaf.spring5.view.ThymeleafViewResolver;

import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.redis.GoodsKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.result.Result;
import com.crazy.miaosha.service.GoodsService;
import com.crazy.miaosha.service.MiaoshaUserService;
import com.crazy.miaosha.vo.GoodsDetailVo;
import com.crazy.miaosha.vo.GoodsVo;

/**
 * 加入页面缓存（渲染+redis）；前后端分离，页面静态化的优点，前端不再是JSP等动态的页面，而是类似html静态的页面格式，方便客户端加载存储，只动态的改变需要的数据而不是整个页面
 *
 */
@Controller
@RequestMapping("/goods")
public class GoodsController {
	
	@Autowired
	private MiaoshaUserService userService;
	
	@Autowired
	private GoodsService goodsService;
	
	@Autowired
	private RedisService redisService;
	
	@Autowired
	private ThymeleafViewResolver thymeleafViewResolver;
	
	@Autowired
	private ApplicationContext applicationContext;

	@RequestMapping("/to_list")
	public String toList(Model model,
			HttpServletResponse response, 
			@CookieValue(value=MiaoshaUserService.COOKIE_NAME_TOKEN, required=false)String cookieToken,
			@RequestParam(value=MiaoshaUserService.COOKIE_NAME_TOKEN,required=false)String paramToken){
		if(StringUtils.isEmpty(paramToken)&&StringUtils.isEmpty(cookieToken)){
			return "login";
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		MiaoshaUser user = userService.getByToken(response, token);
		model.addAttribute("user", user);
		return "goods_list";
	}
	
	/**
	 * 使用参数解析器（有坑），对参数进行处理把所需的数据判断处理封装到MiaoshaUser对象中
	 */
	@RequestMapping("/to_list2")
	@ResponseBody
	public String toList2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user){
		//取页面缓存，使用页面静态化
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)){
			return html;
		}
		
		model.addAttribute("user", user);
		//取页面缓存要放在操作数据库之前才有效
		//查询获得所有的商品信息
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		
		//return "goods_list";
		
		WebContext context = new WebContext(request, response, request.getServletContext(),request.getLocale(),model.asMap());
		//若没有缓存则手动渲染,使用sb自带的thymeleafViewResolver
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
		if(!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping("/to_detail2/{goodsId}")
	@ResponseBody
	public String toDetail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId){
		//取页面缓存
		String html = redisService.get(GoodsKey.getGoodsDetail, ""+goodsId, String.class);
		if(!StringUtils.isEmpty(html)){
			return html;
		}
		
		model.addAttribute("user", user);
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		model.addAttribute("goods", goods);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int miaoshaStatus = 0;//秒杀状态
		int remainSeconds = 0;//剩余时间
		
		if(now < startAt){
			miaoshaStatus = 0;//未开始
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatus = 2;//已结束
			remainSeconds = -1;
		}else{
			miaoshaStatus = 1;//正在进行中
			remainSeconds = 0;
		}
		
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		
		//return "goods_detail";
		
		//手动渲染页面
		WebContext context = new WebContext(request, response, request.getServletContext(),request.getLocale(),model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
		if(!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}
	
	
	/**
	 * 使用前后端分离，前端没有使用框架，只是简单的js
	 */
	@RequestMapping("/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, 
			Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId){
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int miaoshaStatus = 0;//秒杀状态
		int remainSeconds = 0;//剩余时间
		if(now < startAt){
			miaoshaStatus = 0;//未开始
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatus = 2;//已结束
			remainSeconds = -1;
		}else{
			miaoshaStatus = 1;//正在进行中
			remainSeconds = 0;
		}
		
		GoodsDetailVo vo = new GoodsDetailVo();
		vo.setGoodsVo(goods);
		vo.setMiaoshaStatus(miaoshaStatus);
		vo.setRemainSeconds(remainSeconds);
		vo.setUser(user);
		
		return Result.success(vo);
	}
	
}
