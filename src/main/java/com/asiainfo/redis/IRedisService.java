package com.asiainfo.redis;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @Description: redis服务层接口
 * 
 * @author       zq
 * @date         2017年3月19日  上午9:28:12
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
public interface IRedisService {
	
	/**
	 * @Description: 保存字符串值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setString(String key, String value);
	
	/**
	 * @Description: 保存字符串值，设置过期时间
	 * 
	 * @param key
	 * @param value
	 * @param expire	过期时间，单位秒
	 * @return
	 */
	public boolean setString(String key, String value, long expire);

	/**
	 * @Description: 保存字符串值，设置同步/异步方式、重试次数
	 * 
	 * @param key
	 * @param value
	 * @param syn		true=同步保存、false=异步保存
	 * @param retry		获取不到redis连接时，重试次数
	 * @param service	异步使用的线程池，syn=false时才有效，syn=true时该参数可为null
	 * @return
	 */
	public boolean setString(String key, String value, boolean syn, int retry, ExecutorService service);
	
	/**
	 * @Description: 保存字符串值，设置过期时间、同步/异步方式、重试次数
	 * 
	 * @param key
	 * @param value
	 * @param expire	过期时间，单位秒
	 * @param syn		true=同步保存、false=异步保存
	 * @param retry		获取不到redis连接时，重试次数
	 * @param service	异步使用的线程池，syn=false时才有效，syn=true时该参数可为null
	 * @return
	 */
	public boolean setString(String key, String value, long expire, boolean syn, int retry, ExecutorService service);
	
	/**
	 * @Description: 保存对象值
	 * 
	 * @param key
	 * @param value
	 * @return
	 */
	public boolean setObject(String key, Object value);
	
	/**
	 * @Description: 保存对象值，设置过期时间
	 * 
	 * @param key
	 * @param value
	 * @param expire	过期时间，单位秒
	 * @return
	 */
	public boolean setObject(String key, Object value, long expire);
	
	/**
	 * @Description: 保存对象值，设置同步/异步方式、重试次数
	 * 
	 * @param key
	 * @param value
	 * @param syn		true=同步保存、false=异步保存
	 * @param retry		获取不到redis连接时，重试次数
	 * @param service	异步使用的线程池，syn=false时才有效，syn=true时该参数可为null
	 * @return
	 */
	public boolean setObject(String key, Object value, boolean syn, int retry, ExecutorService service);
	
	/**
	 * @Description: 保存对象值，设置过期时间、同步/异步方式、重试次数
	 * 
	 * @param key
	 * @param value
	 * @param expire	过期时间，单位秒
	 * @param syn		true=同步保存、false=异步保存
	 * @param retry		获取不到redis连接时，重试次数
	 * @param service	异步使用的线程池，syn=false时才有效，syn=true时该参数可为null
	 * @return
	 */
	public boolean setObject(String key, Object value, long expire, boolean syn, int retry, ExecutorService service);
	
	/**
	 * @Description: 对象值获取方法
	 * 
	 * @param key
	 * @return
	 */
	public Object getObject(String key);
	
	/**
	 * @Description: 对象值获取方法，设置重试次数
	 * 
	 * @param key
	 * @param retry
	 * @return
	 */
	public Object getObject(String key, int retry);
	
	/**
	 * @Description: 字符串值获取方法
	 * 
	 * @param key
	 * @return
	 */
	public String getString(String key);
	
	/**
	 * @Description: 字符串值获取方法，设置重试次数
	 * 
	 * @param key
	 * @param retry
	 * @return
	 */
	public String getString(String key, int retry);
	
	/**
	 * @Description: 删除方法
	 * 
	 * @param key
	 * @param retry
	 * @return
	 */
	public boolean remove(String key, int retry);
	
	/**
	 * @Description: 删除方法，设置同步/异步方式、重试次数，异步方式时不保证一定能删除，返回true表示已开启线程进行删除。
	 * 
	 * @param key
	 * @param syn		true=同步删除、false=异步删除
	 * @param retry		获取不到redis连接时，重试次数
	 * @param service	异步使用的线程池，syn=false时才有效，syn=true时该参数可为null
	 * @return
	 */
	public boolean remove(String key, boolean syn, int retry, ExecutorService service);
	
	/**
	 * @Description: 获取锁，成功返回锁id（用于释放锁），失败返回null
	 * 
	 * @param key		锁名称
	 * @return
	 */
	public String acquireLock(String key);
	
