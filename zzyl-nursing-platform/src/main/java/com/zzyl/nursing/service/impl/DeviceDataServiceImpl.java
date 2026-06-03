package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.DateTimeZoneConverter;
import com.zzyl.common.constant.HttpStatus;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.dto.DeviceDataPageReqDto;
import com.zzyl.nursing.job.vo.IotMsgNotifyData;
import com.zzyl.nursing.mapper.DeviceMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.DeviceDataMapper;
import com.zzyl.nursing.domain.DeviceData;
import com.zzyl.nursing.service.IDeviceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 设备数据Service业务层处理
 * 
 * @author ziyi
 * @date 2026-06-03
 */
@Service
public class DeviceDataServiceImpl extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService
{
    @Autowired
    private DeviceDataMapper deviceDataMapper;
    @Autowired
    private DeviceMapper deviceMapper;

    /**
     * 查询设备数据
     * 
     * @param id 设备数据主键
     * @return 设备数据
     */
    @Override
    public DeviceData selectDeviceDataById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询设备数据列表
     * 
     * @param dto 设备数据
     * @return 设备数据
     */
    @Override
    public TableDataInfo selectDeviceDataList(DeviceDataPageReqDto dto)
    {
        Page<DeviceData> page = new Page<>(dto.getPageNum(), dto.getPageSize());
        LambdaQueryWrapper<DeviceData> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.like(StringUtils.isNotEmpty(dto.getDeviceName()),DeviceData::getDeviceName, dto.getDeviceName())
                .eq(StringUtils.isNotEmpty(dto.getFunctionId()),DeviceData::getFunctionId, dto.getFunctionId())
                .between(ObjectUtil.isNotEmpty(dto.getStartTime()) && ObjectUtil.isNotEmpty(dto.getEndTime()),
                        DeviceData::getAlarmTime, dto.getStartTime(),dto.getEndTime());
        Page<DeviceData> resultPage = this.page(page, queryWrapper);
        return getTableDataInfo(resultPage);
    }

    private TableDataInfo getTableDataInfo(Page<DeviceData> page) {
        TableDataInfo tableDataInfo = new TableDataInfo();
        tableDataInfo.setRows(page.getRecords());
        tableDataInfo.setTotal(page.getTotal());
        tableDataInfo.setCode(HttpStatus.SUCCESS);
        tableDataInfo.setMsg("请求成功");
        return tableDataInfo;
    }

    /**
     * 新增设备数据
     * 
     * @param deviceData 设备数据
     * @return 结果
     */
    @Override
    public int insertDeviceData(DeviceData deviceData)
    {
        return save(deviceData) ? 1 : 0;
    }

    /**
     * 修改设备数据
     * 
     * @param deviceData 设备数据
     * @return 结果
     */
    @Override
    public int updateDeviceData(DeviceData deviceData)
    {
        return updateById(deviceData) ? 1 : 0;
    }

    /**
     * 批量删除设备数据
     * 
     * @param ids 需要删除的设备数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除设备数据信息
     * 
     * @param id 设备数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    @Override
    public void batchInsertDeviceData(IotMsgNotifyData msgNotifyData) {
        //根据设备ID查询设备信息
        String deviceId = msgNotifyData.getHeader().getDeviceId();
        Device device = deviceMapper.selectOne(Wrappers.<Device>lambdaQuery().eq(Device::getIotId, deviceId));
        if (ObjectUtil.isEmpty(device)){
            log.error("设备不存在");
            return;
        }
        //构建设备数据
        List<DeviceData> deviceDataList = new ArrayList<>();
        msgNotifyData.getBody().getServices().forEach(service -> {
            if (ObjectUtil.isEmpty(service.getProperties())){
                return;
            }
            String time = service.getEventTime();
            LocalDateTime localDateTime = LocalDateTimeUtil.parse(time, "yyyyMMdd'T'HHmmss'Z'");
            LocalDateTime eventTime = DateTimeZoneConverter.utcToShanghai(localDateTime);
            service.getProperties().forEach((k,v)->{
                DeviceData deviceData = BeanUtil.toBean(device, DeviceData.class);
                deviceData.setId(null);
                deviceData.setFunctionId(k);
                deviceData.setDataValue(v+"");
                deviceData.setAlarmTime(eventTime);
                deviceDataList.add(deviceData);
            });

        });
        saveBatch(deviceDataList);



    }
}
