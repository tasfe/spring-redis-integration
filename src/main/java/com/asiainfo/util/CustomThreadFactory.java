package com.asiainfo.util;

import java.util.concurrent.ThreadFactory;

/**
 * @Description: TODO
 * 
 * @author       zq
 * @date         2017年10月16日  下午4:08:08
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public class CustomThreadFactory implements ThreadFactory {

	private int counter = 0;
	private String prefix = "default";
	public CustomThreadFactory() {}
	public CustomThreadFactory(String prefix) {
		this.prefix = prefix;
	}
	   
	/* 
	 * @Description: TODO
	 * @param r
	 * @return
	 * @see java.util.concurrent.ThreadFactory#newThread(java.lang.Runnable)
	 */
	@Override
	public Thread newThread(Runnable r) {
		return new Thread(r, prefix + "-" + counter++);
	}
}
