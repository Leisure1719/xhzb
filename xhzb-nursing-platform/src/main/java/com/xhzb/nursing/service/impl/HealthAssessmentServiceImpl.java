package com.xhzb.nursing.service.impl;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.SecurityUtils;
import com.xhzb.nursing.domain.HealthAssessmentDataCollection;
import com.xhzb.nursing.domain.HealthAssessmentReport;
import com.xhzb.nursing.domain.dto.ElderAssessmentDto;
import com.xhzb.nursing.service.IHealthAssessmentDataCollectionService;
import com.xhzb.nursing.service.IHealthAssessmentReportService;
import com.xhzb.nursing.util.PdfDocumentReader;
import com.xhzb.oss.client.OSSAliyunFileStorageService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.document.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.HealthAssessmentMapper;
import com.xhzb.nursing.domain.HealthAssessment;
import com.xhzb.nursing.service.IHealthAssessmentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Map;

/**
 * 健康评估记录Service业务层处理
 *
 * @author ruoyi
 * @date 2026-07-21
 */
@Service
public class HealthAssessmentServiceImpl extends ServiceImpl<HealthAssessmentMapper, HealthAssessment> implements IHealthAssessmentService {
    @Autowired
    private HealthAssessmentMapper healthAssessmentMapper;

    @Autowired
    private IHealthAssessmentDataCollectionService healthAssessmentDataCollectionService;

    @Autowired
    private ChatClient simpleOpenAiChatClient;

    @Autowired
    private OSSAliyunFileStorageService ossAliyunFileStorageService;

    @Autowired
    private IHealthAssessmentReportService healthAssessmentReportService;

    /**
     * 查询健康评估记录
     *
     * @param id 健康评估记录主键
     * @return 健康评估记录
     */
    @Override
    public HealthAssessmentDataCollection selectHealthAssessmentById(Long id) {
        return healthAssessmentDataCollectionService.getById(id);
    }

    /**
     * 查询健康评估记录列表
     *
     * @param healthAssessment 健康评估记录
     * @return 健康评估记录
     */
    @Override
    public List<HealthAssessment> selectHealthAssessmentList(HealthAssessment healthAssessment) {
        return healthAssessmentMapper.selectHealthAssessmentList(healthAssessment);
    }

    /**
     * 新增健康评估记录
     *
     * @param dto 健康评估记录
     * @return 结果
     */
    @Override
    @Transactional
    public long insertHealthAssessment(ElderAssessmentDto dto) {
        return saveOrUpdateHealthAssessment(dto);
    }

    /**
     * 修改健康评估记录
     *
     * @param dto 健康评估记录
     * @return 结果
     */
    @Override
    @Transactional
    public long updateHealthAssessment(ElderAssessmentDto dto) {
        return saveOrUpdateHealthAssessment(dto);
    }


    private long saveOrUpdateHealthAssessment(ElderAssessmentDto dto) {
        // 新增 health_assessment 数据(由于ElderAssessmentDto继承了base)
        HealthAssessment ha = new HealthAssessment();
        // 判断是否接受到了ID，如果有就走新增，没有就走更新
        if (dto.getId() != null) {
            // 根据id获取 health_assessment 数据
            ha = getById(dto.getId());
        } else {
            // 新增逻辑的基础数据初始胡
            // 核心建议 ： 默认 null
            ha.setCoreSuggestion(null);
            // 入住状态 ： 默认 0 未入住
            ha.setCheckInStatus(0);
            // 评估状态 ： 默认 0 评估中
            ha.setEvaluationProgress(0);
        }
        ha.setElderName(dto.getBasicInfo().getElderName());
        ha.setIdCard(dto.getBasicInfo().getIdCard());
        // 判断ha里是否有id，有id就走修改
        saveOrUpdate(ha);
        // 新增 health_assessment_data_collection 数据
        HealthAssessmentDataCollection hd = new HealthAssessmentDataCollection();
        hd.setId(ha.getId());
        hd.setBasicInfo(JSONUtil.toJsonStr(dto.getBasicInfo()));
        hd.setHealthAssessment(JSONUtil.toJsonStr(dto.getHealthAssessmentDto()));
        hd.setDailyLivingActivities(JSONUtil.toJsonStr(dto.getDailyLivingActivities()));
        hd.setMentalState(JSONUtil.toJsonStr(dto.getMentalState()));
        hd.setPerceptionCommunication(JSONUtil.toJsonStr(dto.getPerceptionAndCommunication()));
        hd.setSocialParticipation(JSONUtil.toJsonStr(dto.getSocialParticipation()));
        healthAssessmentDataCollectionService.saveOrUpdate(hd);
        // 新增 health_assessment_report 数据
        return ha.getId();
    }

