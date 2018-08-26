package com.crazy.miaosha.result;

public class CodeMsg {

	private int code;
	private String msg;
	
	//通用错误：5001XX
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
	public static CodeMsg SERVICE_ERROR = new CodeMsg(500100, "服务端异常");
	public static CodeMsg BINDING_ERROR = new CodeMsg(500101, "参数检验异常：%s");
	public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102, "请求非法，秒杀地址验证失效");
	public static CodeMsg ACCESS_LIMIT = new CodeMsg(500103, "访问过于频繁，请稍后在操作");
	//登录相关5002XX
	public static CodeMsg SESSION_ERROR = new CodeMsg(500201, "session失效，用户信息已过期，请重新登录");
	public static CodeMsg PASSWOED_EMPTY = new CodeMsg(500201, "登录密码不能为空");
	public static CodeMsg MOBILE_EMPTY = new CodeMsg(500202, "手机号不能为空");
	public static CodeMsg MOBILE_ERROR = new CodeMsg(500203, "手机号格式错误");
	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500204, "手机号不存在");
	public static CodeMsg PASSWOED_ERROR = new CodeMsg(500205, "密码错误");
	
	//订单相关500300
	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500300, "订单不存在");
	
	//秒杀相关5005XX
	public static CodeMsg MIAOSHA_OVER = new CodeMsg(500500, "该商品库存为空，秒杀结束");
	public static CodeMsg MIAOSHA_REPEATE = new CodeMsg(500501, "同一用户不能重复秒杀");
	public static CodeMsg MIAOSHA_ERROR = new CodeMsg(500502, "秒杀失败");
	public static CodeMsg VERIFYCODE_ERROR = new CodeMsg(500502, "验证码错误");
	public static CodeMsg VERIFYCODE_EMPTY = new CodeMsg(500502, "验证码不能为空");
	public static CodeMsg VERIFYCODE_STYLE = new CodeMsg(500502, "请输入正确的验证码");
	
	/**
	 * 带参数的CodeMsg 
	 */
	public CodeMsg fillArgs(Object... args){
		int code = this.code;
		String message = String.format(this.msg, args);
		return new CodeMsg(code, message);
	}
	
	private CodeMsg(int code, String msg) {
		this.code = code;
		this.msg = msg;
	}
	
	public int getCode() {
		return code;
	}
	public void setCode(int code) {
		this.code = code;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}
	
	
}
