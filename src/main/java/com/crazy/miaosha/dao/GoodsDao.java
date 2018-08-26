package com.crazy.miaosha.dao;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.crazy.miaosha.domain.MiaoshaGoods;
import com.crazy.miaosha.vo.GoodsVo;

@Mapper
public interface GoodsDao {

	@Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date"
			+ " from miaosha_goods mg left join goods g "
			+ "on mg.goods_id = g.id")
	List<GoodsVo> listGoodsVo();

	@Select("select g.*,mg.miaosha_price,mg.stock_count,mg.start_date,mg.end_date"
			+ " from miaosha_goods mg left join goods g "
			+ "on mg.goods_id = g.id where mg.goods_id = #{goodsId}")
	GoodsVo getGoodsVoByGoodsId(@Param("goodsId")long goodsId);

	@Update("update miaosha_goods set stock_count = stock_count - 1 where goods_id = #{goodsId} and stock_count > 0")
	boolean reduceStock(@Param("goodsId")long goodsId);

	@Update("update miaosha_goods set stock_count = #{stockCount} where goods_id = #{goodsId}")
	public int resetStock(MiaoshaGoods g);
	
}
