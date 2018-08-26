package com.crazy.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.crazy.miaosha.domain.MiaoshaUser;

@Mapper
public interface MiaoshaUserDao {

	@Select("select * from miaosha_user where id = #{id}")
	MiaoshaUser getById(@Param("id")Long id);

	@Update("update from miaosha_user set password = #{password} where id = #{id}")
	void updatePass(MiaoshaUser updateUser);
}