	/**
	 * @Description: 获取锁，成功返回锁id（用于释放锁），失败返回null
	 * 
	 * @param key		锁名称
	 * @param expire	锁的过期时间（占用时间，超过自动释放），单位秒
	 * @return			成功时返回锁id，失败时返回null
	 */
	public String acquireLock(String key, long expire);
	
	/**
	 * @Description: 获取锁，一直获取直到超出timeout指定的时间，成功返回锁id（用于释放锁），失败返回null
	 * 
	 * @param key		锁名称
	 * @param timeout	获取锁的超时时间
	 * @param unit		超时时间单位
	 * @return			成功时返回锁id，失败时返回null
	 */
	public String acquireLock(String key, long timeout, TimeUnit unit);
	
	/**
	 * @Description: 获取锁，一直获取直到超出timeout指定的时间，成功返回锁id（用于释放锁），失败返回null
	 * 
	 * @param key		锁名称
	 * @param expire	锁的过期时间（占用时间，超过自动释放），单位秒
	 * @param timeout	获取锁的超时时间
	 * @param unit		超时时间单位
	 * @return			成功时返回锁id，失败时返回null
	 */
	public String acquireLock(String key, long expire, long timeout, TimeUnit unit);
	
	/**
	 * @Description: 强制释放锁，该方法有风险（如果锁已过期，又被其他用户获取时，该方法会释放锁，可能会造成并发bug），不提供异步方式
	 * 
	 * @param key	锁名称
	 * @return		释放成功返回true，失败返回false
	 */
	public boolean releaseLock(String key);
	
	/**
	 * @Description: 释放锁，只释放指定id的锁
	 * 
	 * @param key		锁名称
	 * @param lockId	锁id（为空时表示强制释放，不为空时必须与当前锁的值一样才会释放）
	 * @return		释放成功返回true，失败返回false
	 */
	public boolean releaseLock(String key, String lockId);
	
	/**
	 * @Description: 释放锁，只释放指定id的锁，提供同步/异步方式，通常异步方式用于有过期时间的锁（没有设置过期时间的锁不建议用异步方式）
	 * 
	 * @param key		锁名称
	 * @param lockId	锁id（为空时表示强制释放，此时异步失效，不为空时必须与当前锁的值一样才会释放）
	 * @param syn		true=同步，false=异步
	 * @param service	只在syn=false时有效，指定线程池
	 * @return
	 */
	public boolean releaseLock(String key, String lockId, boolean syn, ExecutorService service);
	
	/**
	 * @Description: 通过pipeline方式批量写
	 * 
	 * @param map	批量写的key-value集合
	 * @return
	 */
	public boolean pipelineWrite(Map<String, Object> map);
	
	/**
	 * @Description: 通过pipeline方式批量写，指定过期时间，单位秒
	 * 
	 * @param map		批量写的key-value集合
	 * @param expire	过期时间
	 * @return
	 */
	public boolean pipelineWrite(Map<String, Object> map, long expire);
	
	/**
	 * @Description: 通过pipeline方式批量写，指定同步/异步方式写
	 * 
	 * @param map		批量写的key-value集合
	 * @param syn		true=同步，false=异步
	 * @param service	只在syn=false时有效，指定线程池
	 * @return
	 */
	public boolean pipelineWrite(Map<String, Object> map, boolean syn, ExecutorService service);
	
	/**
	 * @Description: 通过pipeline方式批量写，指定过期时间（单位秒），同步/异步方式写
	 * 
	 * @param map		批量写的key-value集合
	 * @param expire	过期时间，单位秒
	 * @param syn		true=同步，false=异步
	 * @param service	只在syn=false时有效，指定线程池
	 * @return
	 */
	public boolean pipelineWrite(Map<String, Object> map, long expire, boolean syn, ExecutorService service);
	
	/**
	 * @Description: 通过pipeline方式批量读，适合超大key列表，spring-data还没实现集群的pipeline
	 * 
	 * @param list	要批量读取的key列表
	 * @return
	 */
	public List<Object> pipelineRead(List<String> list);
	
	public boolean multiSet(Map<String, Object> map);
	
	public boolean multiSet(Map<String, Object> map, long expire);
	
	public boolean multiSet(Map<String, Object> map, boolean syn, ExecutorService service);
	
	public boolean multiSet(Map<String, Object> map, long expire, boolean syn, ExecutorService service);
	
	public List<Object> multiGet(List<String> list);
}
