package com.crazy.miaosha.redis;

public abstract class BaseKeyPrefix implements KeyPrefix{

	private int expireSeconds;
	private String prifix;
	
	public BaseKeyPrefix(String prifix){//0������������
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
			return 0;//0Ĭ����������
		}
	}
	
	public String getPrefix() {
		//String className = getClass().getSimpleName();//prefixĬ�ϸ�ʽ�ǣ� ����_
		return this.prifix+"_";
	}
	
}
