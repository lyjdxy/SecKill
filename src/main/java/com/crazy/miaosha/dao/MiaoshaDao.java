package com.crazy.miaosha.dao;

import org.apache.ibatis.annotations.Mapper;

import com.crazy.miaosha.domain.MiaoshaUser;
import com.crazy.miaosha.domain.OrderInfo;
import com.crazy.miaosha.vo.GoodsVo;

@Mapper
public interface MiaoshaDao {

	OrderInfo miaosha(MiaoshaUser user, GoodsVo goodsVo);

}
