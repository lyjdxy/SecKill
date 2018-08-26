package com.crazy.miaosha.config;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.service.MiaoshaUserService;

/**
 * ���������������뷽��ǰ�Դ���Ĳ������д���
 */
@SuppressWarnings("unused")
@Service
public class UserArgumentResolvers implements HandlerMethodArgumentResolver {
	
	@Autowired
	private MiaoshaUserService userService;
	
	/**
	 * ���жϣ�ͨ���˲Ż�ִ��resolveArgument
	 */
	public boolean supportsParameter(MethodParameter arg0) {
		Class<?> clazz = arg0.getParameterType();
		return clazz == MiaoshaUser.class;
	}
	
	/**
	 *  �������������
	 */
	public Object resolveArgument(MethodParameter param, ModelAndViewContainer mvcContainer,
			NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
		
		/*����AccessIntercept������ִ��
		HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
		HttpServletResponse response = webRequest.getNativeResponse(HttpServletResponse.class);
		
		String paramToken = request.getParameter(MiaoshaUserService.COOKIE_NAME_TOKEN);
		String cookieToken = getCookieValue(request, MiaoshaUserService.COOKIE_NAME_TOKEN);
		if(StringUtils.isEmpty(paramToken)&&StringUtils.isEmpty(cookieToken)){
			return null;
		}
		String token = StringUtils.isEmpty(paramToken)?cookieToken:paramToken;
		*/
		return UserThreadLocal.getUser();//?????ִ?ʱ????hreadLocal??
	}

	/*����AccessIntercept������ִ��
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
	 */
}
