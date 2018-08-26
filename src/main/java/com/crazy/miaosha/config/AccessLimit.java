package com.crazy.miaosha.config;

import java.lang.annotation.Retention;
import static java.lang.annotation.ElementType.METHOD;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(METHOD)
public @interface AccessLimit {

	int seconds();
	int maxCount();
	boolean needLogin() default true;
	
}