    /**
     * 批量删除健康评估记录
     *
     * @param ids 需要删除的健康评估记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAssessmentByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除健康评估记录信息
     *
     * @param id 健康评估记录主键
     * @return 结果
     */
    @Override
    public int deleteHealthAssessmentById(Long id) {
        return removeById(id) ? 1 : 0;
    }

    @Override
    public Map getElderInfo(Long id) {

        HealthAssessment ha = lambdaQuery().eq(HealthAssessment::getId, id).one();
        if(ha == null){
            throw new BaseException("老人健康信息不存在");
        }
        HashMap map = new HashMap<>();
        map.put("coreSuggestion",ha.getCoreSuggestion());
        HealthAssessmentDataCollection hadc = healthAssessmentDataCollectionService.lambdaQuery()
                .eq(HealthAssessmentDataCollection::getId, id)
                .one();
        String basicInfo = hadc.getBasicInfo();
        JSONObject json = JSONUtil.parseObj(basicInfo);
        map.put("phone",json.get("elderContact"));
        map.put("medicalPaymentMethod",json.get("medicalPaymentMethod"));
        map.put("nation",json.get("nation"));
        map.put("educationLevel",json.get("educationLevel"));
        map.put("idCardNo",json.get("idCard"));
        map.put("name",json.get("elderName"));
        map.put("socialSecurityCard",json.get("socialSecurityCard"));
        map.put("livingSituation",json.get("livingSituation"));
        map.put("religiousBelief",json.get("religiousBelief"));
        map.put("economicSource",json.get("economicSource"));
        map.put("maritalStatus",json.get("maritalStatus"));
        return map;
    }

    @Override
    @Transactional
    public long assessmentData(ElderAssessmentDto dto) {
        //1.老人能力评估
        //获取dto数据
        // 更新数据
        Long id = saveOrUpdateHealthAssessment(dto);

        //获取各个指标等级
        // 生活能力等级
        String abilityRating = dto.getDailyLivingActivities().getAbilityRating();
        // 精神状态等级
        String mentalStateRating = dto.getMentalState().getAbilityRating();
        // 感知与沟通等级
        String perceptionAndCommunicationRating = dto.getPerceptionAndCommunication().getAbilityRating();
        // 社会参与等级
        String socialParticipationRating = dto.getSocialParticipation().getAbilityRating();

        // AI 分析获取能力评估结果
        // 跌倒
        Integer fall = dto.getHealthAssessmentDto().getRecent30Days().getFall();
        // 噎食
        Integer lost = dto.getHealthAssessmentDto().getRecent30Days().getLost();
        //自杀
        Integer choking = dto.getHealthAssessmentDto().getRecent30Days().getChoking();
        //走失者
        Integer suicideAttempt = dto.getHealthAssessmentDto().getRecent30Days().getSuicideAttempt();
        //昏迷
        Integer coma = dto.getHealthAssessmentDto().getRecent30Days().getComa();
        // 痴呆
        String dementia = dto.getHealthAssessmentDto().getDiseaseDiagnosis().getDementia();
        // 精神疾病
        String mentalIllness = dto.getHealthAssessmentDto().getDiseaseDiagnosis().getMentalIllness();
        // 认知障碍:画钟测验结果(0-正确，1-错误，2-确诊认知障碍)
        String cognitiveImpairment = dto.getMentalState().getClockDrawingTest().getResult() == 2 ? "有" : "无";
        //把数据填入提示词里面
        String abilityPrompt = getAbilityRating(abilityRating, mentalStateRating, perceptionAndCommunicationRating,
                socialParticipationRating, fall, lost, choking, suicideAttempt, coma,
                dementia, mentalIllness, cognitiveImpairment);
        // 调用AI获取能力评估结果
        String abilityStr = simpleOpenAiChatClient.prompt().user(abilityPrompt).call().content();
        System.out.println(abilityStr);
        // 解析AI返回结果
        JSONObject abilityJson = JSONUtil.parseObj(abilityStr);
        String preLevel = abilityJson.getStr("preLevel"); //老年人等级初步等级
        String finalLevel = abilityJson.getStr("finalLevel"); //老年人等级最终等级
        String reason = abilityJson.getStr("reason"); //老年人等级变更依据说明
        //从oss中下载体检报告
        String reportUrl = dto.getHealthAssessmentDto().getRecent30Days().getMedicalReport();
        PdfDocumentReader pdfDocumentReader = new PdfDocumentReader();
        //读取pdf数据
        List<Document> documents = pdfDocumentReader.getDocsFromPdf(reportUrl);
        StringBuilder sb = new StringBuilder();
        for (Document document : documents) {
            sb.append(document.getText());
        }
        String report = sb.toString();
        //把数据填入提示词
        String healthReportPrompt = getHealthReportPrompt(report);
        //将提示词提交给AI
        String reportStr = simpleOpenAiChatClient.prompt().user(healthReportPrompt).call().content();
        //解析AI返回的数据
        JSONObject reportJson = JSONUtil.parseObj(reportStr);
        double healthScore = reportJson.getDouble("healthScore");//健康评分
        String riskLevel = reportJson.getStr("riskLevel");//危险等级
        String abnormalData = JSONUtil.toJsonStr(reportJson.getStr("abnormalData"));//异常分析
        String systemScore = JSONUtil.toJsonStr(reportJson.getStr("systemScore"));//健康系统分值
        String summarize = reportJson.getStr("summarize");//体检报告总结
        //更新health_assessment_data_collection数据
        HealthAssessmentReport har = new HealthAssessmentReport();
        har.setHealthAssessmentId(dto.getId());
        har.setAssessmentTime(LocalDateTime.now());
        har.setAssessorName(SecurityUtils.getUsername());
        har.setDailyActivityLevel(abilityRating);
        har.setMentalStatusLevel(mentalStateRating);
        har.setPerceptionCommunicationLevel(perceptionAndCommunicationRating);
        har.setSocialParticipationLevel(socialParticipationRating);
        har.setInitialAbilityLevel(preLevel);
        har.setFinalAbilityLevel(finalLevel);
        har.setLevelChangeReason(reason);
        har.setHealthScore(healthScore + "");
        har.setRiskLevel(riskLevel);
        har.setReportSummary(summarize);
        har.setAbnormalAnalysis(abnormalData);
        har.setSystemScore(systemScore);
        har.setCoreSuggestion(healthScore > 60 ? 1 : 0);
        har.setCheckInStatus(0);
        healthAssessmentReportService.save(har);
        //更新health_assessment数据
        lambdaUpdate().eq(HealthAssessment::getId, id)
                .set(HealthAssessment::getEvaluationProgress, 1)
                .set(HealthAssessment::getCoreSuggestion, 1)
                .update();
        return id;

    }

