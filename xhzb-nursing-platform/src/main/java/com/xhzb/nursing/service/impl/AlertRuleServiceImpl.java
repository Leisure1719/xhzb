package com.xhzb.nursing.service.impl;

import java.sql.Array;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.chrono.ChronoLocalDate;
import java.time.chrono.Chronology;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateTime;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.nursing.constant.RedisKeyConstant;
import com.xhzb.nursing.domain.AlertData;
import com.xhzb.nursing.domain.DeviceData;
import com.xhzb.nursing.service.IAlertDataService;
import com.xhzb.system.domain.SysUserRole;
import com.xhzb.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.AlertRuleMapper;
import com.xhzb.nursing.domain.AlertRule;
import com.xhzb.nursing.service.IAlertRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

/**
 * 报警规则Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-30
 */
@Slf4j
@Service
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements IAlertRuleService
{
    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Value("${alertRule.roleAdmin}")
    private String roleAdmin;

    @Value("${alertRule.roleRepair}")
    private String roleRepair;

    @Autowired
    private IAlertDataService alertDataService;

    /**
     * 查询报警规则
     * 
     * @param id 报警规则主键
     * @return 报警规则
     */
    @Override
    public AlertRule selectAlertRuleById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询报警规则列表
     * 
     * @param alertRule 报警规则
     * @return 报警规则
     */
    @Override
    public List<AlertRule> selectAlertRuleList(AlertRule alertRule)
    {
        return alertRuleMapper.selectAlertRuleList(alertRule);
    }

    /**
     * 新增报警规则
     * 
     * @param alertRule 报警规则
     * @return 结果
     */
    @Override
    public int insertAlertRule(AlertRule alertRule)
    {
        return save(alertRule)? 1 : 0;
    }

    /**
     * 修改报警规则
     * 
     * @param alertRule 报警规则
     * @return 结果
     */
    @Override
    public int updateAlertRule(AlertRule alertRule)
    {
        return updateById(alertRule)? 1 : 0;
    }

    /**
     * 批量删除报警规则
     * 
     * @param ids 需要删除的报警规则主键
     * @return 结果
     */
    @Override
    public int deleteAlertRuleByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public void alertRuleFilter() {
        //查询所有规则数据,没有预警规则就直接结束
        Long count = lambdaQuery().eq(AlertRule::getStatus, 1)//已启用的规则
                .count();
        if(count == 0){
            log.info("[定时报警规则校验]没有任何报警规则，无需处理");
            return;
        }
        //查询所有设备的上报数据,没有数据就直接结束
        List<Object> deviceDatas = redisTemplate.opsForHash().values(RedisKeyConstant.IOT_DEVICE_DATA_KEY);
        if(CollUtil.isEmpty(deviceDatas)){
            log.info("[定时报警规则校验]没有任何设备数据，无需处理");
            return;
        }
        ArrayList<DeviceData> allDeviceDataList = new ArrayList<>();
        //将设备数据进行类型转换
        for (Object deviceData : deviceDatas) {//每一个设备的所有物模型数据
            List<DeviceData> oneDeviceDataList = JSONUtil.toList((String) deviceData, DeviceData.class);//具体的每一条物模型数据
            allDeviceDataList.addAll(oneDeviceDataList);
        }
        //判断设备上报数据
        for ( DeviceData deviceData: allDeviceDataList) {
            //上报数据时间间隔是否过长,过长则continue
            LocalDateTime alarmTime = deviceData.getAlarmTime();
            long between = ChronoUnit.SECONDS.between(alarmTime, LocalDateTime.now());
            if(between > 600){
                log.info("[定时报警规则校验]上报数据时间间隔过长");
                continue;
            }
            //查询所有设备报警规则
            List<AlertRule> productRuleList = lambdaQuery().eq(AlertRule::getProductKey, deviceData.getProductKey())//同一个产品
                    .eq(AlertRule::getIotId, -1)//设备id,如果是-1代表全部设备
                    .eq(AlertRule::getStatus, 1)//启用的规则
                    .eq(AlertRule::getFunctionId, deviceData.getFunctionId())//功能id
                    .list();
            //判空
            if(CollUtil.isEmpty(productRuleList)){
                productRuleList = new ArrayList<>();
            }
            //查询对应设备报警规则
            List<AlertRule> deviceRuleList = lambdaQuery().eq(AlertRule::getProductKey, deviceData.getProductKey())//同一个产品
                    .eq(AlertRule::getIotId, deviceData.getIotId())//设备id,如果是-1代表全部设备
                    .eq(AlertRule::getStatus, 1)//启用的规则
                    .eq(AlertRule::getFunctionId, deviceData.getFunctionId())//功能id
                    .list();
            //判空
            if(CollUtil.isEmpty(deviceRuleList)){
                deviceRuleList = new ArrayList<>();
            }
            //合并规则
            productRuleList.addAll(deviceRuleList);
            //判空,为空直接continue
            if(CollUtil.isEmpty(productRuleList)){
                log.info("[定时报警规则校验]当前上报的设备数据无匹配的报警规则");
                continue;
            }
            //去重
            productRuleList = productRuleList.stream().distinct().toList();
            //遍历规则
            for (AlertRule alertRule : productRuleList) {
                String alertEffectivePeriod = alertRule.getAlertEffectivePeriod();//格式为08:00:00~10:00:00
                //分割提取开始和结束时间
                String[] split = alertEffectivePeriod.split("~");
                LocalTime start = LocalTime.parse(split[0]);
                LocalTime end = LocalTime.parse(split[1]);
                //规则是否在生效期内,不在直接continue
                if(LocalTime.now().isBefore(start) || LocalTime.now().isAfter(end)){
                    log.info("[定时报警规则校验]当前规则不在生效期内");
                    continue;
                }
                //获取阈值
                Double value = alertRule.getValue();
                //获取设备上报数据值
                double dataValue = Double.parseDouble(deviceData.getDataValue());
                int compare = Double.compare(dataValue, value);//如果dataValue>value  compare>0
                //获取运算符
                String operator = alertRule.getOperator();//1.>=  2.<
                //设备上报数据是否到达阈值,未到达continue
                if((compare >= 0 && operator.equals(">=")) || (compare < 0 && operator.equals("<"))){
                    String silentKey = CharSequenceUtil.format(RedisKeyConstant.IOT_ALERT_SILENT_PREFIX,
                            deviceData.getIotId(), deviceData.getFunctionId(), alertRule.getId());
                    String silentValue = redisTemplate.opsForValue().get(silentKey);
                    //规则是否在沉默期内,在则continue
                    if(silentValue != null){
                        log.info("[定时报警规则校验]当前规则还在沉默期内");
                        continue;
                    }
                    String duration = CharSequenceUtil.format(RedisKeyConstant.IOT_ALERT_COUNT_PREFIX, deviceData.getIotId(),
                            deviceData.getFunctionId(), alertRule.getId());
                    Long increment = redisTemplate.opsForValue().increment(duration);
                    //规则是否达到持续周期,未达到则continue
                    if(increment < alertRule.getDuration()){
                        log.info("[定时报警规则校验]持续周期次数还未达到，无需处理");
                        continue;
                    }
                    //保存沉默周期的值到Redis，过期时间为沉默周期对应的秒数
                    redisTemplate.opsForValue().setIfAbsent(silentKey,"❤迪哥❤",alertRule.getAlertSilentPeriod()*60, TimeUnit.SECONDS);
                    //删除redis中的持续周期数据
                    redisTemplate.delete(duration);
                    //------------保存报警数据---------------
                    //查询超级管理员的userId
                    List<Long> admins = sysUserRoleMapper.selectUserIdsByRoleName(roleAdmin);
                    List<Long> ids = new ArrayList();
                    //判断是固定设备还是移动设备
                    if(alertRule.getAlertDataType() == 0){
                        //判断设备绑定床位还是老人
                        if(deviceData.getLocationType() == 0){
                            //根据老人id查询护理人员
                            ids = alertRuleMapper.selectNursingIdsByElderId(Long.parseLong(deviceData.getAccessLocation()));
                        }else{
                            //根据床位id查询护理人员
                            ids = alertRuleMapper.selectNursingIdsByBedId(Long.parseLong(deviceData.getAccessLocation()));
                        }
                    }else{//固定设备异常数据
                        //根据设备名称查询修理员
                        ids = sysUserRoleMapper.selectUserIdsByRoleName(roleRepair);
                    }
                    //合并预警人员
                    ids.addAll(admins);
                    //去重
                    ids = ids.stream().distinct().toList();
                    //封装数据
                    ArrayList<AlertData> alertDataList = new ArrayList<>();//拼接reason字段
                    String reason = "功能名称 + %s + 运算符 + %s + 阈值 + %s + 持续周期 + %s + 沉默周期 + %s";
                    String.format(reason,alertRule.getFunctionName(),alertRule.getOperator(),
                            alertRule.getValue(), alertRule.getDuration(),
                            alertRule.getAlertSilentPeriod());
                    for (Long id : ids) {
                        AlertData alertData = new AlertData();
                        BeanUtil.copyProperties(deviceData,alertData);
                        //去alertRule中查询剩下的数据
                        alertData.setId(null);
                        alertData.setAlertReason(reason);
                        alertData.setAlertRuleId(alertRule.getId());
                        alertData.setType(alertRule.getAlertDataType());
                        alertData.setStatus(0);
                        alertData.setUserId(id);
                        alertDataList.add(alertData);
                    }
                    //批量保存
                    alertDataService.saveBatch(alertDataList);
                }



            }

        }

    }

    /**
     * 删除报警规则信息
     * 
     * @param id 报警规则主键
     * @return 结果
     */
    @Override
    public int deleteAlertRuleById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
