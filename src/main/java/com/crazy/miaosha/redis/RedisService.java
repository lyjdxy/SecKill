package com.crazy.miaosha.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

@Service
public class RedisService {
	
	@Autowired
	JedisPool jedisPool;
	
	private void returnToPool(Jedis jedis) {
		if(jedis != null){
			jedis.close();
		}
	}

	//����ת��Ϊ�ַ���
	public static <T> String beanToString(T value){
		if(value == null){
			return null;
		}
		Class<?> clazz = value.getClass();
		if(clazz == int.class || clazz == Integer.class){
			return ""+value;
		}else if(clazz == String.class){
			return (String)value;
		}else if(clazz == long.class || clazz == Long.class){
			return ""+value;
		}else{
			return JSON.toJSONString(value);
		}
	}
	
	//�ַ���ת��Ϊ����
	@SuppressWarnings("unchecked")
	public static <T> T StringToBean(String str, Class<T> clazz) {
		if(str == null || str.length() <= 0 || clazz == null){
			return null;
		}
		if(clazz == int.class || clazz == Integer.class){
			return (T) Integer.valueOf(str);
		}else if(clazz == String.class){
			return (T) str;
		}else if(clazz == long.class || clazz == Long.class){
			return (T) Long.valueOf(str);
		}else{
			return JSON.toJavaObject(JSON.parseObject(str), clazz);
		}
	}
	/**
	 *  ������redis�ķ���
	 */
	//set����
	public <T>boolean set(KeyPrefix prefix, String key, T value) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String str = beanToString(value);
			if(str == null || str.length() <= 0){
				return false;
			}
			//����������Key
			String realKey = prefix.getPrefix() + key;
			int seconds = prefix.expireSeconds();
			if(seconds <= 0){
				jedis.set(realKey, str);
			}else{
				jedis.setex(realKey, seconds, str);
			}
			return true;
		} finally{
			returnToPool(jedis);
		}
	}
	//get����
	public <T> T get(KeyPrefix prefix, String key, Class<T> clazz) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//����������Key
			String realKey = prefix.getPrefix() + key;
			String str = jedis.get(realKey);
			T t = StringToBean(str , clazz);
			return t;
		} finally{
			returnToPool(jedis);
		}
	}
	
	
	//incr����
	public <T> Long incr(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//����������Key
			String realKey = prefix.getPrefix() + key;
			return jedis.incr(realKey);
		} finally{
			returnToPool(jedis);
		}
	}
	//decr����
	public <T> Long decr(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//����������Key
			String realKey = prefix.getPrefix() + key;
			return jedis.decr(realKey);
		} finally{
			returnToPool(jedis);
		}
	}
	//exists����
	public <T> boolean exists(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//����������Key
			String realKey = prefix.getPrefix() + key;
			return jedis.exists(realKey);
		} finally{
			returnToPool(jedis);
		}
	}
	//delete����
	public <T> boolean delete(KeyPrefix prefix, String key) {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			//����������Key
			String realKey = prefix.getPrefix() + key;
			long ret =  jedis.del(realKey);
			return ret > 0;
		} finally{
			returnToPool(jedis);
		}
	}
	//delete����
	public <T> String flushDB() {
		Jedis jedis = null;
		try {
			jedis = jedisPool.getResource();
			String result = jedis.flushDB();
			return result;
		} finally{
			returnToPool(jedis);
		}
	}
	
}
