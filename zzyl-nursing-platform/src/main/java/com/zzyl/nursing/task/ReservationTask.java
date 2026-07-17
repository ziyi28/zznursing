package com.zzyl.nursing.task;

import com.zzyl.nursing.service.IReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @Author: ziyi
 * @Date: 2026/5/26 11:14
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Component
@Slf4j
public class ReservationTask {
    @Autowired
    private IReservationService reservationService;
    public void execReservation(){
        log.info("开始执行定时任务，修改已预约状态");
        reservationService.updateReservationStatus();
        log.info("修改成功");

    }
}