    /**
     * 删除评估记录
     */
    @Override
    @Transactional
    public void removeReport(Long id) {
        //将health_assessment中的evaluation_progress置为0
        Long healthAssessmentId = healthAssessmentReportService.getById(id).getHealthAssessmentId();
        lambdaUpdate().eq(HealthAssessment::getId, healthAssessmentId)
                .set(HealthAssessment::getEvaluationProgress, 0)
                .update();
        //删除health_assessment_report数据
        healthAssessmentReportService.removeById(id);


    }


    public static String getAbilityRating(String abilityRating, String mentalStateRating, String perceptionAndCommunicationRating,
                                          String socialParticipationRating, Integer fall, Integer lost, Integer choking, Integer suicideAttempt, Integer coma,
                                          String dementia, String mentalIllness, String cognitiveImpairment) {
        String prompt = """
                ## 老人评估的的信息：
                - 日常生活活动分级：%s
                - 精神状态分级：%s
                - 感知觉与沟通分级：%s
                - 社会参与分级：%s
                - 跌倒次数：%s
                - 噎食次数：%s
                - 自杀次数：%s
                - 走失次数：%s
                - 昏迷次数：%s
                - 痴呆疾病：%s
                - 精神疾病：%s
                - 是否确诊为认知障碍：%s
                
                ## 评估规则1：
                - 能力完好：
                    日常生活活动、精神状态、感知觉与沟通分级均为0，社会参与分级为0或1
                - 轻度失能：
                    日常生活活动分级为0，但精神状态、感知觉与沟通中至少一项分级为1及以上，或社会参与的分级为2；
                    或日常生活活动分级为1，精神状态、感知觉与沟通、社会参与中至少有一项的分级为0或1
                - 中度失能：
                    日常生活活动分级为1，但精神状态、感知觉与沟通、社会参与均为2，或有一项为3；
                    或日常生活活动分级为2，且精神状态、感知觉与沟通、社会参与中有1-2项的分级为1或2
                - 重度失能：
                    日常生活活动的分级为3；
                    或日常生活活动、精神状态、感知觉与沟通、社会参与分级均为2；
                    或日常生活活动分级为2，且精神状态、感知觉与沟通、社会参与中至少有一项分级为3
                
                ## 评估原则2：
                1.有认知障碍/痴呆、精神疾病者，在原有能力级别上提高一个等级；
                2.近30天内发生过2次及以上跌倒、噎食、自杀、走失者，在原有能力级别上提高一个等级；
                3.处于昏迷状态者，直接评定为重度失能；
                4.若初步等级确定为“3重度失能”，则不考虑上述1-3中各情况对最终等级的影响，等级不再提高
                
                ## 匹配规则
                1. 请根据老人的评估信息与规则1逐条进行比对，判断老人属于哪一种能力
                2. 然后拿老人的评估信息逐条与规则2进行比对，再次判断老人属于哪一种能力。
                3. 如果两次评级不一样，升级的理由是什么，理由只需要填写评估原则2 的一条或多条内容，把内容输出到reason中，不需要说明分析理由
                4. 结合评估的原则，给出老人的两次评级，不需要输出分析过程，只需要输出json格式，不要出现markdown语法
                格式为：
                {{
                    "preLevel": "能力完好|轻度失能|中度失能|重度失能",
                    "finalLevel": "能力完好|轻度失能|中度失能|重度失能",
                    "reason":"评估原则2中一条或多条"
                }}
                """;

        return prompt.formatted(abilityRating, mentalStateRating, perceptionAndCommunicationRating,
                socialParticipationRating, fall, lost, choking, suicideAttempt, coma,
                dementia, mentalIllness, cognitiveImpairment);
    }

