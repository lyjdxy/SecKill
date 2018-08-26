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
 * 传统注入解析器的方式
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
 * 3.本例采用的是spring-boot的方式
 */
@Configuration
//public class WebConfig extends WebMvcConfigurationSupport{	WebMvcConfigurationSupport继承这个类有坑
public class WebConfig implements WebMvcConfigurer{
	
	@Autowired
	private UserArgumentResolvers userArgumentResolvers;
	
	@Autowired
	private AccessInterceptor accessInterceptor;
	
	@Override
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(accessInterceptor);
	}
	
	//注册参数解析器
	public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		resolvers.add(userArgumentResolvers);
	}
	
	/*@Override
	protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
		//注册UserArgumentResolvers的参数分解器
		resolvers.add(userArgumentResolvers);
		
		super.addArgumentResolvers(argumentResolvers);
	}
	*/
	/**
	 * 因为sb2.x版本舍弃了spring.resources.cache-period=3600配置，
	 **** 更换成了spring.resources.cache.period这个能启动，但是没效果
	 * 要为客户端静态页面添加缓存时间Cache-Control max-age=3600，
	 * 
	 * 另外谷歌查看Cache-Control不准确，建议使用火狐
	 * 
	 * 精确设定缓存资源。使用配置文件可以粗略设置缓存时间。
     * 指定，缓存时间为24小时。
	 */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry
                .addResourceHandler("/**/*.htm")
                .addResourceLocations("classpath:/static/")
                .setCacheControl(CacheControl.maxAge(1800, TimeUnit.SECONDS));
    }
}
