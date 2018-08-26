package com.crazy.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
	
}
/**	问题：静态资源加载不了？
 * 		   
 * 	原因： 本项目中使用到了拦截器，因而使用了配置类生成拦截器，在配置类中继承了WebMvcConfigurerAdapter这个类，
 * 		但因为此类已被标注过时，所以使用了WebMvcConfigurationSupport类来代替，
 * 		但发现如果继承了WebMvcConfigurationSupport，则在yml中配置的相关内容会失效。
 * 		因此使得Spring boot的自动配置的静态资源路径失效。
 * 
 * 	解决：https://blog.csdn.net/wilsonsong1024/article/details/80176285
 * 		方法一：WebMvcConfigurerAdapter类只含有对WebMvcConfigurer接口的空实现，
 * 			因此我们可以直接使配置类实现WebMvcConfigurer接口，不必继承这两个类。
 * 
 * 		
 * 		方法二： 如果非要继承WebMvcConfigurationSupport，就需要重新指定静态资源
			 * @Override
			protected void addResourceHandlers(ResourceHandlerRegistry registry) {
			    registry.addResourceHandler("/**")
			            .addResourceLocations("classpath:/META-INF/resources/")
			            .addResourceLocations("classpath:/resources/")
			            .addResourceLocations("classpath:/static/")
			            .addResourceLocations("classpath:/templates/");
			    super.addResourceHandlers(registry);
			}
 */
