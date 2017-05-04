package com.asiainfo.redis.task;

import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.asiainfo.redis.IExecutor;
import com.asiainfo.redis.IRedisDao;
import com.asiainfo.redis.util.ServiceUtil;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年4月5日  下午4:04:55
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RedisExecuteTask implements Runnable {

	private static final Logger logger = LoggerFactory.getLogger(RedisExecuteTask.class);
	
	private IRedisDao redisDao;
	private IExecutor executor;
	private int retry;
	
	public RedisExecuteTask(IRedisDao redisDao, IExecutor executor, int retry) {
		this.redisDao = redisDao;
		this.executor = executor;
		this.retry = retry;
	}
	
	/* 
	 * @Description: TODO
	 * @see java.lang.Runnable#run()
	 */
	@Override
	public void run() {
		
		logger.debug("执行redis异步任务，参数为retry={} ......", this.retry);
		for (int i = 0; i < this.retry; i++) {
			try {
				this.executor.execute(this.redisDao);
				return;
			} catch (Exception ex) {
				logger.error("执行redis异步任务时出现异常，异常信息：\n{}", ex);
			}
			if ((i + 1) < this.retry) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("执行redis异步任务时出现异常，连续尝试{}次未能成功 ......", this.retry);
	}
}
