package com.crazy.miaosha;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class MainApplication {
	
	public static void main(String[] args) {
		SpringApplication.run(MainApplication.class, args);
	}
	
}
/**	���⣺��̬��Դ���ز��ˣ�
 * 		   
 * 	ԭ�� ����Ŀ��ʹ�õ��������������ʹ�������������������������������м̳���WebMvcConfigurerAdapter����࣬
 * 		����Ϊ�����ѱ���ע��ʱ������ʹ����WebMvcConfigurationSupport�������棬
 * 		����������̳���WebMvcConfigurationSupport������yml�����õ�������ݻ�ʧЧ��
 * 		���ʹ��Spring boot���Զ����õľ�̬��Դ·��ʧЧ��
 * 
 * 	�����https://blog.csdn.net/wilsonsong1024/article/details/80176285
 * 		����һ��WebMvcConfigurerAdapter��ֻ���ж�WebMvcConfigurer�ӿڵĿ�ʵ�֣�
 * 			������ǿ���ֱ��ʹ������ʵ��WebMvcConfigurer�ӿڣ����ؼ̳��������ࡣ
 * 
 * 		
 * 		�������� �����Ҫ�̳�WebMvcConfigurationSupport������Ҫ����ָ����̬��Դ
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
