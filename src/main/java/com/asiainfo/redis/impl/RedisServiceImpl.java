package com.asiainfo.redis.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.asiainfo.redis.IExecutor;
import com.asiainfo.redis.IRedisDao;
import com.asiainfo.redis.IRedisService;
import com.asiainfo.redis.task.RedisExecuteTask;
import com.asiainfo.redis.util.RedisThreadPoolTools;
import com.asiainfo.redis.util.ServiceUtil;

/**
 * @Description: redis服务层实现类
 * 
 * @author       zq
 * @date         2017年3月19日  上午9:35:17
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
@Service
public class RedisServiceImpl implements IRedisService {

	private static final Logger logger = LoggerFactory.getLogger(RedisServiceImpl.class);

	@Value("${spring.redis.cluster.pipeline.maxnum}")
	private int maxNum;
	
	@Autowired
	private IRedisDao redisDao;

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean setString(final String key, final String value) {
		return this.setString(key, value, -1L, true, 1, null);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param retry
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setString(java.lang.String, java.lang.String, long)
	 */
	@Override
	public boolean setString(final String key, final String value, final long expire) {
		return this.setString(key, value, expire, true, 1, null);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param syn
	 * @param retry
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setString(java.lang.String, java.lang.String, boolean, int, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean setString(final String key, final String value, final boolean syn, final int retry, ExecutorService service) {
		return this.setString(key, value, -1L, syn, retry, service);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param expire
	 * @param syn
	 * @param retry
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setString(java.lang.String, java.lang.String, long, boolean, int, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean setString(final String key, final String value, final long expire, 
			final boolean syn, final int retry, final ExecutorService service) {
		
		logger.debug("调用redis String保存方法，参数为key={}, value={}, expire={}, syn={}, retry={} ......", 
				key, value, expire, syn, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		//任务执行器
		final IExecutor executor = new IExecutor() {
			@Override
			public void execute(IRedisDao redisDao) {
				if (expire <= 0) {
					redisDao.setString(key, value);
				} else {
					redisDao.setString(key, value, expire);
				}
			}
		};
		
		//异步保存时，直接返回true，并调用线程池执行加载任务，不能保证一定加载成功
		if (!syn) {
			if (service == null) {
				RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, times));
			} else {
				service.execute(new RedisExecuteTask(this.redisDao, executor, times));
			}
			return true;
		}
		
		//同步保存
		for (int i = 0; i < times; i++) {
			try {
				executor.execute(this.redisDao);
				return true;
			} catch (Exception ex) {
				logger.error("保存key={}时出现异常，异常信息：\n{}", key, ex);
			}
			
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("保存时出现异常，连续尝试{}次未能成功，参数为：key={}, value={}, expire={} ......", 
				times, key, value, expire);
		return false;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setObject(final String key, final Object value) {
		return this.setObject(key, value, -1L);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param retry
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setObject(java.lang.String, java.lang.Object, long)
	 */
	@Override
	public boolean setObject(final String key, final Object value, final long expire) {
		return this.setObject(key, value, expire, true, 1, null);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param syn
	 * @param retry
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setObject(java.lang.String, java.lang.Object, boolean, int, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean setObject(final String key, final Object value, final boolean syn, final int retry, final ExecutorService service) {
		return this.setObject(key, value, -1L, syn, retry, service);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param expire
	 * @param syn
	 * @param retry
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#setObject(java.lang.String, java.lang.Object, long, boolean, int, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean setObject(final String key, final Object value, final long expire, 
			final boolean syn, final int retry, final ExecutorService service) {
		
		logger.debug("调用redis Object保存方法，参数为key={}, value={}, expire={}, syn={}, retry={} ......", 
				key, value, expire, syn, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		//任务执行器
		final IExecutor executor = new IExecutor() {
			@Override
			public void execute(IRedisDao redisDao) {
				if (expire <= 0) {
					redisDao.setObject(key, value);
				} else {
					redisDao.setObject(key, value, expire);
				}
			}
		};
		
		//异步保存时，直接返回true，并调用线程池执行加载任务，不能保证一定加载成功
		if (!syn) {
			if (service == null) {
				RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, times));
			} else {
				service.execute(new RedisExecuteTask(this.redisDao, executor, times));
			}
			return true;
		}
		//同步保存
		for (int i = 0; i < times; i++) {
			try {
				executor.execute(this.redisDao);
				return true;
			} catch (Exception ex) {
				logger.error("保存key={}时出现异常，异常信息：\n{}", key, ex);
			}
			
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("保存时出现异常，连续尝试{}次未能成功，参数为：key={}, value={}, expire={} ......", 
				times, key, value, expire);
		return false;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#getString(java.lang.String)
	 */
	@Override
	public String getString(final String key) {
		return this.getString(key, 1);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param retry
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#getString(java.lang.String, int)
	 */
	@Override
	public String getString(final String key, final int retry) {
		
		logger.debug("调用redis String读取方法，参数为key={}, retry={} ......", key, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		for (int i = 0; i < times; i++) {
			try {
				return this.redisDao.getString(key);
			} catch (Exception ex) {
				logger.error("读取key={}时出现异常，异常信息：\n{}", key, ex);
			}
			
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("读取时出现异常，连续尝试{}次未能成功，参数为：key={}, retry={} ......", times, key, retry);
		//为了区分null和获取异常，这里抛出异常
		throw new RuntimeException("key=" + key + "读取redis数据时发生异常！");
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(final String key) {
		return this.getObject(key, 1);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param retry
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IFilterRedisService#getObject(java.lang.String, int)
	 */
	@Override
	public Object getObject(final String key, final int retry) {

		logger.debug("调用redis Object读取方法，参数为key={}, retry={} ......", key, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		for (int i = 0; i < times; i++) {
			try {
				return this.redisDao.getObject(key);
			} catch (Exception ex) {
				logger.error("读取key={}时出现异常，异常信息：\n{}", key, ex);
			}
			
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("读取时出现异常，连续尝试{}次未能成功，参数为：key={}, retry={} ......", times, key, retry);
		//为了区分null和获取异常，这里抛出异常
		throw new RuntimeException("key=" + key + "读取redis数据时发生异常！");
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param retry
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#remove(java.lang.String, int)
	 */
	@Override
	public boolean remove(final String key, final int retry) {
		
		logger.debug("调用redis删除方法，参数为key={}, retry={} ......", key, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		
		//同步删除
		for (int i = 0; i < times; i++) {
			try {
				this.redisDao.remove(key);
				return true;
			} catch (Exception ex) {
				logger.error("删除key={}时出现异常，异常信息：\n{}", key, ex);
			}
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("删除时出现异常，连续尝试{}次未能成功，要删除的参数为：key={}, retry={} ......", times, key, retry);
		return false;
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param syn
	 * @param retry
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#remove(java.lang.String, boolean, int, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean remove(final String key, final boolean syn, final int retry, final ExecutorService service) {
		
		logger.debug("调用redis删除方法，参数为key={}, syn={}, retry={} ......", key, syn, retry);
		
		//重试次数默认为1次
		final int times = (retry <= 0) ? 1 : retry;
		//任务执行器
		final IExecutor executor = new IExecutor() {
			@Override
			public void execute(IRedisDao redisDao) {
				redisDao.remove(key);
			}
		};
				
		//异步删除时，直接返回true，并调用线程池执行删除任务，不能保证一定删除成功
		if (!syn) {
			if (service == null) {
				RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, times));
			} else {
				service.execute(new RedisExecuteTask(this.redisDao, executor, times));
			}
			return true;
		}
		//同步删除
		for (int i = 0; i < times; i++) {
			try {
				executor.execute(this.redisDao);
				return true;
			} catch (Exception ex) {
				logger.error("删除key={}时出现异常，异常信息：\n{}", key, ex);
			}
			
			if ((i + 1) < times) {
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
		}
		logger.error("删除时出现异常，连续尝试{}次未能成功，要删除的参数为：key={}, syn={} ......", times, key, syn);
		return false;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#acquireLock(java.lang.String)
	 */
	@Override
	public String acquireLock(final String key) {

		logger.debug("调用redis分布式锁获取方法，参数为key={} ......", key);
		
		final String lockId = UUID.randomUUID().toString();
		try {
			boolean lock = this.redisDao.setIfAbsent(key, lockId);
			if (lock) {
				return lockId;
			}
			logger.info("未能获取到锁key={} ......", key);
		} catch (Exception ex) {
			logger.error("获取锁key={}时出现异常，异常信息：\n{}", key, ex);
		}
		return null;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param expire
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#acquireLock(java.lang.String, long)
	 */
	@Override
	public String acquireLock(final String key, final long expire) {
		
		logger.debug("调用redis分布式锁获取方法，参数为key={}, expire={} ......", key, expire);
		
		final String lockId = UUID.randomUUID().toString();
		try {
			boolean lock = this.redisDao.setIfAbsent(key, lockId, expire);
			if (lock) {
				return lockId;
			}
			logger.info("未能获取到锁key={} ......", key);
		} catch (Exception ex) {
			logger.error("获取锁={}时出现异常，异常信息：\n{}", key, ex);
		}
		return null;
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param timeout
	 * @param unit
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#acquireLock(java.lang.String, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public String acquireLock(final String key, final long timeout, final TimeUnit unit) {
		
		logger.debug("调用redis分布式锁获取方法，参数为key={}, timeout={} ......", key, timeout);
		
		final String lockId = UUID.randomUUID().toString();
		final long maxwait = unit.toMillis(timeout);
		final long start = System.currentTimeMillis();
		try {
			while ((System.currentTimeMillis() - start) < maxwait) {
				if (this.redisDao.setIfAbsent(key, lockId)) {
					return lockId;
				}
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
			logger.info("超过{}ms未能获取到锁key={} ......", maxwait, key);
		} catch (Exception ex) {
			logger.error("获取锁key={}时出现异常，异常信息：\n{}", key, ex);
		}
		return null;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param expire
	 * @param timeout
	 * @param unit
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#acquireLock(java.lang.String, long, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public String acquireLock(final String key, final long expire, final long timeout, final TimeUnit unit) {
		
		logger.debug("调用redis分布式锁获取方法，参数为key={}, expire={}, timeout={} ......", key, expire, timeout);
		
		final String lockId = UUID.randomUUID().toString();
		final long maxwait = unit.toMillis(timeout);
		final long start = System.currentTimeMillis();
		try {
			while ((System.currentTimeMillis() - start) < maxwait) {
				if (this.redisDao.setIfAbsent(key, lockId, expire)) {
					return lockId;
				}
				ServiceUtil.waitFor(100, TimeUnit.MILLISECONDS);
			}
			logger.info("超过{}ms未能获取到锁key={} ......", maxwait, key);
		} catch (Exception ex) {
			logger.error("获取锁key={}时出现异常，异常信息：\n{}", key, ex);
		}
		return null;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#releaseLock(java.lang.String)
	 */
	@Override
	public boolean releaseLock(final String key) {

		logger.debug("调用redis分布式锁强制释放方法，参数为key={} ......", key);
		
		try {
			this.redisDao.remove(key);
			return true;
		} catch (Exception ex) {
			logger.error("释放锁key={}时出现异常，异常信息：\n{}", ex);
		}
		return false;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param lockId
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#releaseLock(java.lang.String, java.lang.String)
	 */
	@Override
	public boolean releaseLock(final String key, final String lockId) {
		return this.releaseLock(key, lockId, true, null);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param lockId
	 * @param syn
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#releaseLock(java.lang.String, java.lang.String, boolean, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean releaseLock(final String key, final String lockId, final boolean syn, final ExecutorService service) {

		logger.debug("调用redis分布式锁释放方法，参数为key={}, lockId={}, syn={} ......", key, lockId, syn);
		
		try {
			//lockId为空时强制释放锁，强制使用同步方式
			if (StringUtils.isEmpty(lockId)) {
				return this.releaseLock(key);
			}
			
			Object currentLock = this.redisDao.getObject(key);
			//当前锁为空直接返回
			if (null == currentLock) {
				return true;
			}

			//锁id相同时，释放锁
			if (lockId.equals(currentLock)) {
				//任务执行器
				final IExecutor executor = new IExecutor() {
					@Override
					public void execute(IRedisDao redisDao) {
						redisDao.remove(key);
					}
				};
				//异步释放
				if (!syn) {
					if (service == null) {
						RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, 1));
					} else {
						service.execute(new RedisExecuteTask(this.redisDao, executor, 1));
					}
					return true;
				}
				
				//同步释放
				executor.execute(this.redisDao);
				return true;
			}
			//锁id不一样表示自己的锁已过期，锁被其他人使用，不需要释放
			return true;
		} catch (Exception ex) {
			logger.error("释放锁key={}时出现异常，异常信息：\n{}", key, ex);
		}
		return false;
	}
	
	/* 
	 * @Description: TODO
	 * @param map
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#pipelineWrite(java.util.Map)
	 */
	@Override
	public boolean pipelineWrite(final Map<String, Object> map) {
		return this.pipelineWrite(map, -1L, true, null);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param expire
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#pipelineWrite(java.util.Map, long)
	 */
	@Override
	public boolean pipelineWrite(final Map<String, Object> map, final long expire) {
		return this.pipelineWrite(map, expire, true, null);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param syn
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#pipelineWrite(java.util.Map, boolean, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean pipelineWrite(final Map<String, Object> map, final boolean syn, final ExecutorService service) {
		return this.pipelineWrite(map, -1L, syn, service);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param expire
	 * @param syn
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#pipelineWrite(java.util.Map, long, boolean, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean pipelineWrite(final Map<String, Object> map, final long expire, final boolean syn, final ExecutorService service) {
		
		logger.info("调用redis pipeline批量保存方法，保存{}条记录，参数为：expire={}s、syn={} ......", map == null ? 0 : map.size(), expire, syn);
		
		if (map == null || map.isEmpty()) {
			return true;
		}
		
		try {
			//拆分成1w条一组，防止一次性写太多导致redis崩溃
			final List<Map<String, Object>> list = this.splitMap(map);
			//任务执行器
			final IExecutor executor = new IExecutor() {
				@Override
				public void execute(IRedisDao redisDao) {
					int count = 0;
					for (Map<String, Object> update : list) {
						logger.info("redis pipeline拆分第{}批，保存{}条记录，参数为：expire={}s、syn={} ......", ++count, update.size(), expire, syn);
						if (expire <= 0L) {
							redisDao.pipelineSet(update);
						} else {
							redisDao.pipelineSet(update, expire, TimeUnit.SECONDS);
						}
					}
				}
			};
			//异步方式无法保证一定保存成功，返回true表示已启动线程进行处理
			if (!syn) {
				if (service == null) {
					RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, 1));
				} else {
					service.execute(new RedisExecuteTask(this.redisDao, executor, 1));
				}
				return true;
			}
			
			//同步方式保存
			executor.execute(this.redisDao);
			return true;
		} catch (Exception ex) {
			logger.error("pipeline批量保存出现异常，异常信息：\n{}", ex);
		}
		return false;
	}
	
	//拆分redis写数据集合，每1w条一个批次
	private List<Map<String, Object>> splitMap(Map<String, Object> map) {
		
		final int maxPerPipeline = this.maxNum < 1000 ? 1000 : maxNum;
		List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
		Map<String, Object> temp = new HashMap<String, Object>();
		for (Map.Entry<String, Object> entry : map.entrySet()) {
			if (temp.size() % maxPerPipeline == 0) {
				temp = new HashMap<String, Object>();
				result.add(temp);
			}
			temp.put(entry.getKey(), entry.getValue());
		}
		return result;
	}

	/* 
	 * @Description: TODO
	 * @param list
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#pipelineRead(java.util.List)
	 */
	@Override
	public List<Object> pipelineRead(final List<String> list) {
		
		logger.info("调用redis pipeline批量读取方法，读取{}条记录 ......", list == null ? 0 : list.size());
		
		if (list == null || list.isEmpty()) {
			return new ArrayList<Object>();
		}
		
		List<Object> result = new ArrayList<Object>();
		try {
			int count = 0;
			List<List<String>> keyList = this.splitList(list);
			for (List<String> readList : keyList) {
				logger.info("redis pipeline拆分第{}批，读取{}条记录 ......", ++count, readList.size());
				result.addAll(this.redisDao.pipelineGet(readList));
			}
			return result;
		} catch (Exception ex) {
			logger.error("pipeline批量读取出现异常，异常信息：\n{}", ex);
		}
		return null;
	}
	
	//拆分redis读数据集合，每1w条一个批次
	private List<List<String>> splitList(List<String> list) {
		
		final int maxPerPipeline = this.maxNum < 1000 ? 1000 : maxNum;
		List<List<String>> result = new ArrayList<List<String>>();
		List<String> temp = new ArrayList<String>();
		for (String key : list) {
			if (temp.size() % maxPerPipeline == 0) {
				temp = new ArrayList<String>();
				result.add(temp);
			}
			temp.add(key);
		}
		return result;
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#multiSet(java.util.Map)
	 */
	@Override
	public boolean multiSet(Map<String, Object> map) {
		return this.multiSet(map, -1L, true, null);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param expire
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#multiSet(java.util.Map, long)
	 */
	@Override
	public boolean multiSet(Map<String, Object> map, long expire) {
		return this.multiSet(map, expire, true, null);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param syn
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#multiSet(java.util.Map, boolean, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean multiSet(Map<String, Object> map, boolean syn, ExecutorService service) {
		return this.multiSet(map, -1L, syn, service);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param expire
	 * @param syn
	 * @param service
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#multiSet(java.util.Map, long, boolean, java.util.concurrent.ExecutorService)
	 */
	@Override
	public boolean multiSet(final Map<String, Object> map, final long expire, final boolean syn, final ExecutorService service) {
		
		logger.info("调用redis multiSet批量保存方法，保存{}条记录，参数为：expire={}s、syn={} ......", map == null ? 0 : map.size(), expire, syn);
		
		if (map == null || map.isEmpty()) {
			return true;
		}
		
		try {
			//任务执行器
			final IExecutor executor = new IExecutor() {
				@Override
				public void execute(IRedisDao redisDao) {
					if (expire <= 0L) {
						redisDao.multiSet(map);
					} else {
						redisDao.multiSet(map, expire, TimeUnit.SECONDS);
					}
				}
			};
			//异步方式无法保证一定保存成功，返回true表示已启动线程进行处理
			if (!syn) {
				if (service == null) {
					RedisThreadPoolTools.getInstance().execute(new RedisExecuteTask(this.redisDao, executor, 1));
				} else {
					service.execute(new RedisExecuteTask(this.redisDao, executor, 1));
				}
				return true;
			}
			
			//同步方式保存
			executor.execute(this.redisDao);
			return true;
		} catch (Exception ex) {
			logger.error("multiSet批量保存出现异常，异常信息：\n{}", ex);
		}
		return false;
	}

	/* 
	 * @Description: TODO
	 * @param list
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisService#multiGet(java.util.List)
	 */
	@Override
	public List<Object> multiGet(List<String> list) {
		
		logger.info("调用redis multGet批量读取方法，读取{}条记录 ......", list == null ? 0 : list.size());
		
		if (list == null || list.isEmpty()) {
			return new ArrayList<Object>();
		}
		
		try {
			return this.redisDao.multiGet(list);
		} catch (Exception ex) {
			logger.error("multGet批量读取出现异常，异常信息：\n{}", ex);
		}
		return null;
	}
}
