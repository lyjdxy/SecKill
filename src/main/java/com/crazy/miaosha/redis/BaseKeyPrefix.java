package com.crazy.miaosha.redis;

public abstract class BaseKeyPrefix implements KeyPrefix{

	private int expireSeconds;
	private String prifix;
	
	public BaseKeyPrefix(String prifix){//0代表永不过期
		this(0, prifix);
	}
	
	public BaseKeyPrefix(int expireSeconds, String prifix){
		this.expireSeconds = expireSeconds;
		this.prifix = prifix;
	}
	
	public int expireSeconds() {
		if(this.expireSeconds != 0){
			return this.expireSeconds;
		}else{
			return 0;//0默认永不过期
		}
	}
	
	public String getPrefix() {
		//String className = getClass().getSimpleName();//prefix默认格式是： 类名_
		return this.prifix+"_";
	}
	
}
