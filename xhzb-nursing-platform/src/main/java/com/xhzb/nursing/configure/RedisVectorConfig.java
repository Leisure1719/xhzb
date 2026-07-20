package com.xhzb.nursing.configure;

import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.TokenCountBatchingStrategy;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.JedisPooled;

/**
 * Redis向量存储配置类
 * 用于配置基于Redis的向量数据库，支持AI语义搜索功能
 */
@Configuration
public class RedisVectorConfig {
    @Value("${spring.data.redis.host}")
    private String redisHost;

    /**
     * 创建Jedis连接池实例
     * 用于连接Redis服务器，提供向量数据存储能力
     * 
     * @return JedisPooled Redis连接池对象
     */
    @Bean
    public JedisPooled jedisPooled() {
        // 连接到指定IP和端口的Redis服务器
        return new JedisPooled(redisHost, 6378);
    }

    /**
     * 创建向量存储实例
     * 配置Redis作为向量数据库，用于存储和检索AI生成的向量嵌入
     * 
     * @param jedisPooled Redis连接池
     * @param openAiEmbeddingModel OpenAI嵌入模型，用于生成文本向量
     * @return VectorStore 向量存储对象
     */
    @Bean
    public VectorStore vectorStore(JedisPooled jedisPooled, OpenAiEmbeddingModel openAiEmbeddingModel) {
        return RedisVectorStore.builder(jedisPooled, openAiEmbeddingModel)
                .indexName("spring-ai-index")                // 设置向量索引名称，默认为"spring-ai-index"
                .prefix("doc:")                  // 设置Redis键前缀，默认为"doc:"
                .metadataFields(                         // 定义元数据字段，用于过滤和检索
                        RedisVectorStore.MetadataField.tag("country"),   // 国家标签字段
                        RedisVectorStore.MetadataField.numeric("year"))  // 年份数值字段
                .initializeSchema(true)                   // 是否初始化索引架构，默认false
                .batchingStrategy(new TokenCountBatchingStrategy()) // 批处理策略，按token数量分批处理
                .build();
    }

}