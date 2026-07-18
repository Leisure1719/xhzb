package com.xhzb.nursing.configure;

import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.service.impl.RedisMemoryChatServiceImpl;
import com.xhzb.nursing.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SpringAiConfigure {

    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     * @param openAiChatModel
     * @return
     */
    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, WeatherTools weatherTools, RedisMemoryChatServiceImpl redisMemoryChatService) {
        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem(SystemConstants.prompt1)
                //.defaultTools(weatherTools)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisMemoryChatService).build())
                .build();
    }

//    @Bean
//    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel,WeatherTools weatherTools) {
//        return ChatClient
//                .builder(deepSeekChatModel)
//                .defaultSystem(SystemConstants.prompt)
//                .defaultTools(weatherTools)
//                .build();
//    }
}