package com.xhzb.nursing.job;

import com.xhzb.nursing.service.IContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ContractJob {
    @Autowired
    private IContractService contractService;

    @Scheduled(cron = "0 0 0 * * ?")
    public void updateContractStautsJob(){
        log.info("更新合同状态..........");
        contractService.updateContract();
    }
}