    private String getHealthReportPrompt(String content) {
        String prompt = """
                请以一个专业医生的视角来分析这份体检报告，报告中包含了一些异常数据，我需要您对这些数据进行解读，并给出相应的健康建议。
                体检内容如下：
                %s
                要求：
                1. 提取体检报告中的“总检日期”；
                2. 通过临床医学、疾病风险评估模型和数据智能分析，给该用户的风险等级和健康指数给出结果。风险等级分为：健康、提示、风险、危险、严重危险。健康指数范围为0至100分；
                3. 对于体检报告有异常数据，请列出（异常数据的结论、体检项目名称、检查结果、参考值、单位、异常解读、建议）这8字段。解读异常数据，解决这些数据可能代表的健康问题或风险。分析可能的原因，包括但不限于生活习惯、饮食习惯、遗传因素等。基于这些异常数据和可能的原因，请给出具体的健康建议，包括饮食调整、运动建议、生活方式改变以及是否需要进一步检查或治疗等。
                结论格式：异常数据的结论：肥胖，体检项目名称：体重指数BMI，检查结果：29.2，参考值>24，单位：-。异常解读：体重超标包括超重与肥胖。体重指数（BMI）=体重（kg）/身⾼（m）的平⽅，BMI≥24为超重，BMI≥28为肥胖；男性腰围≥90cm和⼥性腰围≥85cm为腹型肥胖。体重超标是⼀种由多因素（如遗传、进⻝油脂较多、运动少、疾病等）引起的慢性代谢性疾病，尤其是肥胖，已经被世界卫⽣组织列为导致疾病负担的⼗⼤危险因素之⼀。AI建议：采取综合措施预防和控制体重，积极改变⽣活⽅式，宜低脂、低糖、⾼纤维素膳⻝，多⻝果蔬及菌藻类⻝物，增加有氧运动。若有相关疾病（如⾎脂异常、⾼⾎压、糖尿病等）应积极治疗。
                4. 根据这个体检报告的内容，分别是给人体的8大系统打分，每项满分为100分，8大系统分别为：呼吸系统、消化系统、内分泌系统、免疫系统、循环系统、泌尿系统、运动系统、感官系统
                5. 给体检报告做一个总结，总结格式：体检报告中尿蛋⽩、癌胚抗原、⾎沉、空腹⾎糖、总胆固醇、⽢油三酯、低密度脂蛋⽩胆固醇、⾎清载脂蛋⽩B、动脉硬化指数、⽩细胞、平均红细胞体积、平均⾎红蛋⽩共12项指标提示异常，尿液常规共1项指标处于临界值，⾎脂、⾎液常规、尿液常规、糖类抗原、⾎清酶类等共43项指标提示正常，综合这些临床指标和数据分析：肾脏、肝胆、⼼脑⾎管存在隐患，其中⼼脑⾎管有“⾼危”⻛险；肾脏部位有“中危”⻛险；肝胆部位有“低危”⻛险。
                
                # 输出要求：
                最后，将以上结果输出为纯JSON格式，不要包含其他的文字说明，也不要出现Markdown语法相关的文字，所有的返回结果都是json，注意双引号的单引号的配合使用，详细格式如下：
                
                {
                  "healthScore": XX.XX,
                  "riskLevel": "健康|提示|风险|危险|严重危险",
                  "abnormalData": [
                    {
                      "conclusion": "异常数据的结论",
                      "examinationItem": "体检项目名称",
                      "result": "检查结果",
                      "referenceValue": "参考值",
                      "unit": "单位",
                      "interpret":"对于异常的结论进一步详细的说明",
                      "advice":"针对于这一项的异常，给出一些健康的建议"
                    }
                  ],
                  "systemScore": {
                    "breathingSystem": XX,
                    "digestiveSystem": XX,
                    "endocrineSystem": XX,
                    "immuneSystem": XX,
                    "circulatorySystem": XX,
                    "urinarySystem": XX,1
                    "motionSystem": XX,
                    "senseSystem": XX
                  },
                  "summarize": "体检报告的总结"
                }
                
                """;
        return prompt.formatted(content);
    }

}
