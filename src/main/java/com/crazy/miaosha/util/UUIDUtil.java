package com.crazy.miaosha.util;

import java.util.UUID;

public class UUIDUtil {
	
	//�����ֱ��ʹ��ԭ����UUID������ֻ��Ϊ��ȥ�����ɵ�UUID�е�-����
	public static String uuid(){
		return UUID.randomUUID().toString().replace("-", "");
	}

}
