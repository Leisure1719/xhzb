package com.xhzb.nursing.service.impl;


import cn.hutool.json.JSONUtil;
import com.xhzb.nursing.domain.vo.Msg;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisMemoryChatServiceImpl implements ChatMemory {

    @Autowired
    private RedisTemplate redisTemplate;
    //定义在redis中的存储位置，一个：相当于一级目录，方便管理
    private static final String REDIS_KEY_PREFIX = "chat:memory:";
    /**
     *
     * @param conversationId    会话id
     * @param messages   消息数组
     */
    @Override
    public void add(String conversationId, List<Message> messages) {
        if(messages.isEmpty() || messages == null){
            return;
        }
        //首先将Message对象数据存入Msg对象中,并转成Json格式准备存入redis中
        List<String> jsonStr = messages.stream()
                .map(message -> new Msg(message))
                .map(msg -> JSONUtil.toJsonStr(msg))
                .toList();
        //将Json格式数据存入redis中
        redisTemplate.opsForList().leftPushAll(REDIS_KEY_PREFIX + conversationId,jsonStr);
    }
    /**
     *
     * 使用 range 是因为：
     * Redis List 适合存储有序的消息序列
     * range 是获取 List 中元素的标准方式
     * 配合 leftPushAll 实现消息的顺序存储与读取
     */
    @Override
    public List<Message> get(String conversationId) {
        //从redis中把json数据先取出来
        List<String> range = redisTemplate.opsForList().range(REDIS_KEY_PREFIX + conversationId, 0, -1);
        //将json数据转成Message对象
        if(range.isEmpty() || range == null){
            return List.of();
        }
        List<Message> messages = range.stream()
                .map(jsonStr -> JSONUtil.toBean(jsonStr, Msg.class))
                .map(msg -> msg.toMessage())
                .toList();

        return messages;
    }

    @Override
    public void clear(String conversationId) {
        redisTemplate.delete(REDIS_KEY_PREFIX + conversationId);
    }
}
