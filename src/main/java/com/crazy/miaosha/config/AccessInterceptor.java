package com.crazy.miaosha.config;

import java.io.IOException;
import java.io.OutputStream;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;





import com.alibaba.fastjson.JSON;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.redis.AccessKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.result.Result;
import com.crazy.miaosha.service.MiaoshaUserService;

@Service
public class AccessInterceptor extends HandlerInterceptorAdapter{
	
	@Autowired
	private MiaoshaUserService userService;
	
	@Autowired
	private RedisService redisService;
	
	@Override
	public boolean preHandle(HttpServletRequest request,
			HttpServletResponse response, Object handler) throws Exception {
		
		request.setCharacterEncoding("UTF-8");
		
		if(handler instanceof HandlerMethod){
			MiaoshaUser user = getUser(request, response);
			UserThreadLocal.setUser(user);//保存到ThreadLocal中
			
			HandlerMethod hm = (HandlerMethod) handler;
			AccessLimit accessLimit = hm.getMethodAnnotation(AccessLimit.class);//获得方法的注解
			if(accessLimit == null){
				return true;
			}
			int seconds = accessLimit.seconds();
			int maxCount = accessLimit.maxCount();
			boolean needLogin = accessLimit.needLogin();
			String key = request.getRequestURI();
			if(needLogin){
				if(user == null){
					WarnClient(response, Result.error(CodeMsg.SESSION_ERROR));//这里报错了，得告知浏览器
					return false;
				}
				key += "_" + user.getId();
			}
			AccessKey ak = AccessKey.withExpire(seconds);
			Integer alreadyCount = redisService.get(ak, key, Integer.class);
			if(alreadyCount == null){
				redisService.set(ak, key, 1);
			}else if(alreadyCount < maxCount){
				redisService.incr(ak, key);
			}else{
				WarnClient(response, Result.error(CodeMsg.ACCESS_LIMIT));
				return false;
			}
			
			
			
			
		}
		return true;
	}
	
	private void WarnClient(HttpServletResponse response, Result<Object> result) throws IOException {
		OutputStream out = response.getOutputStream();
		String cmStr = JSON.toJSONString(result);
		out.write(cmStr.getBytes("UTF-8"));
		out.flush();
		out.close();
	}

	private MiaoshaUser getUser(HttpServletRequest request, HttpServletResponse response){
		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
		if(StringUtils.isEmpty(paramToken)&&StringUtils.isEmpty(cookieToken)){
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		return userService.getByToken(response, token);
	}
	private String getCookieValue(HttpServletRequest request, String cookieName) {
		Cookie[] cookies = request.getCookies();
		
		if(cookies == null || cookies.length <= 0){
			return null;
		}
		
		for(Cookie c : cookies){
			if(c.getName().equals(cookieName)){
				return c.getValue();
			}
		}
		return null;
	}
}
