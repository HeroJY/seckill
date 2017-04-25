package org.seckill.service;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.entity.Seckill;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration({"classpath:spring/spring-dao.xml","classpath:spring/spring-service.xml"})
public class SeckillServiceTest {
	private final Logger logger = LoggerFactory.getLogger(this.getClass());
	
	@Autowired
	private SeckillService seckillService;

	@Test
	public void testGetSeckillList() throws Exception{
		List<Seckill> seckills = seckillService.getSeckillList();
		logger.info("list={}",seckills);
	}
	
	@Test
	public void testGetById() throws Exception{
		long id=1000;
		Seckill seckill = seckillService.getById(id);
		logger.info("seckill={}",seckill);
	}
	
	@Test
	public void testExportSeckillUrl() throws Exception{
		long id=1000;
		Exposer exposer = seckillService.exportSeckillUrl(id);
		logger.info("exposer={}",exposer);
	}
	
	@Test
	public void testExecuteSeckill() throws Exception{
		long id=1000L;
		long phone=13550122L;
		String md5 = "5e109936c14ced2cf34e225c999bb13f";
		SeckillExecution seckillExecution = seckillService.executeSeckill(id, phone, md5);
		logger.info("seckillExecution={}",seckillExecution);
	}
}
