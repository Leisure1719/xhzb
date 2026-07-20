package com.xhzb.nursing.configure;

import com.xhzb.nursing.constant.SystemConstants;
import com.xhzb.nursing.service.impl.RedisMemoryChatServiceImpl;
import com.xhzb.nursing.tools.WeatherTools;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.SimpleLoggerAdvisor;


import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class SpringAiConfigure {


    /**
     * 创建并返回一个ChatClient的Spring Bean实例。
     *
     * @param openAiChatModel
     * @return
     */
    @Bean
    public ChatClient openAiChatClient(OpenAiChatModel openAiChatModel, WeatherTools weatherTools, RedisMemoryChatServiceImpl redisMemoryChatService, VectorStore vectorStore) {
        QuestionAnswerAdvisor questionAnswerAdvisor = QuestionAnswerAdvisor
                .builder(vectorStore)
                .searchRequest(SearchRequest.builder()
                        .similarityThreshold(0.7d)
                        .topK(5)
                        .build())
                .build();

        return ChatClient
                .builder(openAiChatModel)
                .defaultSystem(SystemConstants.prompt1)
                //.defaultTools(weatherTools)
                .defaultAdvisors(new SimpleLoggerAdvisor(),
                        MessageChatMemoryAdvisor.builder(redisMemoryChatService).build(),
                        questionAnswerAdvisor)
                .build();
    }

    @Bean("chatMemory")
    public ChatMemory chatMemory(RedisMemoryChatServiceImpl redisMemoryChatService) {
        return redisMemoryChatService;
    }

//    @Bean
//    public ChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel,WeatherTools weatherTools) {
//        return ChatClient
//                .builder(deepSeekChatModel)
//                .defaultSystem(SystemConstants.prompt)
//                .defaultTools(weatherTools)
//                .build();
//    }

    @Bean
    public TextSplitter splitterText() {
        return TokenTextSplitter.builder()
                .withChunkSize(1000)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();

    }

}