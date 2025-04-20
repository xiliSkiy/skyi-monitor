package com.skyi.storage.service.impl;

import com.skyi.storage.service.CacheService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存服务实现
 */
@Slf4j
@Service
public class RedisCacheServiceImpl implements CacheService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean set(String key, Object value, long timeout, TimeUnit unit) {
        try {
            redisTemplate.opsForValue().set(key, value, timeout, unit);
            return true;
        } catch (Exception e) {
            log.error("设置缓存失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public Object get(String key) {
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("获取缓存失败, key: {}", key, e);
            return null;
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(String key, Class<T> clazz) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value != null && clazz.isInstance(value)) {
                return (T) value;
            }
            return null;
        } catch (Exception e) {
            log.error("获取缓存失败, key: {}", key, e);
            return null;
        }
    }

    @Override
    public boolean delete(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.delete(key));
        } catch (Exception e) {
            log.error("删除缓存失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public long deleteByPattern(String pattern) {
        try {
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                return redisTemplate.delete(keys);
            }
            return 0;
        } catch (Exception e) {
            log.error("批量删除缓存失败, pattern: {}", pattern, e);
            return 0;
        }
    }

    @Override
    public boolean expire(String key, long timeout, TimeUnit unit) {
        try {
            return Boolean.TRUE.equals(redisTemplate.expire(key, timeout, unit));
        } catch (Exception e) {
            log.error("设置缓存过期时间失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public boolean hasKey(String key) {
        try {
            return Boolean.TRUE.equals(redisTemplate.hasKey(key));
        } catch (Exception e) {
            log.error("判断缓存是否存在失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public long getExpire(String key, TimeUnit unit) {
        try {
            Long expire = redisTemplate.getExpire(key, unit);
            return expire != null ? expire : -1;
        } catch (Exception e) {
            log.error("获取缓存过期时间失败, key: {}", key, e);
            return -1;
        }
    }

    @Override
    public boolean hashSet(String key, String hashKey, Object value) {
        try {
            redisTemplate.opsForHash().put(key, hashKey, value);
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存失败, key: {}, hashKey: {}", key, hashKey, e);
            return false;
        }
    }

    @Override
    public Object hashGet(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().get(key, hashKey);
        } catch (Exception e) {
            log.error("获取Hash缓存失败, key: {}, hashKey: {}", key, hashKey, e);
            return null;
        }
    }

    @Override
    public boolean hashSetAll(String key, Map<String, Object> map) {
        try {
            redisTemplate.opsForHash().putAll(key, map);
            return true;
        } catch (Exception e) {
            log.error("设置Hash缓存失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public Map<Object, Object> hashGetAll(String key) {
        try {
            return redisTemplate.opsForHash().entries(key);
        } catch (Exception e) {
            log.error("获取Hash所有键值失败, key: {}", key, e);
            return new HashMap<>();
        }
    }

    @Override
    public long hashDelete(String key, Object... hashKeys) {
        try {
            return redisTemplate.opsForHash().delete(key, hashKeys);
        } catch (Exception e) {
            log.error("删除Hash缓存失败, key: {}", key, e);
            return 0;
        }
    }

    @Override
    public boolean hashHasKey(String key, String hashKey) {
        try {
            return redisTemplate.opsForHash().hasKey(key, hashKey);
        } catch (Exception e) {
            log.error("判断Hash缓存是否存在失败, key: {}, hashKey: {}", key, hashKey, e);
            return false;
        }
    }

    @Override
    public long listPush(String key, Object value) {
        try {
            Long size = redisTemplate.opsForList().rightPush(key, value);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("往List缓存添加元素失败, key: {}", key, e);
            return 0;
        }
    }

    @Override
    public long listPushAll(String key, Object... values) {
        try {
            Long size = redisTemplate.opsForList().rightPushAll(key, values);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("往List缓存添加多个元素失败, key: {}", key, e);
            return 0;
        }
    }

    @Override
    public List<Object> listRange(String key, long start, long end) {
        try {
            return redisTemplate.opsForList().range(key, start, end);
        } catch (Exception e) {
            log.error("从List缓存获取元素范围失败, key: {}", key, e);
            return new ArrayList<>();
        }
    }

    @Override
    public Set<Object> setMembers(String key) {
        try {
            return redisTemplate.opsForSet().members(key);
        } catch (Exception e) {
            log.error("获取Set缓存失败, key: {}", key, e);
            return new HashSet<>();
        }
    }

    @Override
    public long setAdd(String key, Object... values) {
        try {
            Long size = redisTemplate.opsForSet().add(key, values);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("往Set缓存添加元素失败, key: {}", key, e);
            return 0;
        }
    }

    @Override
    public long setRemove(String key, Object... values) {
        try {
            Long size = redisTemplate.opsForSet().remove(key, values);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("从Set缓存移除元素失败, key: {}", key, e);
            return 0;
        }
    }

    @Override
    public boolean setContains(String key, Object value) {
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(key, value));
        } catch (Exception e) {
            log.error("判断Set缓存是否包含元素失败, key: {}", key, e);
            return false;
        }
    }

    @Override
    public long setSize(String key) {
        try {
            Long size = redisTemplate.opsForSet().size(key);
            return size != null ? size : 0;
        } catch (Exception e) {
            log.error("获取Set缓存大小失败, key: {}", key, e);
            return 0;
        }
    }
} 