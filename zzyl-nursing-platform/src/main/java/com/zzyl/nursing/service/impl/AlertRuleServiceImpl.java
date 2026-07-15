package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.text.CharSequenceUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.nursing.config.WebSocketServer;
import com.zzyl.nursing.domain.AlertData;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.mapper.DeviceMapper;
import com.zzyl.nursing.service.IAlertDataService;
import com.zzyl.nursing.vo.AlertNotifyVo;
import com.zzyl.system.mapper.SysUserRoleMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.AlertRuleMapper;
import com.zzyl.nursing.domain.AlertRule;
import com.zzyl.nursing.service.IAlertRuleService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 报警规则Service业务层处理
 *
 * @author ziyi
 * @date 2026-06-09
 */
@Service
@Slf4j
public class AlertRuleServiceImpl extends ServiceImpl<AlertRuleMapper, AlertRule> implements IAlertRuleService {

    @Autowired
    private AlertRuleMapper alertRuleMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private SysUserRoleMapper sysUserRoleMapper;

    @Autowired
    private IAlertDataService alertDataService;

    @Value("${alert.deviceMaintainerRole}")
    private String deviceMaintainerRole;

    @Value("${alert.managerRole}")
    private String managerRole;

    /**
     * 查询报警规则
     *
     * @param id 报警规则主键
     * @return 报警规则
     */
    @Override
    public AlertRule selectAlertRuleById(Long id) {
        return getById(id);
    }

    /**
     * 查询报警规则列表
     *
     * @param alertRule 报警规则
     * @return 报警规则
     */
    @Override
    public List<AlertRule> selectAlertRuleList(AlertRule alertRule) {
        return alertRuleMapper.selectAlertRuleList(alertRule);
    }

    /**
     * 新增报警规则
     *
     * @param alertRule 报警规则
     * @return 结果
     */
    @Override
    public int insertAlertRule(AlertRule alertRule) {
        return save(alertRule) ? 1 : 0;
    }

    /**
     * 修改报警规则
     *
     * @param alertRule 报警规则
     * @return 结果
     */
    @Override
    public int updateAlertRule(AlertRule alertRule) {
        return updateById(alertRule) ? 1 : 0;
    }

    /**
     * 批量删除报警规则
     *
     * @param ids 需要删除的报警规则主键
     * @return 结果
     */
    @Override
    public int deleteAlertRuleByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除报警规则信息
     *
     * @param id 报警规则主键
     * @return 结果
     */
    @Override
    public int deleteAlertRuleById(Long id) {
        return removeById(id) ? 1 : 0;
    }

    /**
     * 报警过滤入口
     */
    @Override
    public void alertFilter() {
        // 查询所有启用规则，若无则退出
        long count = count(Wrappers.<AlertRule>lambdaQuery().eq(AlertRule::getStatus, 1));
        if (count <= 0) {
            return;
        }
        // 查询所有上报数据
        List<Object> values = redisTemplate.opsForHash().values(CacheConstants.IOT_DEVICE_LAST_DATA);
        if (CollUtil.isEmpty(values)) {
            return;
        }
        // 修复 Bug5：Redis 每个 value 存的是 DeviceData 列表，使用 toList 解析
        List<DeviceData> deviceDataList = new ArrayList<>();
        values.forEach(value -> deviceDataList.addAll(JSONUtil.toList(value.toString(), DeviceData.class)));

        // 遍历报警数据逐条处理
        deviceDataList.forEach(this::alertFilterData);
    }

    /**
     * 逐条过滤报警数据
     *
     * @param deviceData 设备数据
     */
    private void alertFilterData(DeviceData deviceData) {
        // 判断时效性，超过 1 分钟的数据不处理
        LocalDateTime alarmTime = deviceData.getAlarmTime();
        long between = LocalDateTimeUtil.between(alarmTime, LocalDateTime.now(), ChronoUnit.SECONDS);
        if (between > 60) {
            return;
        }
        // 全局规则（iotId = -1）
        List<AlertRule> globalAlertRule = list(Wrappers.<AlertRule>lambdaQuery()
                .eq(AlertRule::getProductKey, deviceData.getProductKey())
                .eq(AlertRule::getStatus, 1)
                .eq(AlertRule::getFunctionId, deviceData.getFunctionId())
                .eq(AlertRule::getIotId, "-1"));
        // 设备专属规则
        List<AlertRule> deviceAlertRule = list(Wrappers.<AlertRule>lambdaQuery()
                .eq(AlertRule::getProductKey, deviceData.getProductKey())
                .eq(AlertRule::getStatus, 1)
                .eq(AlertRule::getIotId, deviceData.getIotId())
                .eq(AlertRule::getFunctionId, deviceData.getFunctionId()));
        // 合并
        Collection<AlertRule> alertRules = CollUtil.addAll(globalAlertRule, deviceAlertRule);
        if (CollUtil.isEmpty(alertRules)) {
            return;
        }
        alertRules.forEach(alertRule -> deviceDataAlarmHandler(alertRule, deviceData));
    }

