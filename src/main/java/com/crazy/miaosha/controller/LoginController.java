package com.crazy.miaosha.controller;

import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crazy.miaosha.result.Result;
import com.crazy.miaosha.service.MiaoshaUserService;
import com.crazy.miaosha.vo.LoginVo;

@Controller
@RequestMapping("login")
public class LoginController {
	
	@Autowired
	private MiaoshaUserService userService;
	
	//日志
	private static Logger log = LoggerFactory.getLogger(LoginController.class);
	
	@RequestMapping("/to_login")
	public String toLogin(){
		return "login";
	}
	
	@RequestMapping("/do_login")
	@ResponseBody
	public Result<Boolean> do_Login(HttpServletResponse response, @Valid LoginVo loginVo/*, BindingResult error*/){
		log.info(loginVo.toString());
		//参数校验
//		String mobile = loginVo.getMobile();
//		String password = loginVo.getPassword();
//		验证使用JSR303代替
//		if(StringUtils.isEmpty(password)){
//			return Result.error(CodeMsg.PASSWOED_EMPTY);
//		}
//		if(StringUtils.isEmpty(mobile)){
//			return Result.error(CodeMsg.MOBILE_EMPTY);
//		}
//		if(!ValidatorUtil.isMobile(mobile)){
//			return Result.error(CodeMsg.MOBILE_ERROR);
//		}
		/*if(error.hasFieldErrors()){
			FieldError fieldError = error.getFieldError();
			System.out.println("有错误："+fieldError.getDefaultMessage());
		}*/
//		使用@ExceptionHandler拦截异常，并处理
		//登录
		userService.login(response, loginVo);
		return Result.success(true); 
	}

}
