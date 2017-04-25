package org.seckill.dao;


import java.util.Date;
import java.util.List;

import org.apache.ibatis.annotations.Param;
import org.seckill.entity.Seckill;

public interface SeckillDao {
	
	/**
	 * 减库存
	 * @param seckillId
	 * @param killTime
	 * @return 如果影响行数大于1，表示更新的记录行数
	 */
	int reduceNumber(@Param("seckillId")long seckillId, @Param("killTime")Date killTime);
	
	/**
	 * 根据Id查询秒杀对象
	 * @param seckillId
	 * @return
	 */
	Seckill queryById(long seckillId);
	
	/**
	 * 根据偏移量查询秒杀商品列表(主要做分页用)
	 * @param offet（偏移量）
	 * @param limit（偏移量之后的条数）
	 * @return
	 */
	//因为mybatis不能保存形参的名字，所以需要告诉mybatis我入参的名字，这样才能在mapper的xml里面取到对应的参数
	List<Seckill> queryAll(@Param("offset")int offset, @Param("limit")int limit);
}
