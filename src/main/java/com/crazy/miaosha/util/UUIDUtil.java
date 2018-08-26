package com.crazy.miaosha.util;

import java.util.UUID;

public class UUIDUtil {
	
	//相比起直接使用原生的UUID，这种只是为了去掉生成的UUID中的-而已
	public static String uuid(){
		return UUID.randomUUID().toString().replace("-", "");
	}

}
