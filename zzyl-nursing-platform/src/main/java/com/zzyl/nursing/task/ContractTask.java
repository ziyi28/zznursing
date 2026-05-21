package com.zzyl.nursing.task;

import com.zzyl.nursing.service.IContractService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: ziyi
 * @Date: 2026/5/21 11:07
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Component
@Slf4j
public class ContractTask {
    @Autowired
    private IContractService contractService;
    public void execContract(){
        log.info("开始执行定时任务，修改已生效合同状态");
        contractService.updateContractStatus();
        log.info("修改成功");

    }
}
