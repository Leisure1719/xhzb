package com.xhzb.nursing.job;

import com.xhzb.nursing.service.IAlertRuleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 设备数据报警规则校验定时任务
 *
 * @Author mr.wu
 * @Date 2025-5-29 11:28
 */
@Slf4j
@Component
public class AlertRuleJob {

    @Autowired
    private IAlertRuleService alertRuleService;

    /**
     * 定时进行设备数据对应的报警规则校验，将报警数据保存到对应表中
     */
    public void alertRuleFilter(){
        alertRuleService.alertRuleFilter();
        log.info("设备数据报警规则校验定时任务，执行完毕");
    }
}