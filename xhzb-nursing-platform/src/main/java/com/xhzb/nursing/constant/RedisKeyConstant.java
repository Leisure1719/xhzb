package com.xhzb.nursing.constant;

public class RedisKeyConstant {
    //大模型对话历史
    public static final String CHAT_HISTORY_PREFIX = "chat:history:";
    //大模型会话记忆
    public static final String REDIS_KEY_PREFIX = "chat:memory:";
    //iot设备信息
    public static final String IOT_ALL_PRODUCT_LIST = "iot:product";
    //iot设备数据
    public static final String IOT_DEVICE_DATA_KEY = "iot:device";

    /**
     * 沉默周期 redis key 前缀 使用redis的setIfAbsent
     * setIfAbsent = Redis 原生命令 SETNX (SET if Not eXists)
     */
    public static final String IOT_ALERT_SILENT_PREFIX = "iot:alert:silent:{}:{}:{}";


    /**
     * 报警次数 redis key 前缀 使用redis的原子自增increment ()
     * 自动初始化：key 不存在时，自动创建并从 0 开始自增, 返回自增后的最新值 ,
     * 只能对整数类型的值操作，非数字会报错
     */
    public static final String IOT_ALERT_COUNT_PREFIX = "iot:alert:count:{}:{}:{}";
}
