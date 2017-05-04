package com.asiainfo.redis.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.jedis.JedisConverters;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.RedisConnectionUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.TimeoutUtils;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.stereotype.Component;

import com.asiainfo.redis.IRedisDao;
import com.asiainfo.redis.util.JedisClusterPipeline;

import redis.clients.jedis.JedisCluster;

/**
 * @Description: redis缓存操作实现类
 * 
 * @author       zq
 * @date         2017年4月1日  下午5:19:23
 * Copyright: 	  北京亚信智慧数据科技有限公司
 */
@Component
public class RedisDaoImpl implements IRedisDao {

	@Autowired
	private RedisTemplate<String, Object> redisTemplate;

	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setString(java.lang.String, java.lang.String)
	 */
	@Override
	public void setString(final String key, final String value) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[] rawValue = stringSerializer.serialize(value);
		
		this.redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.set(rawKey, rawValue);
				return null;
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param timeout	过期时间，小于0时不设置过期时间
	 * @param unit		过期时间单位
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setString(java.lang.String, java.lang.String, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void setString(final String key, final String value, final long timeout, final TimeUnit unit) {
		
		final TimeUnit timeUnit = (null == unit ? TimeUnit.SECONDS : unit);
		if (timeout <= 0L) {
			this.setString(key, value);
			return;
		}
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[] rawValue = stringSerializer.serialize(value);
		this.redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.setEx(rawKey, TimeoutUtils.toSeconds(timeout, timeUnit), rawValue);
				return null;
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @param expire	过期时间，小于0时不设置过期时间，单位秒
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setString(java.lang.String, java.lang.String, long)
	 */
	@Override
	public void setString(final String key, final String value, final long expire) {
		this.setString(key, value, expire, TimeUnit.SECONDS);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param date		过期日期，为null时不设置过期时间
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setString(java.lang.String, java.lang.String, java.util.Date)
	 */
	@Override
	public void setString(final String key, final String value, final Date date) {
		
		final long timeout = (null == date) ? -1L : (date.getTime() - System.currentTimeMillis());
		if (timeout > 0L) {
			this.setString(key, value, timeout, TimeUnit.MILLISECONDS);
		} else {
			this.setString(key, value);
		}
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setObject(java.lang.String, java.lang.Object)
	 */
	@Override
	public void setObject(final String key, final Object value) {
		this.redisTemplate.opsForValue().set(key, value);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param timeout	过期时间，小于0时不设置过期时间
	 * @param unit		过期时间单位
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setObject(java.lang.String, java.lang.Object, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void setObject(final String key, final Object value, final long timeout, final TimeUnit unit) {
		
		final TimeUnit timeUnit = (null == unit ? TimeUnit.SECONDS : unit);
		if (timeout > 0L) {
			this.redisTemplate.opsForValue().set(key, value, timeout, timeUnit);
		} else {
			this.setObject(key, value);
		}
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @param expire	过期时间，单位秒
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setObject(java.lang.String, java.lang.Object, long)
	 */
	@Override
	public void setObject(final String key, final Object value, final long expire) {
		this.setObject(key, value, expire, TimeUnit.SECONDS);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param date		过期日期，为null时表示不设置过期时间
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setObject(java.lang.String, java.lang.Object, java.util.Date)
	 */
	@Override
	public void setObject(final String key, final Object value, final Date date) {
		
		final long timeout = (null == date) ? -1L : (date.getTime() - System.currentTimeMillis());
		if (timeout > 0L) {
			this.setObject(key, value, timeout, TimeUnit.MILLISECONDS);
		} else {
			this.setObject(key, value);
		}
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setIfAbsent(java.lang.String, java.lang.Object)
	 */
	@Override
	public boolean setIfAbsent(final String key, final Object value) {
		return this.redisTemplate.opsForValue().setIfAbsent(key, value).booleanValue();
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param timeout	过期时间
	 * @param unit		过期时间单位
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setIfAbsent(java.lang.String, java.lang.Object, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean setIfAbsent(final String key, final Object value, final long timeout, final TimeUnit unit) {
		
		final TimeUnit timeUnit = (null == unit ? TimeUnit.SECONDS : unit);
		boolean lock = this.setIfAbsent(key, value);
		if (lock && timeout > 0L) {
			try {
				this.expire(key, timeout, timeUnit);
			} catch (Exception ex) {
				//设置过期时间失败时必须删除已设置的key
				this.remove(key);
				return false;
			}
		}
		return lock;
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @param obj
	 * @param expire	过期时间，单位秒
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setIfAbsent(java.lang.String, java.lang.Object, long)
	 */
	@Override
	public boolean setIfAbsent(final String key, final Object value, final long expire) {
		return this.setIfAbsent(key, value, expire, TimeUnit.SECONDS);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param value
	 * @param date
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#setIfAbsent(java.lang.String, java.lang.Object, java.util.Date)
	 */
	@Override
	public boolean setIfAbsent(final String key, final Object value, final Date date) {
		
		final long timeout = (null == date) ? -1L : (date.getTime() - System.currentTimeMillis());
		if (timeout > 0L) {
			return this.setIfAbsent(key, value, timeout, TimeUnit.MILLISECONDS);
		} else {
			return this.setIfAbsent(key, value);
		}
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#getString(java.lang.String)
	 */
	@Override
	public String getString(final String key) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		
		return this.redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				return stringSerializer.deserialize(connection.get(rawKey));
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#getObject(java.lang.String)
	 */
	@Override
	public Object getObject(final String key) {
		return this.redisTemplate.opsForValue().get(key);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#containsKey(java.lang.String)
	 */
	@Override
	public boolean containsKey(final String key) {
		return this.redisTemplate.hasKey(key).booleanValue();
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param newKey
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#rename(java.lang.String, java.lang.String)
	 */
	@Override
	public void rename(final String key, final String newKey) {
		this.redisTemplate.boundValueOps(key).rename(newKey);
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#remove(java.lang.String)
	 */
	@Override
	public void remove(final String key) {
		this.redisTemplate.delete(key);
	}

	/* 
	 * @Description: TODO
	 * @param pattern
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#keys(java.lang.String)
	 */
	@Override
	public Set<String> keys(final String pattern) {
		return this.redisTemplate.keys(pattern);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param expire	过期时间，单位秒
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#expire(java.lang.String, long)
	 */
	@Override
	public boolean expire(final String key, final long expire) {
		return this.expire(key, expire, TimeUnit.SECONDS);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param timeout	过期时间
	 * @param unit		过期时间单位
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#expire(java.lang.String, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public boolean expire(final String key, final long timeout, final TimeUnit unit) {
		final TimeUnit timeUnit = (null == unit ? TimeUnit.SECONDS : unit);
		return timeout <= 0L ? false : this.redisTemplate.expire(key, timeout, timeUnit).booleanValue();
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param date		过期日期
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#expire(java.lang.String, java.util.Date)
	 */
	@Override
	public boolean expire(final String key, final Date date) {
		return null == date ? false : this.redisTemplate.expireAt(key, date).booleanValue();
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#getExpire(java.lang.String)
	 */
	@Override
	public Long getExpire(final String key) {
		return this.redisTemplate.getExpire(key, TimeUnit.SECONDS);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param unit		过期时间单位，为null时，默认返回秒
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#getExpire(java.lang.String, java.util.concurrent.TimeUnit)
	 */
	@Override
	public Long getExpire(final String key, final TimeUnit unit) {
		final TimeUnit timeUnit = (null == unit ? TimeUnit.SECONDS : unit);
		return this.redisTemplate.getExpire(key, timeUnit);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param values
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#sAdd(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean sAdd(final String key, final String... values) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[][] rawValues = new byte[values.length][];
		for (int i = 0; i < values.length; i++) {
			rawValues[i] = stringSerializer.serialize(values[i]);
		}
		
		final Long result = this.redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.sAdd(rawKey, rawValues);
			}
		}, true);

		//返回1表示添加成功，返回0表示要添加的values已经存在
		return result != null ? (result.longValue() > 0) : false;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param values
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#sRem(java.lang.String, java.lang.String[])
	 */
	@Override
	public boolean sRem(final String key, final String... values) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[][] rawValues = new byte[values.length][];
		for (int i = 0; i < values.length; i++) {
			rawValues[i] = stringSerializer.serialize(values[i]);
		}
		
		final Long result = this.redisTemplate.execute(new RedisCallback<Long>() {
			@Override
			public Long doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.sRem(rawKey, rawValues);
			}
		}, true);
		
		//返回1表示删除成功，返回0表示要删除的values不存在
		return result != null ? (result.longValue() > 0) : false;
	}
	
	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#sMembers(java.lang.String)
	 */
	@Override
	public Set<String> sMembers(final String key) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		
		final Set<byte[]> rawValues = this.redisTemplate.execute(new RedisCallback<Set<byte[]>>() {
			@Override
			public Set<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.sMembers(rawKey);
			}
		}, true);
		
		final Set<String> result = new HashSet<String>();
		if (rawValues != null) {
			for (byte[] value : rawValues) {
				result.add(stringSerializer.deserialize(value));
			}
		}
		return result;
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#pipelineSet(java.util.Map)
	 */
	@Override
	public void pipelineSet(final Map<String, Object> map) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) this.redisTemplate.getValueSerializer();
		
		final RedisConnection redisConnection = this.redisTemplate.getConnectionFactory().getConnection();
		final Object nativeConnection = redisConnection.getNativeConnection();
		//单点模式支持pipeline
		if (redisConnection.isPipelined()) {
			this.redisTemplate.executePipelined(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
						byte[] rawKey = stringSerializer.serialize(entry.getKey());
						byte[] rawValue = valueSerializer.serialize(entry.getValue());
						connection.set(rawKey, rawValue);
					}
					return null;
				}
			}, valueSerializer);
		//集群模式不支持pipeline，只能自己实现
		} else if (nativeConnection instanceof JedisCluster) {
			final JedisClusterPipeline pipeline = JedisClusterPipeline.pipelined((JedisCluster) nativeConnection);
			try {
				//刷新元数据
				pipeline.refreshCluster();
				for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
					byte[] rawKey = stringSerializer.serialize(entry.getKey());
					byte[] rawValue = valueSerializer.serialize(entry.getValue());
					pipeline.set(rawKey, rawValue);
				}
				//同步到redis集群
				pipeline.sync();
			} catch (Exception ex) {
				throw JedisConverters.toDataAccessException(ex);
			} finally {
				pipeline.close();
				RedisConnectionUtils.releaseConnection(redisConnection, this.redisTemplate.getConnectionFactory());
			}
		} else {
			throw new UnsupportedOperationException("Pipeline is not supported for currently mode.");
		}
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param timeout	过期时间
	 * @param unit		过期时间单位
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#pipelineSet(java.util.Map, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void pipelineSet(final Map<String, Object> map, final long timeout, final TimeUnit unit) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) this.redisTemplate.getValueSerializer();
		
		final RedisConnection redisConnection = this.redisTemplate.getConnectionFactory().getConnection();
		final Object nativeConnection = redisConnection.getNativeConnection();
		final long expire = TimeoutUtils.toSeconds(timeout, unit);
		//单点模式支持pipeline
		if (redisConnection.isPipelined()) {
			this.redisTemplate.executePipelined(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
						byte[] rawKey = stringSerializer.serialize(entry.getKey());
						byte[] rawValue = valueSerializer.serialize(entry.getValue());
						connection.setEx(rawKey, expire, rawValue);
					}
					return null;
				}
			}, valueSerializer);
		//集群模式不支持pipeline，只能自己实现
		} else if (nativeConnection instanceof JedisCluster) {
			final JedisClusterPipeline pipeline = JedisClusterPipeline.pipelined((JedisCluster) nativeConnection);
			try {
				//刷新元数据
				pipeline.refreshCluster();
				for (java.util.Map.Entry<String, Object> entry : map.entrySet()) {
					byte[] rawKey = stringSerializer.serialize(entry.getKey());
					byte[] rawValue = valueSerializer.serialize(entry.getValue());
					pipeline.setex(rawKey, (int) expire, rawValue);
				}
				//同步到redis集群
				pipeline.sync();
			} catch (Exception ex) {
				throw JedisConverters.toDataAccessException(ex);
			} finally {
				pipeline.close();
				RedisConnectionUtils.releaseConnection(redisConnection, this.redisTemplate.getConnectionFactory());
			}
		} else {
			throw new UnsupportedOperationException("Pipeline is not supported for currently mode.");
		}
	}
	
	/* 
	 * @Description: TODO
	 * @param list
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#pipelineGet(java.util.List)
	 */
	@Override
	public List<Object> pipelineGet(final List<String> list) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final RedisSerializer<?> valueSerializer = this.redisTemplate.getValueSerializer();
		
		final RedisConnection redisConnection = this.redisTemplate.getConnectionFactory().getConnection();
		final Object nativeConnection = redisConnection.getNativeConnection();
		//单点模式支持pipeline
		if (redisConnection.isPipelined()) {
			return this.redisTemplate.executePipelined(new RedisCallback<Object>() {
				@Override
				public Object doInRedis(RedisConnection connection) throws DataAccessException {
					for (String key : list) {
						byte[] rawKey = stringSerializer.serialize(key);
						connection.get(rawKey);
					}
					return null;
				}
			}, valueSerializer);
		//集群模式不支持pipeline，只能自己实现
		} else if (nativeConnection instanceof JedisCluster) {
			final JedisClusterPipeline pipeline = JedisClusterPipeline.pipelined((JedisCluster) nativeConnection);
			try {
				//刷新元数据
				pipeline.refreshCluster();
				for (String key : list) {
					byte[] rawKey = stringSerializer.serialize(key);
					pipeline.get(rawKey);
				}
				//获取redis批量返回数据
				final List<Object> values = pipeline.syncAndReturnAll();
				
				//解析返回值
				final List<Object> result = new ArrayList<Object>();
				for (Object rawValue : values) {
					result.add(valueSerializer.deserialize((byte[]) rawValue));
				}
				return result;
			} catch (Exception ex) {
				throw JedisConverters.toDataAccessException(ex);
			} finally {
				pipeline.close();
				RedisConnectionUtils.releaseConnection(redisConnection, this.redisTemplate.getConnectionFactory());
			}
		} else {
			throw new UnsupportedOperationException("Pipeline is not supported for currently mode.");
		}
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#multiSet(java.util.Map)
	 */
	@Override
	public void multiSet(final Map<String, Object> map) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) this.redisTemplate.getValueSerializer();
		
		this.redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					byte[] rawKey = stringSerializer.serialize(entry.getKey());
					byte[] rawValue = valueSerializer.serialize(entry.getValue());
					connection.set(rawKey, rawValue);
				}
				return null;
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param map
	 * @param timeout
	 * @param unit
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#multiSet(java.util.Map, long, java.util.concurrent.TimeUnit)
	 */
	@Override
	public void multiSet(final Map<String, Object> map, final long timeout, final TimeUnit unit) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) this.redisTemplate.getValueSerializer();
		final long expire = TimeoutUtils.toSeconds(timeout, unit);
		
		this.redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				for (Map.Entry<String, Object> entry : map.entrySet()) {
					byte[] rawKey = stringSerializer.serialize(entry.getKey());
					byte[] rawValue = valueSerializer.serialize(entry.getValue());
					connection.setEx(rawKey, expire, rawValue);
				}
				return null;
			}
		}, true);
	}
	
