package com.xhzb.nursing.service.impl;


import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.constant.RedisKeyConstant;
import com.xhzb.nursing.service.ChatHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;
import java.util.Set;

@Service
public class ChatHistoryServiceImpl implements ChatHistoryService {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private RedisMemoryChatServiceImpl redisMemoryChatServiceImpl;

    @Override
    public void saveChatHistory(String userId, String chatId) {
        //将该用户的每次会话记录存入redis中
        stringRedisTemplate.opsForSet().add(RedisKeyConstant.CHAT_HISTORY_PREFIX + userId, chatId);
    }

    @Override
    public List<String> getChatHistory(String userId) {
        List list = stringRedisTemplate
                .opsForSet()
                .members(RedisKeyConstant.CHAT_HISTORY_PREFIX + userId)
                .stream()
                .sorted(Comparator.comparing(String::toString))
                .toList();
        return list;
    }

    @Override
    public void deleteHistory(String chatId) {
        //删除指定的会话记录
        redisMemoryChatServiceImpl.clear(chatId);
        //删除redis中的会话历史
        stringRedisTemplate.opsForSet().remove(RedisKeyConstant.CHAT_HISTORY_PREFIX + SecurityUtils.getUserId(), chatId);
    }
}