    /**
     * 判断设备数据是否触发报警规则
     *
     * @param alertRule  报警规则
     * @param deviceData 设备数据
     */
    private void deviceDataAlarmHandler(AlertRule alertRule, DeviceData deviceData) {
        // 判断上报时间是否在规则生效时段内 00:00:00~23:59:59
        String[] split = alertRule.getAlertEffectivePeriod().split("~");
        LocalTime start = LocalTime.parse(split[0]);
        LocalTime end = LocalTime.parse(split[1]);
        LocalTime alertTime = LocalDateTimeUtil.of(deviceData.getAlarmTime()).toLocalTime();
        if (alertTime.isBefore(start) || alertTime.isAfter(end)) {
            return;
        }

        String iotId = deviceData.getIotId();

        // 修复 Bug4：统计次数 key 格式与参考代码保持一致，iotId 与 functionId 之间加 ":"
        String alertCountKey = CacheConstants.ALERT_COUNT_KEY_PREFIX
                + iotId + ":" + deviceData.getFunctionId() + ":" + alertRule.getId();

        // 数据对比（左：上报值，右：规则阈值）
        int compare = NumberUtil.compare(Double.valueOf(deviceData.getDataValue()), alertRule.getValue());
        if ((">=".equals(alertRule.getOperator()) && compare >= 0)
                || ("<".equals(alertRule.getOperator()) && compare < 0)) {
            log.info("设备数据：{} 报警规则：{} 满足报警条件", deviceData, alertRule);
        } else {
            // 数据正常，清除计数
            redisTemplate.delete(alertCountKey);
            return;
        }

        // 判断是否在沉默周期内
        String alertSilentPeriodKey = CacheConstants.ALERT_SILENT_PREFIX
                + iotId + ":" + deviceData.getFunctionId() + ":" + alertRule.getId();
        String silent = redisTemplate.opsForValue().get(alertSilentPeriodKey);
        if (StringUtils.isNotEmpty(silent)) {
            return;
        }

        // 持续周期计数
        String data = redisTemplate.opsForValue().get(alertCountKey);
        int count = StringUtils.isEmpty(data) ? 1 : Integer.parseInt(data) + 1;
        if (ObjectUtil.notEqual(count, alertRule.getDuration())) {
            redisTemplate.opsForValue().set(alertCountKey, String.valueOf(count));
            return;
        }

        // 达到持续周期，触发报警
        redisTemplate.delete(alertCountKey);
        // 进入沉默周期
        redisTemplate.opsForValue().set(alertSilentPeriodKey, "1",
                alertRule.getAlertSilentPeriod(), TimeUnit.MINUTES);

        // 查找需要通知的人员
        List<Long> userIds = new ArrayList<>();
        if (alertRule.getAlertDataType().equals(0)) {
            // 老人异常数据
            if (deviceData.getLocationType().equals(0)) {
                // 随身设备（报警手表），通过 iotId 找护理员
                userIds = deviceMapper.selectNursingIdsByIotIdWithElder(iotId);
            } else if (deviceData.getLocationType().equals(1)
                    // 修复 Bug3：固定设备需同时满足 physicalLocationType == 2（床位）
                    && deviceData.getPhysicalLocationType().equals(2)) {
                // 床位设备，通过床位 iotId 找护理员
                userIds = deviceMapper.selectNursingIdsByIotIdWithBed(iotId);
            }
        } else {
            // 设备异常数据，通知维修人员
            userIds = sysUserRoleMapper.selectUserIdByRoleName(deviceMaintainerRole);
        }

        // 无论何种情况，均通知管理员
        List<Long> managerRoleUser = sysUserRoleMapper.selectUserIdByRoleName(managerRole);
        Collection<Long> allUserIds = CollUtil.addAll(userIds, managerRoleUser);
        allUserIds = CollUtil.distinct(allUserIds);

        List<AlertData> alertData = insertAlertData(allUserIds, alertRule, deviceData);
        webSocketNotity(alertData.get(0), alertRule, allUserIds);
    }

    @Autowired
    private WebSocketServer webSocketServer;
    private void webSocketNotity(AlertData alertData, AlertRule alertRule, Collection<Long> allIds) {

        log.info("allUserIds: {}", allIds);
        //属性拷贝
        AlertNotifyVo alertNotifyVo = BeanUtil.toBean(alertData, AlertNotifyVo.class);
        alertNotifyVo.setAccessLocation(alertData.getRemark());
        alertNotifyVo.setFunctionName(alertRule.getFunctionName());
        alertNotifyVo.setAlertDataType(alertRule.getAlertDataType());
        alertNotifyVo.setNotifyType(1);
        // 向指定的人推送消息
        webSocketServer.sendMessageToConsumer(alertNotifyVo, allIds);
    }

    /**
     * 保存报警数据
     *
     * @param allUserIds 需要通知的用户 ID 集合
     * @param alertRule  触发的报警规则
     * @param deviceData 设备上报数据
     */
    private List<AlertData> insertAlertData(Collection<Long> allUserIds, AlertRule alertRule, DeviceData deviceData) {
        AlertData alertData = BeanUtil.toBean(deviceData, AlertData.class);
        alertData.setAlertRuleId(alertRule.getId());
        // 修复 Bug2：使用 functionName（可读名称）而非 functionId
        String alertReason = CharSequenceUtil.format("{}{}{},持续{}个周期就报警",
                alertRule.getFunctionName(), alertRule.getOperator(),
                alertRule.getValue(), alertRule.getDuration());
        alertData.setAlertReason(alertReason);
        alertData.setStatus(0);
        // 修复 Bug1：使用 setType 而非 setLocationType
        alertData.setType(alertRule.getAlertDataType());

        List<AlertData> collect = allUserIds.stream().map(id -> {
            AlertData dbAlertData = BeanUtil.toBean(alertData, AlertData.class);
            dbAlertData.setUserId(id);
            dbAlertData.setId(null);
            return dbAlertData;
        }).collect(Collectors.toList());

        // 批量保存
        alertDataService.saveBatch(collect);
        return collect;
    }
}