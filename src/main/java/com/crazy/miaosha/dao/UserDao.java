package com.crazy.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import com.crazy.miaosha.domain.User;

@Mapper
public interface UserDao {

	@Select("select * from user where id = #{id}")
	public User getById(@Param("id")Integer id);
	
}
