package com.xhzb;

import com.xhzb.nursing.configure.RedisVectorConfig;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;
import org.springframework.ai.rag.retrieval.search.VectorStoreDocumentRetriever;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
@Slf4j
@SpringBootTest
public class RedisStackTest {

    @Autowired
    private VectorStore vectorStore;

    @Test
    public void test() {
        Document doc1 = new Document("延庆区位于北京西北部，以山地为主（山区占72.8%），拥有海陀山等自然景观，空气质量优异（2025年7月AQI达优级），是北京市生态涵养核心区。");
        Document doc2 = new Document("北京八达岭长城是世界文化遗产，明代长城最精华段，素有“北门锁钥”之称，是万里长城的重要关隘与代表性景观。");
        List<Document> list = new ArrayList<>();
        list.add(doc1);
        list.add(doc2);
        vectorStore.add(list);
    }


    public class MyPagePdfDocumentReader {

        List<Document> getDocsFromPdf() {

            PagePdfDocumentReader pdfReader = new PagePdfDocumentReader("classpath:/养老院-医疗基础知识.pdf",
                    PdfDocumentReaderConfig.builder()
                            .withPageTopMargin(0)
                            .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                    .withNumberOfTopTextLinesToDelete(0)
                                    .build())
                            .withPagesPerDocument(1)
                            .build());

            return pdfReader.read();
        }

    }

    public List<Document> splitCustomized(List<Document> documents) {
        TokenTextSplitter splitter = new TokenTextSplitter().builder()
                .withChunkSize(1000)
                .withMinChunkSizeChars(400)
                .withMinChunkLengthToEmbed(10)
                .withMaxNumChunks(5000)
                .withKeepSeparator(true)
                .build();
        return splitter.apply(documents);
    }

    @Test
    public void test2() {
        MyPagePdfDocumentReader myPagePdfDocumentReader = new MyPagePdfDocumentReader();
        List<Document> docsFromPdf = myPagePdfDocumentReader.getDocsFromPdf();
        System.out.println(docsFromPdf);
//        List<Document> list = splitCustomized(docsFromPdf);
//        int totalSize = 10;
//        for (int i = 0; i < list.size(); i += totalSize) {
//            List<Document> document = list.subList(i, Math.min(i + totalSize, list.size()));
//            vectorStore.add(document);
//        }
    }


    @Test
    public void testRetriever() {

        SearchRequest request = SearchRequest.builder()
                .topK(5)
                .similarityThreshold(0.5)
                .query("护理服务宗旨与核心价值是什么")
                .build();
        List<Document> documents = vectorStore.similaritySearch(request);
        log.info("检索到的文档数量:{}", documents.size());
        for (Document document : documents) {
            log.info("文档内容:{}", document.getFormattedContent());
        }
    }

    @Test
    public void testRetriever1() {
        VectorStoreDocumentRetriever retriever = VectorStoreDocumentRetriever.builder()
                .vectorStore(vectorStore)
                .similarityThreshold(0.5) // 设置相似度阈值
                .topK(5) // 设置返回的文档数量
                .build();
        List<Document> documents = retriever.retrieve(new Query("护理服务宗旨与核心价值是什么"));
        System.out.println(documents);
    }
}
