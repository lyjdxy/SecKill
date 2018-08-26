package com.crazy.miaosha.config;

import java.util.List;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
/**
 * ��ͳע��������ķ�ʽ
 * 1. <mvc:annotation-driven>
 *	      <mvc:argument-resolvers>
 *	        <bean class="com.crazy.miaosha.config.UserArgumentResolvers"/>
 *	      </mvc:argument-resolvers>
 *	  </mvc:annotation-driven>
 * 2. <bean class="org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter">
 *		    <property name="userArgumentResolver">
 *		          <bean class="com.crazy.miaosha.config.UserArgumentResolvers"/>
 *		    </property>
 *		</bean>
 * 3.�������õ���spring-boot�ķ�ʽ
 */
@Configuration
//public class WebConfig extends WebMvcConfigurationSupport{	WebMvcConfigurationSupport�̳�������п�
public class WebConfig implements WebMvcConfigurer{
	
	@Autowired
	private UserArgumentResolvers userArgumentResolvers;
	
	@Autowired
	private AccessInterceptor accessInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessInterceptor);
	}
	
	//ע�����������
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(userArgumentResolvers);
	}
	
	/*@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		//ע��UserArgumentResolvers�Ĳ����ֽ���
		resolvers.add(userArgumentResolvers);
		
		super.addArgumentResolvers(argumentResolvers);
	}
	*/
	/**
	 * ��Ϊsb2.x�汾������spring.resources.cache-period=3600���ã�
	 **** ��������spring.resources.cache.period���������������ûЧ��
	 * ҪΪ�ͻ��˾�̬ҳ����ӻ���ʱ��Cache-Control max-age=3600��
	 * 
	 * ����ȸ�鿴Cache-Control��׼ȷ������ʹ�û��
	 * 
	 * ��ȷ�趨������Դ��ʹ�������ļ����Դ������û���ʱ�䡣
     * ָ��������ʱ��Ϊ24Сʱ��
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**/*.htm")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1800, TimeUnit.SECONDS));
    }
}
