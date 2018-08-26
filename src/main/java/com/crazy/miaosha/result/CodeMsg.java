package com.crazy.miaosha.result;

public class CodeMsg {

	private int code;
	private String msg;
	
	//ͨ�ô���5001XX
	public static CodeMsg SUCCESS = new CodeMsg(0, "success");
	public static CodeMsg SERVICE_ERROR = new CodeMsg(500100, "������쳣");
	public static CodeMsg BINDING_ERROR = new CodeMsg(500101, "���������쳣��%s");
	public static CodeMsg REQUEST_ILLEGAL = new CodeMsg(500102, "����Ƿ�����ɱ��ַ��֤ʧЧ");
	public static CodeMsg ACCESS_LIMIT = new CodeMsg(500103, "���ʹ���Ƶ�������Ժ��ڲ���");
	//��¼���5002XX
	public static CodeMsg SESSION_ERROR = new CodeMsg(500201, "sessionʧЧ���û���Ϣ�ѹ��ڣ������µ�¼");
	public static CodeMsg PASSWOED_EMPTY = new CodeMsg(500201, "��¼���벻��Ϊ��");
	public static CodeMsg MOBILE_EMPTY = new CodeMsg(500202, "�ֻ��Ų���Ϊ��");
	public static CodeMsg MOBILE_ERROR = new CodeMsg(500203, "�ֻ��Ÿ�ʽ����");
	public static CodeMsg MOBILE_NOT_EXIST = new CodeMsg(500204, "�ֻ��Ų�����");
	public static CodeMsg PASSWOED_ERROR = new CodeMsg(500205, "�������");
	
	//�������500300
	public static CodeMsg ORDER_NOT_EXIST = new CodeMsg(500300, "����������");
	
	//��ɱ���5005XX
	public static CodeMsg MIAOSHA_OVER = new CodeMsg(500500, "����Ʒ���Ϊ�գ���ɱ����");
	public static CodeMsg MIAOSHA_REPEATE = new CodeMsg(500501, "ͬһ�û������ظ���ɱ");
	public static CodeMsg MIAOSHA_ERROR = new CodeMsg(500502, "��ɱʧ��");
	public static CodeMsg VERIFYCODE_ERROR = new CodeMsg(500502, "��֤�����");
	public static CodeMsg VERIFYCODE_EMPTY = new CodeMsg(500502, "��֤�벻��Ϊ��");
	public static CodeMsg VERIFYCODE_STYLE = new CodeMsg(500502, "��������ȷ����֤��");
	
	/**
	 * ��������CodeMsg 
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
