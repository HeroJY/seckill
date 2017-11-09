package org.seckill.web;

import java.util.Date;
import java.util.List;

import javafx.application.Application;
import org.seckill.dto.Exposer;
import org.seckill.dto.SeckillExecution;
import org.seckill.dto.SeckillResult;
import org.seckill.entity.Seckill;
import org.seckill.enums.SeckillStatEnum;
import org.seckill.exception.RepeatKillException;
import org.seckill.exception.SeckillCloseException;
import org.seckill.service.SeckillService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpSession;

/**
 * @author hjy
 * 秒杀操作controller
 */
@Controller
@RequestMapping("/seckill")//url:/模块/资源/{id}/细分
public class SeckillController {
	
	private final Logger logger = LoggerFactory.getLogger(this.getClass());


	@Autowired
	private SeckillService seckillService;

	/**
	 * 秒杀商品列表页面Controller
	 * @param model
	 * @return
	 */
	@RequestMapping(value="/list",method = RequestMethod.GET)
	public String list(Model model){
		//获取页面列表
		List<Seckill> seckills = seckillService.getSeckillList();
		model.addAttribute("list", seckills);
		//返回list.jsp+model=ModelAndView
		///WEB-INF/jsp/list.jsp
		return "list";
	}
	
	//秒杀商品详情页面Controller
	@RequestMapping(value="/{seckillId}/detail",method = RequestMethod.GET)
	public String detail(@PathVariable("seckillId") Long seckillId,Model model){
		if(seckillId == null){
			return "redirect:/seckill/list";
		}
		Seckill seckill = seckillService.getById(seckillId);
		if(seckill == null){
			return "forwoard:/seckill/list";
		}
		model.addAttribute("seckill",seckill);
		return "detail";
	}


	/**
	 * 	根据商品Id提供md5加密后的独一无二的访问url Controller
	 * 	ajax json接口所以不需要Model
	 * 	produces告诉浏览器返回类型，同时解决页面乱码问题
	 * @param seckillId
	 * @return
	 */
	@RequestMapping(value="/{seckillId}/exposer",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody//SpringMVC扫描到该注解会自动把返回类型封装成json
	public SeckillResult<Exposer> exporser(@PathVariable("seckillId") Long seckillId){
		SeckillResult<Exposer> result;
		try {
			Exposer exposer = seckillService.exportSeckillUrl(seckillId);
			result = new SeckillResult<Exposer>(true, exposer);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			result  = new SeckillResult<Exposer>(false,e.getMessage());
		}
		return result;
	}

	/**
	 * 通过独一无二的url 执行秒杀逻辑Controller
	 * @param seckillId
	 * @param md5
	 * @param userPhone
	 * @return
	 */
	@RequestMapping(value="/{seckillId}/{md5}/execution",method = RequestMethod.POST,produces = {"application/json;charset=UTF-8"})
	@ResponseBody
	//@CookieValue中如果不设置required = false那么只要请求的RequestHeader中没有killPhone那么就会报错
	public SeckillResult<SeckillExecution> execute(@PathVariable("seckillId")Long seckillId, 
			@PathVariable("md5")String md5, @CookieValue(value = "killPhone", required = false)Long userPhone){
		if(userPhone == null){
			return new SeckillResult<SeckillExecution>(false, "未注册");
		}
		try {
			SeckillExecution seckillExecution = seckillService.executeSeckill(seckillId, userPhone, md5);
			//因为是应该抛出的异常，需要让前台显示，所以返回true
			return new SeckillResult<SeckillExecution>(true, seckillExecution);
		}catch (RepeatKillException e) {
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.REPEATE_KILL);
			return new SeckillResult<SeckillExecution>(true, execution);
		}catch (SeckillCloseException e) {
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.END);
			return new SeckillResult<SeckillExecution>(true, execution);
		}catch (Exception e) {
			logger.error(e.getMessage(), e);
			SeckillExecution execution = new SeckillExecution(seckillId, SeckillStatEnum.INNER_ERROR);
			return new SeckillResult<SeckillExecution>(false, execution);
		}
	}

	/**
	 * 返回系统时间Controller
	 * @return
	 */
	@RequestMapping(value = "/time/now",method=RequestMethod.GET)
	@ResponseBody
	public SeckillResult<Long> time(){
		Date now = new Date();
		//getTime()是转化为毫秒，与前台好比较
		return new SeckillResult<Long>(true,now.getTime());
	}
}