	/* 
	 * @Description: TODO
	 * @param list
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#multiGet(java.util.List)
	 */
	@Override
	public List<Object> multiGet(final List<String> list) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> valueSerializer = (RedisSerializer<Object>) this.redisTemplate.getValueSerializer();
		
		return this.redisTemplate.execute(new RedisCallback<List<Object>>() {
			@Override
			public List<Object> doInRedis(RedisConnection connection) throws DataAccessException {
				List<Object> result = new ArrayList<Object>();
				for (String key : list) {
					byte[] rawKey = stringSerializer.serialize(key);
					byte[] rawValue = connection.get(rawKey);
					result.add(valueSerializer.deserialize(rawValue));
				}
				return result;
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hSetString(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public boolean hSetString(final String key, final String field, final String value) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[] rawField = stringSerializer.serialize(field);
		final byte[] rawValue = stringSerializer.serialize(value);
		
		return this.redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hSet(rawKey, rawField, rawValue);
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param map
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hMSetString(java.lang.String, java.util.Map)
	 */
	@Override
	public void hMSetString(String key, Map<String, String> map) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final Map<byte[], byte[]> hashes = new LinkedHashMap<byte[], byte[]>(map.size());
		for (Map.Entry<String, String> entry : map.entrySet()) {
			hashes.put(stringSerializer.serialize(entry.getKey()), stringSerializer.serialize(entry.getValue()));
		}
		
		this.redisTemplate.execute(new RedisCallback<Object>() {
			@Override
			public Object doInRedis(RedisConnection connection) throws DataAccessException {
				connection.hMSet(rawKey, hashes);
				return null;
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param field
	 * @param value
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hSet(java.lang.String, java.lang.Object, java.lang.Object)
	 */
	@Override
	public boolean hSet(String key, Object field, Object value) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> hashKeySerializer = (RedisSerializer<Object>) this.redisTemplate.getHashKeySerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>) this.redisTemplate.getHashValueSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[] rawField = hashKeySerializer.serialize(field);
		final byte[] rawValue = hashValueSerializer.serialize(value);
		
		return this.redisTemplate.execute(new RedisCallback<Boolean>() {
			@Override
			public Boolean doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hSet(rawKey, rawField, rawValue);
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param map
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hMSet(java.lang.String, java.util.Map)
	 */
	@Override
	public void hMSet(String key, Map<Object, Object> map) {
		this.redisTemplate.opsForHash().putAll(key, map);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param field
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hGetString(java.lang.String, java.lang.String)
	 */
	@Override
	public String hGetString(String key, String field) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[] rawField = stringSerializer.serialize(field);
		
		return this.redisTemplate.execute(new RedisCallback<String>() {
			@Override
			public String doInRedis(RedisConnection connection) throws DataAccessException {
				return stringSerializer.deserialize(connection.hGet(rawKey, rawField));
			}
		}, true);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param fields
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hMGetString(java.lang.String, java.lang.String[])
	 */
	@Override
	public List<String> hMGetString(String key, String... fields) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		final byte[][] rawFields = new byte[fields.length][];
		
		List<byte[]> list = this.redisTemplate.execute(new RedisCallback<List<byte[]>>() {
			@Override
			public List<byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hMGet(rawKey, rawFields);
			}
		}, true);
		
		if (list == null) {
			return null;
		}
		List<String> result = new ArrayList<String>();
		for (byte[] value : list) {
			result.add(stringSerializer.deserialize(value));
		}
		return result;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hGetAllString(java.lang.String)
	 */
	@Override
	public Map<String, String> hGetAllString(String key) {

		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		
		Map<byte[],byte[]> map = this.redisTemplate.execute(new RedisCallback<Map<byte[],byte[]>>() {
			@Override
			public Map<byte[],byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hGetAll(rawKey);
			}
		}, true);
		
		if (map == null) {
			return null;
		}
		Map<String, String> result = new HashMap<String, String>();
		for (Map.Entry<byte[],byte[]> entry : map.entrySet()) {
			result.put(stringSerializer.deserialize(entry.getKey()), stringSerializer.deserialize(entry.getValue()));
		}
		return result;
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param field
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hGet(java.lang.String, java.lang.Object)
	 */
	@Override
	public Object hGet(String key, Object field) {
		return this.redisTemplate.opsForHash().get(key, field);
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @param fields
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hMGet(java.lang.String, java.lang.Object[])
	 */
	@Override
	public List<Object> hMGet(String key, Object... fields) {
		return this.redisTemplate.opsForHash().multiGet(key, Arrays.asList(fields));
	}

	/* 
	 * @Description: TODO
	 * @param key
	 * @return
	 * @see com.asiainfo.biapp.mcd.redis.IRedisDao#hGetAll(java.lang.String)
	 */
	@Override
	public Map<Object, Object> hGetAll(String key) {
		
		final RedisSerializer<String> stringSerializer = this.redisTemplate.getStringSerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> hashKeySerializer = (RedisSerializer<Object>) this.redisTemplate.getHashKeySerializer();
		@SuppressWarnings("unchecked")
		final RedisSerializer<Object> hashValueSerializer = (RedisSerializer<Object>) this.redisTemplate.getHashValueSerializer();
		final byte[] rawKey = stringSerializer.serialize(key);
		
		Map<byte[], byte[]> map = this.redisTemplate.execute(new RedisCallback<Map<byte[], byte[]>>() {
			@Override
			public Map<byte[], byte[]> doInRedis(RedisConnection connection) throws DataAccessException {
				return connection.hGetAll(rawKey);
			}
		}, true);
		
		if (map == null) {
			return null;
		}
		Map<Object, Object> result = new HashMap<Object, Object>();
		for (Map.Entry<byte[],byte[]> entry : map.entrySet()) {
			result.put(hashKeySerializer.deserialize(entry.getKey()), hashValueSerializer.deserialize(entry.getValue()));
		}
		return result;
	}
}
