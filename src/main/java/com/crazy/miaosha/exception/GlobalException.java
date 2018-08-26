package com.crazy.miaosha.exception;

import com.crazy.miaosha.result.CodeMsg;

public class GlobalException extends RuntimeException{

	private static final long serialVersionUID = 1L;
	
	private CodeMsg cm;
	
	public GlobalException(CodeMsg cm){
		super();
		this.cm = cm;
	}
	
	public CodeMsg getCm() {
		return cm;
	}
	
	

}
