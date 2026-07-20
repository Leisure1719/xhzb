package com.xhzb.nursing.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;

import cn.hutool.json.JSONUtil;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.common.utils.uuid.UUID;
import com.xhzb.oss.client.OSSAliyunFileStorageService;
import org.springframework.ai.document.Document;
import org.springframework.ai.reader.ExtractedTextFormatter;
import org.springframework.ai.reader.pdf.PagePdfDocumentReader;
import org.springframework.ai.reader.pdf.config.PdfDocumentReaderConfig;
import org.springframework.ai.transformer.splitter.TextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.KnowledgeBaseMapper;
import com.xhzb.nursing.domain.KnowledgeBase;
import com.xhzb.nursing.service.IKnowledgeBaseService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Map;

/**
 * 知识库Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-19
 */
@Service
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase> implements IKnowledgeBaseService
{
    @Autowired
    private KnowledgeBaseMapper knowledgeBaseMapper;
    @Autowired
    private OSSAliyunFileStorageService ossAliyunFileStorageService;
    @Autowired
    private TextSplitter textSplitter;
    @Autowired
    private VectorStore vectorStore;

    /**
     * 查询知识库
     * 
     * @param id 知识库主键
     * @return 知识库
     */
    @Override
    public KnowledgeBase selectKnowledgeBaseById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询知识库列表
     * 
     * @param knowledgeBase 知识库
     * @return 知识库
     */
    @Override
    public List<KnowledgeBase> selectKnowledgeBaseList(KnowledgeBase knowledgeBase)
    {
        return knowledgeBaseMapper.selectKnowledgeBaseList(knowledgeBase);
    }

    /**
     * 新增知识库
     * 
     * @param knowledgeBase 知识库
     * @return 结果
     */
    @Override
    public int insertKnowledgeBase(KnowledgeBase knowledgeBase) {
        //从阿里云下载文档
        InputStream fileStream = ossAliyunFileStorageService.download(knowledgeBase.getDocumentUrl());

        //按页读取文档字节流
        if(fileStream == null){
            throw new RuntimeException("该文件不存在");
        }
        PagePdfDocumentReader pdfReader = new PagePdfDocumentReader(new InputStreamResource(fileStream),
                PdfDocumentReaderConfig.builder()
                        .withPageTopMargin(0)
                        .withPageExtractedTextFormatter(ExtractedTextFormatter.builder()
                                .withNumberOfTopTextLinesToDelete(0)
                                .build())
                        .withPagesPerDocument(1)
                        .build());

        List<Document> documents = pdfReader.read();
        //拆分文档
        List<Document> splitDocuments = textSplitter.split(documents);
        //获取所有文档的id
        List<String> ids = splitDocuments.stream().map(Document::getId).toList();
        //分批次添加到Redis向量数据库
        int totalSize = 10;
        for (int i = 0; i < splitDocuments.size(); i+=totalSize) {
            List<Document> batch = splitDocuments.subList(i, Math.min(i + totalSize, splitDocuments.size()));
            vectorStore.add(batch);
        }
        //文档保存到数据库表
        knowledgeBase.setCreateTime(DateUtils.getNowDate());
        knowledgeBase.setCreateBy(SecurityUtils.getUserId().toString());
        knowledgeBase.setRemark(JSONUtil.toJsonStr(ids));
        return knowledgeBaseMapper.insertKnowledgeBase(knowledgeBase);

    }

    /**
     * 修改知识库
     * 
     * @param knowledgeBase 知识库
     * @return 结果
     */
    @Override
    public int updateKnowledgeBase(KnowledgeBase knowledgeBase)
    {
        return updateById(knowledgeBase)? 1 : 0;
    }

    /**
     * 批量删除知识库
     * 
     * @param id 需要删除的知识库主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeBaseByIds(long id) {
        return 0;
    }
    /**
     * 删除知识库信息
     * 
     * @param id 知识库主键
     * @return 结果
     */
    @Override
    public int deleteKnowledgeBaseById(Long id)
    {
        //根据id查询出数据
        KnowledgeBase knowledgeBase = selectKnowledgeBaseById(id);
        //删除向量数据库内容
        String idsStr = knowledgeBase.getRemark();
        List<String> ids = JSONUtil.toList(idsStr, String.class);
        vectorStore.delete(ids);
        //删除oss文件
        ossAliyunFileStorageService.delete(knowledgeBase.getDocumentUrl());
        //删除数据表
        return knowledgeBaseMapper.deleteKnowledgeBaseById(id);
    }

    @Override
    public Map upload(MultipartFile file) throws IOException {
        String fileName = file.getOriginalFilename();
        //获取文件名后缀
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        //给文件名拼接uuid，为了文件名不重复
        String uuidName = UUID.randomUUID() + suffix;
        //将文件上传到oss
        String url = ossAliyunFileStorageService.store(uuidName, file.getInputStream());
        //准备返回给前端的数据
        AjaxResult ajax = AjaxResult.success();
        ajax.put("fileName",url);
        ajax.put("url",url);
        ajax.put("originalFilename",fileName);
        return ajax;
    }
}
