package com.xhzb.nursing.service.impl;

import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.json.ObjectMapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.nursing.domain.*;
import com.xhzb.nursing.domain.dto.CheckInApplyDto;
import com.xhzb.nursing.domain.vo.CheckInConfigVo;
import com.xhzb.nursing.domain.vo.CheckInDetailVo;
import com.xhzb.nursing.domain.vo.CheckInElderVo;
import com.xhzb.nursing.domain.vo.ElderFamilyVo;
import com.xhzb.nursing.service.*;
import com.xhzb.nursing.util.CodeGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.CheckInMapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Objects;

/**
 * 入住表Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService
{
    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private IHealthAssessmentService healthAssessmentService;

    @Autowired
    private IHealthAssessmentReportService healthAssessmentReportService;

    @Autowired
    private IContractService contractService;

    @Autowired
    private ICheckInConfigService checkInConfigService;

    @Autowired
    private IBedService bedService;

    @Autowired
    private IElderService elderService;
    @Autowired
    private IHealthAssessmentDataCollectionService healthAssessmentDataCollectionService;


    /**
     * 查询入住表
     * 
     * @param id 入住表主键
     * @return 入住表
     */
    @Override
    public CheckIn selectCheckInById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询入住表列表
     * 
     * @param checkIn 入住表
     * @return 入住表
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn)
    {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住表
     * 
     * @param checkIn 入住表
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn)
    {
        return save(checkIn)? 1 : 0;
    }

    /**
     * 修改入住表
     * 
     * @param checkIn 入住表
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn)
    {
        return updateById(checkIn)? 1 : 0;
    }

    /**
     * 批量删除入住表
     * 
     * @param ids 需要删除的入住表主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public CheckInDetailVo getDetailById(long id) {
        //获取老人id
        CheckIn checkin = getById(id);
        Long elderId = checkin.getElderId();
        //封装入住老人信息
        Elder elder = elderService.getById(elderId);
        HealthAssessmentDataCollection hadc = healthAssessmentDataCollectionService.getById(id);
        JSONObject json = JSONUtil.parseObj(hadc.getBasicInfo());
        CheckInElderVo checkInElderVo = BeanUtil.toBean(elder, CheckInElderVo.class);
        checkInElderVo.setAge(Integer.valueOf((json.getStr("age"))));
        CheckInDetailVo checkInDetailVo = new CheckInDetailVo();
        //封装老人家属信息=
        String elderFamily = checkin.getRemark();
        List<ElderFamilyVo> elderFamilyVoList = JSONUtil.toList(elderFamily, ElderFamilyVo.class);
        //封装入住配置信息
        CheckInConfig checkinConfig = checkInConfigService.getById(id);
        CheckInConfigVo checkInConfigVo = BeanUtil.toBean(checkinConfig, CheckInConfigVo.class);
        //封装入住开始时间，入住结束时间，床位
        checkInConfigVo.setStartDate(checkin.getStartDate());
        checkInConfigVo.setEndDate(checkin.getEndDate());
        checkInConfigVo.setBedNumber(checkin.getBedNumber());
        //封装合同信息
        Contract contract = contractService.lambdaQuery().eq(Contract::getElderId, elderId).one();
        checkInDetailVo.setCheckInConfigVo(checkInConfigVo);
        checkInDetailVo.setContract(contract);
        checkInDetailVo.setCheckInElderVo(checkInElderVo);
        checkInDetailVo.setElderFamilyVoList(elderFamilyVoList);
        return checkInDetailVo;
    }

    @Transactional
    @Override
    public void apply(CheckInApplyDto dto) {
        //评估是否完成
        Long healthAssessmentId = dto.getHealthAssessmentId();
        HealthAssessment ha = healthAssessmentService.lambdaQuery()
                .eq(HealthAssessment::getIdCard, healthAssessmentId)
                .eq(HealthAssessment::getEvaluationProgress, 1)
                .one();
        if(ha != null){
            throw new BaseException("请先完成老人评估");
        }
        //老人是否已经入住
        //是->不允许申请
        Elder olderPerson = elderService.lambdaQuery()
                .eq(Elder::getIdCardNo, dto.getCheckInElderDto().getIdCardNo())
                .in(Elder::getStatus, 0,1, 2)
                .one();
        if (Objects.nonNull(olderPerson)) {
            throw new IllegalArgumentException("老人已入住, 无法重复入住");
        }
        //否->更新床位状态为已入住
        Long bedId = dto.getCheckInConfigDto().getBedId();
        bedService.lambdaUpdate().eq(Bed::getId,bedId)
                .set(Bed::getBedStatus,1)
                .update();
        //新增或更新老人
        Elder elder = BeanUtil.toBean(dto.getCheckInElderDto(), Elder.class);
        Elder elderDb = elderService.lambdaQuery()
                .eq(Elder::getIdCardNo, elder.getIdCardNo())
                .eq(Elder::getStatus,3)
                .one();
        //如果老人存在，走更新逻辑
        if(elderDb != null){
            elder.setStatus(1);
            elder.setBedId(dto.getCheckInConfigDto().getBedId());
            elder.setBedNumber(dto.getCheckInConfigDto().getCode());
            elder.setId(elderDb.getId());
            elderService.updateById(elder);
        }else{
            //否则走新增逻辑
            elder.setStatus(1);
            elder.setBedId(dto.getCheckInConfigDto().getBedId());
            elder.setBedNumber(dto.getCheckInConfigDto().getCode());
            elderService.save(elder);
        }
        //新增合同
        Contract contract = BeanUtil.toBean(dto.getCheckInContractDto(), Contract.class);
        contract.setStatus(1);
        contract.setStartDate(dto.getCheckInConfigDto().getStartDate());
        contract.setEndDate(dto.getCheckInConfigDto().getEndDate());
        contract.setElderName(elder.getName());
        contract.setElderId(elder.getId());
        contract.setContractNumber("HT" + CodeGenerator.generateContractNumber());//合同编号
        contractService.save(contract);
        //新增入住信息
        CheckIn checkIn = new CheckIn();
        checkIn.setElderId(elder.getId());
        checkIn.setElderName(elder.getName());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setStartDate(dto.getCheckInConfigDto().getStartDate());
        checkIn.setEndDate(dto.getCheckInConfigDto().getEndDate());
        checkIn.setNursingLevelName(dto.getCheckInConfigDto().getNursingLevelName());
        checkIn.setBedNumber(elder.getBedNumber());
        checkIn.setStatus(1);
        checkIn.setRemark(JSONUtil.toJsonStr(dto.getElderFamilyDtoList()));
        save(checkIn);
        //新增入住配置
        CheckInConfig checkInConfig = BeanUtil.toBean(dto.getCheckInConfigDto(), CheckInConfig.class);
        checkInConfig.setCheckInId(checkIn.getId());
        checkInConfigService.save(checkInConfig);
        //更新评估数据，老人id，入住状态
        healthAssessmentService.lambdaUpdate()
                .eq(HealthAssessment::getId,dto.getHealthAssessmentId())
                .set(HealthAssessment::getCheckInStatus,1)
                .set(HealthAssessment::getElderId,elder.getId())
                .update();
        //更新评估报告入住状态
        healthAssessmentReportService.lambdaUpdate()
                .eq(HealthAssessmentReport::getHealthAssessmentId,dto.getHealthAssessmentId())
                .set(HealthAssessmentReport::getCheckInStatus,1)
                .update();
    }

    /**
     * 删除入住表信息
     * 
     * @param id 入住表主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
