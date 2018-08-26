package com.crazy.miaosha.exception;

import java.util.List;

import org.springframework.validation.BindException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.result.Result;

@ControllerAdvice
@ResponseBody
public class GlobalExceptionHandler {

	@ExceptionHandler
	public Result<String> exceptionHandler(Exception e){
		e.printStackTrace();
		if(e instanceof GlobalException){
			GlobalException ge = (GlobalException) e;
			return Result.error(ge.getCm());
		}else if(e instanceof BindException){
			BindException be = (BindException) e;
			List<ObjectError> allErrors = be.getAllErrors();
			ObjectError objectError = allErrors.get(0);
			String message = objectError.getDefaultMessage();
			return Result.error(CodeMsg.BINDING_ERROR.fillArgs(message));
		}else{
			return Result.error(CodeMsg.SERVICE_ERROR);
		}
		
	}
	
}
