package org.seckill.dao;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.entity.SuccessKilled;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml"})
public class SuccessKilledDaoTest {
	
	@Autowired
	private SuccessKilledDao successKilledDao;

	@Test
	public void SuccessKilledDao() throws Exception{
		int insertCount = successKilledDao.insertSuccessKilled(1000L, 18981801687L);
		System.err.println("insertCount="+insertCount);
	}
	
	@Test
	public void queryByIdWithSeckill() throws Exception{
		SuccessKilled successKilled = successKilledDao.queryByIdWithSeckill(1000L, 18981801687L);
		System.err.println(successKilled);
		System.err.println(successKilled.getSeckill());
	}

}
