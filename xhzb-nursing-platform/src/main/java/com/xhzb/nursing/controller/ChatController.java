package com.xhzb.nursing.controller;

import cn.hutool.core.collection.CollUtil;
import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.service.ChatHistoryService;
import com.xhzb.nursing.service.impl.RedisMemoryChatServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Delete;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.messages.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController extends BaseController {

    @Autowired
    private ChatClient openAiChatClient;

    @Qualifier("chatMemory")
    @Autowired
    private ChatMemory chatMemory;

    @Autowired
    private ChatHistoryService chatHistoryService;

    @Autowired
    private RedisMemoryChatServiceImpl redisMemoryChatService;

    @PostMapping("/chat")

    //如果要用流式输出的话，返回值类型就不能是String了
    public Flux<String> chat(String prompt, String chatId) {
        log.info("chat - prompt: {}, chatId: {}", prompt, chatId);
        chatHistoryService.saveChatHistory(SecurityUtils.getUserId() + "", chatId);


        //return deepSeekChatClient.prompt()
        return openAiChatClient
                .prompt()
                .user(prompt)
                .advisors(advisor -> advisor.param(chatMemory.CONVERSATION_ID,chatId))
                .stream()//call()是非流式输出，stream是流式输出
                .content();
    }


    @GetMapping("/history")
    public AjaxResult getChatHistory(){
        return success(chatHistoryService.getChatHistory(SecurityUtils.getUserId()+""));
    }

    @GetMapping("/history/{chatId}")
    public AjaxResult detail(@PathVariable String chatId){
        List<Message> messages = redisMemoryChatService.get(chatId);

        if(CollUtil.isEmpty(messages)){
            return success(new ArrayList<>());
        }
        List<Message> messages1 = new ArrayList<>();
        for (int i = messages.size() - 1; i >= 0; i--) {
            messages1.add(messages.get(i));
        }
        ArrayList<Map> list = new ArrayList<>();
        for (Message message : messages1) {
            HashMap<String, String> map = new HashMap<>();
            map.put("role",message.getMessageType().getValue());
            map.put("content",message.getText());
            list.add(map);
        }
        return success(list);
    }

    @Delete("/history/{chatId}")
    public AjaxResult deleteChatHistory(@PathVariable String chatId){
        chatHistoryService.deleteHistory(chatId);
        return success();
    }
}