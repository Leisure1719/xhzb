package com.xhzb.nursing.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@Slf4j
@RestController
@RequestMapping("/ai")
public class ChatController {

    @Autowired
    private ChatClient openAiChatClient;

    @RequestMapping(value = "/chat")
    //如果要用流式输出的话，返回值类型就不能是String了
    public Flux<String> chat(String prompt, String chatId) {
        log.info("chat - prompt: {}, chatId: {}", prompt, chatId);

        //return deepSeekChatClient.prompt()
        return openAiChatClient.prompt()
                .user(prompt)
                .stream()//call()是非流式输出，stream是流式输出
                .content();
    }
}