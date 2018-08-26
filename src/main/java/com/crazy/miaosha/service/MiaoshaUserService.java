package com.crazy.miaosha.service;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.crazy.miaosha.dao.MiaoshaUserDao;
import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.exception.GlobalException;
import com.crazy.miaosha.redis.MiaoshaUserKey;
import com.crazy.miaosha.redis.RedisService;
import com.crazy.miaosha.result.CodeMsg;
import com.crazy.miaosha.util.MD5Util;
import com.crazy.miaosha.util.UUIDUtil;
import com.crazy.miaosha.vo.LoginVo;

@Service
public class MiaoshaUserService {
	public static final String COOKIE_NAME_TOKEN = "token";

	@Autowired
	private MiaoshaUserDao miaoshaUserDao;
	
	@Autowired
	private RedisService redisService;
	
	public MiaoshaUser getById(Long id){
		//�Ľ�һ�£���ֱ�Ӵ����ݿ��ȡ����
		//redisȡ����
		MiaoshaUser user = redisService.get(MiaoshaUserKey.getById, ""+id, MiaoshaUser.class);
		if(user != null){
			return user;
		}
		//�����ݿ�ȡ��ͬʱ����redis
		user = miaoshaUserDao.getById(id);
		if(user != null){
			redisService.set(MiaoshaUserKey.getById, ""+id, user);
		}
		return user;
	}
	
	public boolean updatePassword(long id, String newPass, String token){
		//�ȴ�redisȡ
		MiaoshaUser user = getById(id);
		if(user == null){
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//�������ݿ�
		MiaoshaUser updateUser = new MiaoshaUser();
		updateUser.setId(id);
		updateUser.setPassword(MD5Util.FormPassToDBPass(newPass, user.getSalt()));
		miaoshaUserDao.updatePass(updateUser);
		//ע�⣺������
		redisService.delete(MiaoshaUserKey.getById,""+id);
		user.setPassword(updateUser.getPassword());
		redisService.set(MiaoshaUserKey.token, token, user);
		return true;
	}
	
	public Boolean login(HttpServletResponse response, LoginVo loginVo){
		if(loginVo == null){
			throw new GlobalException(CodeMsg.SERVICE_ERROR);
		}
		String mobile = loginVo.getMobile();
		String formPass = loginVo.getPassword();
		//�ж��ֻ����Ƿ����
		MiaoshaUser user = getById(Long.parseLong(mobile));
		//System.out.println(user);
		if(user == null){
			throw new GlobalException(CodeMsg.MOBILE_NOT_EXIST);
		}
		//��֤����
		String dbPass = user.getPassword();
		String saltDB = user.getSalt();
		String calePass = MD5Util.FormPassToDBPass(formPass, saltDB);
		if(!calePass.equals(dbPass)){
			throw new GlobalException(CodeMsg.PASSWOED_ERROR);
		}
		//����cookie
		String token = UUIDUtil.uuid();
		addCookie(response, token, user);
		return true;
	}

	//����ҳ��֮ǰ������ȡһ��cookie�е�token
	public MiaoshaUser getByToken(HttpServletResponse response, String token) {
		if(StringUtils.isEmpty(token)){
			return null;
		}
		MiaoshaUser user = redisService.get(MiaoshaUserKey.token, token, MiaoshaUser.class);
		if(user == null){
			return null;
		}
		//ÿ�λ�ȡҲ�ø�����Ч��
		addCookie(response, token, user);
		return user;
	}
	
	private void addCookie(HttpServletResponse response, String token, MiaoshaUser user){
		redisService.set(MiaoshaUserKey.token, token, user);
		Cookie cookie = new Cookie(COOKIE_NAME_TOKEN, token);
		cookie.setMaxAge(MiaoshaUserKey.token.expireSeconds());
		cookie.setPath("/");
		response.addCookie(cookie);
	}
	
}
