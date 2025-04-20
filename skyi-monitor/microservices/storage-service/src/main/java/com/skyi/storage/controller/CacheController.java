package com.skyi.storage.controller;

import com.skyi.storage.service.CacheService;
import com.skyi.storage.utils.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 缓存控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/cache")
@Tag(name = "缓存接口", description = "提供缓存的操作功能")
public class CacheController {

    @Autowired
    private CacheService cacheService;

    @PostMapping
    @Operation(summary = "设置缓存", description = "设置缓存")
    public Result<Boolean> set(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "缓存值") @RequestParam String value) {
        log.debug("设置缓存, key: {}", key);
        boolean result = cacheService.set(key, value);
        return Result.success(result);
    }

    @PostMapping("/expire")
    @Operation(summary = "设置缓存并设置过期时间", description = "设置缓存并设置过期时间")
    public Result<Boolean> setWithExpire(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "缓存值") @RequestParam String value,
            @Parameter(description = "过期时间(秒)") @RequestParam long timeout) {
        log.debug("设置缓存并设置过期时间, key: {}, timeout: {}秒", key, timeout);
        boolean result = cacheService.set(key, value, timeout, TimeUnit.SECONDS);
        return Result.success(result);
    }

    @GetMapping
    @Operation(summary = "获取缓存", description = "获取缓存")
    public Result<Object> get(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("获取缓存, key: {}", key);
        Object value = cacheService.get(key);
        return Result.success(value);
    }

    @DeleteMapping
    @Operation(summary = "删除缓存", description = "删除缓存")
    public Result<Boolean> delete(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("删除缓存, key: {}", key);
        boolean result = cacheService.delete(key);
        return Result.success(result);
    }

    @DeleteMapping("/pattern")
    @Operation(summary = "批量删除缓存", description = "根据模式批量删除缓存")
    public Result<Long> deleteByPattern(
            @Parameter(description = "键模式") @RequestParam String pattern) {
        log.debug("批量删除缓存, pattern: {}", pattern);
        long result = cacheService.deleteByPattern(pattern);
        return Result.success(result);
    }

    @PutMapping("/expire")
    @Operation(summary = "设置缓存过期时间", description = "设置缓存过期时间")
    public Result<Boolean> expire(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "过期时间(秒)") @RequestParam long timeout) {
        log.debug("设置缓存过期时间, key: {}, timeout: {}秒", key, timeout);
        boolean result = cacheService.expire(key, timeout, TimeUnit.SECONDS);
        return Result.success(result);
    }

    @GetMapping("/exist")
    @Operation(summary = "判断缓存是否存在", description = "判断缓存是否存在")
    public Result<Boolean> hasKey(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("判断缓存是否存在, key: {}", key);
        boolean result = cacheService.hasKey(key);
        return Result.success(result);
    }

    @GetMapping("/expire")
    @Operation(summary = "获取缓存过期时间", description = "获取缓存过期时间")
    public Result<Long> getExpire(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("获取缓存过期时间, key: {}", key);
        long result = cacheService.getExpire(key, TimeUnit.SECONDS);
        return Result.success(result);
    }

    @PostMapping("/hash")
    @Operation(summary = "设置Hash缓存", description = "设置Hash缓存")
    public Result<Boolean> hashSet(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "Hash键") @RequestParam String hashKey,
            @Parameter(description = "缓存值") @RequestParam String value) {
        log.debug("设置Hash缓存, key: {}, hashKey: {}", key, hashKey);
        boolean result = cacheService.hashSet(key, hashKey, value);
        return Result.success(result);
    }

    @GetMapping("/hash")
    @Operation(summary = "获取Hash缓存", description = "获取Hash缓存")
    public Result<Object> hashGet(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "Hash键") @RequestParam String hashKey) {
        log.debug("获取Hash缓存, key: {}, hashKey: {}", key, hashKey);
        Object value = cacheService.hashGet(key, hashKey);
        return Result.success(value);
    }

    @GetMapping("/hash/all")
    @Operation(summary = "获取Hash所有键值", description = "获取Hash所有键值")
    public Result<Map<Object, Object>> hashGetAll(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("获取Hash所有键值, key: {}", key);
        Map<Object, Object> value = cacheService.hashGetAll(key);
        return Result.success(value);
    }

    @DeleteMapping("/hash")
    @Operation(summary = "删除Hash缓存", description = "删除Hash缓存")
    public Result<Long> hashDelete(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "Hash键") @RequestParam String hashKey) {
        log.debug("删除Hash缓存, key: {}, hashKey: {}", key, hashKey);
        long result = cacheService.hashDelete(key, hashKey);
        return Result.success(result);
    }

    @GetMapping("/hash/exist")
    @Operation(summary = "判断Hash缓存是否存在", description = "判断Hash缓存是否存在")
    public Result<Boolean> hashHasKey(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "Hash键") @RequestParam String hashKey) {
        log.debug("判断Hash缓存是否存在, key: {}, hashKey: {}", key, hashKey);
        boolean result = cacheService.hashHasKey(key, hashKey);
        return Result.success(result);
    }

    @GetMapping("/set")
    @Operation(summary = "获取Set缓存", description = "获取Set缓存")
    public Result<Set<Object>> setMembers(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("获取Set缓存, key: {}", key);
        Set<Object> result = cacheService.setMembers(key);
        return Result.success(result);
    }

    @PostMapping("/set")
    @Operation(summary = "往Set缓存添加元素", description = "往Set缓存添加元素")
    public Result<Long> setAdd(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "元素值") @RequestParam String value) {
        log.debug("往Set缓存添加元素, key: {}", key);
        long result = cacheService.setAdd(key, value);
        return Result.success(result);
    }

    @DeleteMapping("/set")
    @Operation(summary = "从Set缓存移除元素", description = "从Set缓存移除元素")
    public Result<Long> setRemove(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "元素值") @RequestParam String value) {
        log.debug("从Set缓存移除元素, key: {}", key);
        long result = cacheService.setRemove(key, value);
        return Result.success(result);
    }

    @GetMapping("/set/exist")
    @Operation(summary = "判断Set缓存是否包含元素", description = "判断Set缓存是否包含元素")
    public Result<Boolean> setContains(
            @Parameter(description = "缓存键") @RequestParam String key,
            @Parameter(description = "元素值") @RequestParam String value) {
        log.debug("判断Set缓存是否包含元素, key: {}", key);
        boolean result = cacheService.setContains(key, value);
        return Result.success(result);
    }

    @GetMapping("/set/size")
    @Operation(summary = "获取Set缓存大小", description = "获取Set缓存大小")
    public Result<Long> setSize(
            @Parameter(description = "缓存键") @RequestParam String key) {
        log.debug("获取Set缓存大小, key: {}", key);
        long result = cacheService.setSize(key);
        return Result.success(result);
    }
} 