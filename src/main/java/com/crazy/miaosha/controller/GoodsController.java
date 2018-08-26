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
 * ����ҳ�滺�棨��Ⱦ+redis����ǰ��˷��룬ҳ�澲̬�����ŵ㣬ǰ�˲�����JSP�ȶ�̬��ҳ�棬��������html��̬��ҳ���ʽ������ͻ��˼��ش洢��ֻ��̬�ĸı���Ҫ�����ݶ���������ҳ��
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
	 * ʹ�ò������������пӣ����Բ������д��������������жϴ����װ��MiaoshaUser������
	 */
	@RequestMapping("/to_list2")
	@ResponseBody
	public String toList2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user){
		//ȡҳ�滺�棬ʹ��ҳ�澲̬��
		String html = redisService.get(GoodsKey.getGoodsList, "", String.class);
		if(!StringUtils.isEmpty(html)){
			return html;
		}
		
		model.addAttribute("user", user);
		//ȡҳ�滺��Ҫ���ڲ������ݿ�֮ǰ����Ч
		//��ѯ������е���Ʒ��Ϣ
		List<GoodsVo> goodsList = goodsService.listGoodsVo();
		model.addAttribute("goodsList", goodsList);
		
		//return "goods_list";
		
		WebContext context = new WebContext(request, response, request.getServletContext(),request.getLocale(),model.asMap());
		//��û�л������ֶ���Ⱦ,ʹ��sb�Դ���thymeleafViewResolver
		html = thymeleafViewResolver.getTemplateEngine().process("goods_list", context);
		if(!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsList, "", html);
		}
		return html;
	}
	
	@RequestMapping("/to_detail2/{goodsId}")
	@ResponseBody
	public String toDetail2(HttpServletRequest request, HttpServletResponse response, Model model,MiaoshaUser user,@PathVariable("goodsId")long goodsId){
		//ȡҳ�滺��
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
		
		int miaoshaStatus = 0;//��ɱ״̬
		int remainSeconds = 0;//ʣ��ʱ��
		
		if(now < startAt){
			miaoshaStatus = 0;//δ��ʼ
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatus = 2;//�ѽ���
			remainSeconds = -1;
		}else{
			miaoshaStatus = 1;//���ڽ�����
			remainSeconds = 0;
		}
		
		model.addAttribute("miaoshaStatus", miaoshaStatus);
		model.addAttribute("remainSeconds", remainSeconds);
		
		//return "goods_detail";
		
		//�ֶ���Ⱦҳ��
		WebContext context = new WebContext(request, response, request.getServletContext(),request.getLocale(),model.asMap());
		html = thymeleafViewResolver.getTemplateEngine().process("goods_detail", context);
		if(!StringUtils.isEmpty(html)){
			redisService.set(GoodsKey.getGoodsDetail, ""+goodsId, html);
		}
		return html;
	}
	
	
	/**
	 * ʹ��ǰ��˷��룬ǰ��û��ʹ�ÿ�ܣ�ֻ�Ǽ򵥵�js
	 */
	@RequestMapping("/detail/{goodsId}")
	@ResponseBody
	public Result<GoodsDetailVo> detail(HttpServletRequest request, HttpServletResponse response, 
			Model model, MiaoshaUser user, @PathVariable("goodsId")long goodsId){
		
		GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
		
		long startAt = goods.getStartDate().getTime();
		long endAt = goods.getEndDate().getTime();
		long now = System.currentTimeMillis();
		
		int miaoshaStatus = 0;//��ɱ״̬
		int remainSeconds = 0;//ʣ��ʱ��
		if(now < startAt){
			miaoshaStatus = 0;//δ��ʼ
			remainSeconds = (int) ((startAt - now)/1000);
		}else if(now > endAt){
			miaoshaStatus = 2;//�ѽ���
			remainSeconds = -1;
		}else{
			miaoshaStatus = 1;//���ڽ�����
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
