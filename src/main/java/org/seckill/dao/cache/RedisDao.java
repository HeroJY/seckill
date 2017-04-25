package org.seckill.dao.cache;

import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

/**
 * @author hjy
 *
 */
public class RedisDao {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	//类似于数据库连接池的pool
	private final JedisPool jedisPool;
	
	public RedisDao(String ip, int port){
		jedisPool = new JedisPool(ip,port);
	}
	
	//从redis中获取Seckill对象
	public Seckill getSeckill(long seckillId){
		//redis操作逻辑
		try {
			Jedis jedis = jedisPool.getResource();
			try {
				String key = "seckill:" + seckillId;
				//并没有实现内部序列化操作
				//get->byte[]->反序列化->Object(Seckill)
				
			} finally {
				jedis.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
	
	//把Seckill对象放入redis中
	public String putSeckill(Seckill seckill){
		
		return null;
	}

}
