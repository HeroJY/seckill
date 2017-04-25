package org.seckill.service.impl;

import java.util.Date;
import java.util.List;

import javax.transaction.Transactional;

import org.seckill.dao.SeckillDao;
import org.seckill.dao.SuccessKilledDao;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.seckill.entity.SuccessKilled;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.exception.SeckillException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

/**
 * @author hjy
 *
 */
@Service
public class SeckillServiceImpl implements SeckillService{
	
	private Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillDao seckillDao;
	
	@Autowired
	private SuccessKilledDao successKilledDao;
	
	//md5盐值字符串，用户混淆MD5
	private final String slat ="zxcvbnmasdfghjkl1234567890";

	@Override
	public List<Seckill> getSeckillList() {
		return seckillDao.queryAll(0, 4);
	}

	@Override
	public Seckill getById(long seckillId) {
		return seckillDao.queryById(seckillId);
	}

	@Override
	public Exposer exportSeckillUrl(long seckillId) {
		//优化点：通过redis缓存起来，降低数据库的压力
		/**
		 * 基本缓存控制逻辑
		 * get from cache
		 * if null
		 *  get db
		 * else
		 *  put cache
		 * logic
		 * */
		Seckill seckill = seckillDao.queryById(seckillId);
		
		if(seckill ==null){
			return new Exposer(false, seckillId);
		}
		Date startTime = seckill.getStartTime();
		Date endTime = seckill.getEndTime();
		//系统当前时间
		Date nowTime = new Date();
		if(nowTime.getTime() < startTime.getTime()
				|| nowTime.getTime() > endTime.getTime()){
			return new Exposer(false, seckillId, nowTime.getTime(), startTime.getTime(), endTime.getTime());
		}
		//转化特定字符串的过程，不可逆
		String md5 = getMD5(seckillId); 
		return new Exposer(true, md5, seckillId);
	}
	
	private String getMD5(long seckillId){
		String base = seckillId + "/" +slat;
		String md5 = DigestUtils.md5DigestAsHex(base.getBytes());
		System.err.println(md5);;
		return md5;
	};

	@Override
	@Transactional
	/**
	 * 在这个方法抛出RunTimeException的时候就会回滚，否则就提交
	 */
	public SeckillExecution executeSeckill(long seckillId, long userPhone,
			String md5) throws SeckillException, RepeatKillException,
			SeckillCloseException {
		if(md5 == null || !md5.equals(getMD5(seckillId))){
			throw new SeckillException("seckill data rewrite");
		}
		
		//执行秒杀逻辑：减库存，记录购买行为
		Date nowTime = new Date();
		try {
			int updateCount = seckillDao.reduceNumber(seckillId, nowTime);
			if (updateCount <= 0) {
				//秒杀关闭异常
				throw new SeckillCloseException("秒杀关闭");
			} else {
				//记录购买行为
				int insertCount = successKilledDao.insertSuccessKilled(
						seckillId, userPhone);
				if (insertCount <= 0) {
					//重复秒杀异常
					throw new RepeatKillException("重复秒杀");
				} else {
					//秒杀成功
					SuccessKilled successKilled = successKilledDao
							.queryByIdWithSeckill(seckillId, userPhone);
					return new SeckillExecution(seckillId, SeckillStatEnum.SUCCESS,
							successKilled);
				}
			}
		} catch(SeckillCloseException e1){
			throw e1;
		}catch(RepeatKillException e2){
			throw e2;
		}catch (Exception e) {
			logger.error(e.getMessage(),e);
			//把编译时异常转化为运行时异常
			throw new SeckillException("seckill inner error:" + e.getMessage());
		}
	}

}
