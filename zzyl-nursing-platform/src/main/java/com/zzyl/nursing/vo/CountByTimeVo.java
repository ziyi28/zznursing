package com.zzyl.nursing.vo;

import lombok.Data;

import java.time.LocalDateTime;

/**
 * @Author: ziyi
 * @Date: 2026/5/26 10:07
 * @Version: v1.0.0
 * @Description: TODO
 **/
@Data
public class CountByTimeVo {
    private LocalDateTime time;
    private Integer count;
}
