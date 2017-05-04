package com.asiainfo.redis.util;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年4月5日  上午11:17:23
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class RedisThreadPoolTools {
	
	private static final Logger logger = LoggerFactory.getLogger(RedisThreadPoolTools.class);
	
	private final ExecutorService service;
	
	private RedisThreadPoolTools() {
		this.service = Executors.newFixedThreadPool(10);
	}
	
	private RedisThreadPoolTools(int size) {
		this.service = Executors.newFixedThreadPool(size);
	}
	
	static class RedisThreadPoolHolder {
		static RedisThreadPoolTools INSTANCE = new RedisThreadPoolTools();
	}
	
	//并发发送线程池
	public static RedisThreadPoolTools getInstance() {
		return RedisThreadPoolHolder.INSTANCE;
	}
	
	public void execute(Runnable r) {
		try {
			this.service.execute(r);
		} catch (Exception ex) {
			logger.error("线程调度发生异常，异常信息如下：\n{}", ex);
		}
	}
}
