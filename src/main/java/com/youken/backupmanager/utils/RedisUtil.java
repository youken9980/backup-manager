package com.youken.backupmanager.utils;

import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.Serializable;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 杨剑
 * @date 2019/12/26
 */
@Slf4j
@Component
public class RedisUtil {

	private RedisTemplate<Serializable, Object> redisTemplate;

	private RedisUtil() {
	}

	@Autowired
	public void setRedisTemplate(RedisTemplate<Serializable, Object> redisTemplate) {
		this.redisTemplate = redisTemplate;
	}

	// ======== key 操作 ======== //

	public Boolean hasKey(@NonNull Serializable key) {
		return redisTemplate.hasKey(key);
	}

	public Set<Serializable> keys(@NonNull String pattern) {
		return redisTemplate.keys(pattern);
	}

	public void delete(@NonNull Serializable key) {
		if (hasKey(key)) {
			redisTemplate.delete(key);
		}
	}

	public void deleteList(@NonNull Serializable... keys) {
		for (@NonNull Serializable key : keys) {
			delete(key);
		}
	}

	public void deletePattern(@NonNull Serializable pattern) {
		Set<Serializable> keys = redisTemplate.keys(pattern);
		if (!CollectionUtils.isEmpty(keys)) {
			redisTemplate.delete(keys);
		}
	}

	public Boolean expire(@NonNull Serializable key, @NonNull long seconds) {
		return redisTemplate.expire(key, seconds, TimeUnit.SECONDS);
	}

	public Boolean expireAt(@NonNull Serializable key, @NonNull Date date) {
		return redisTemplate.expireAt(key, date);
	}

	// ======== string 操作 ======== //

	public Object valueGet(@NonNull Serializable key) {
		return redisTemplate.opsForValue().get(key);
	}

	public void valueSet(@NonNull Serializable key, @NonNull Object value) {
		redisTemplate.opsForValue().set(key, value);
	}

	public void valueSet(@NonNull Serializable key, @NonNull Object value, @NonNull Long expireTime) {
		redisTemplate.opsForValue().set(key, value, expireTime, TimeUnit.SECONDS);
	}

	public void valueSetIfAbsent(@NonNull Serializable key, @NonNull Object value) {
		redisTemplate.opsForValue().setIfAbsent(key, value);
	}

	public void valueSetIfAbsent(@NonNull Serializable key, @NonNull Object value, @NonNull Long expireTime) {
		redisTemplate.opsForValue().setIfAbsent(key, value, expireTime, TimeUnit.SECONDS);
	}

	public Long valueIncrement(@NonNull Serializable key) {
		return redisTemplate.opsForValue().increment(key);
	}

	// ======== list 操作 ======== //

	public Long listLeftPush(@NonNull Serializable key, @NonNull Object value) {
		return redisTemplate.opsForList().leftPush(key, value);
	}

	public List<Object> listRange(@NonNull Serializable key) {
		return redisTemplate.opsForList().range(key, 0L, Long.MAX_VALUE);
	}

	// ======== set 操作 ======== //

	public Long setAdd(@NonNull Serializable key, @NonNull Object value) {
		return redisTemplate.opsForSet().add(key, value);
	}

	public Set<Object> setMembers(@NonNull Serializable key) {
		return redisTemplate.opsForSet().members(key);
	}

	// ======== hash 操作 ======== //

	public List<Object> hashMultiGet(@NonNull Serializable key, @NonNull String... fields) {
		List<String> fieldList = fields == null ? Collections.emptyList() : Arrays.asList(fields);
		HashOperations<Serializable, String, Object> hashOperations = redisTemplate.opsForHash();
		return hashOperations.multiGet(key, fieldList);
	}

	public void hashPutAll(@NonNull Serializable key, @NonNull Map<String, String> hash) {
		redisTemplate.opsForHash().putAll(key, hash);
	}
}
