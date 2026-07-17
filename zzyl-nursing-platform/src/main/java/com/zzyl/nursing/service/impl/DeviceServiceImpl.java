package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.util.*;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson2.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.*;
import com.zzyl.common.DateTimeZoneConverter;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.dto.DeviceDto;
import com.zzyl.nursing.vo.DeviceDetailVo;
import com.zzyl.nursing.vo.ProductVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.DeviceMapper;
import com.zzyl.nursing.domain.Device;
import com.zzyl.nursing.service.IDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * 【请填写功能名称】Service业务层处理
 * 
 * @author ziyi
 * @date 2026-06-02
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService
{
    @Autowired
    private DeviceMapper deviceMapper;
    @Autowired
    private RedisTemplate<String,String> redisTemplate;
    @Autowired
    private IoTDAClient iotdAClient;

    /**
     * 查询【请填写功能名称】
     * 
     * @param id 【请填写功能名称】主键
     * @return 【请填写功能名称】
     */
    @Override
    public Device selectDeviceById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询【请填写功能名称】列表
     * 
     * @param device 【请填写功能名称】
     * @return 【请填写功能名称】
     */
    @Override
    public List<Device> selectDeviceList(Device device)
    {
        return deviceMapper.selectDeviceList(device);
    }

    /**
     * 新增【请填写功能名称】
     * 
     * @param device 【请填写功能名称】
     * @return 结果
     */
    @Override
    public int insertDevice(Device device)
    {
        return save(device) ? 1 : 0;
    }

    /**
     * 修改【请填写功能名称】
     * 
     *
     * @return 结果
     */
    @Override
    public void updateDevice(DeviceDto deviceDto)
    {
        UpdateDeviceRequest request = new UpdateDeviceRequest();
        request.withDeviceId(deviceDto.getIotId());
        UpdateDevice body = new UpdateDevice();
        body.withDeviceName(deviceDto.getDeviceName());
        request.withBody(body);

        UpdateDeviceResponse response = iotdAClient.updateDevice(request);
        if (response.getHttpStatusCode()!=200){
            throw new BaseException("物联网接口，修改设备接口，调用失败");
        }
        //本地修改
        Device device = BeanUtil.toBean(deviceDto, Device.class);
        try {
            updateById(device);
        } catch (Exception e) {
            throw new BaseException("该老人/位置已经绑定了该类型设备，请重新选择绑定位置");
        }
    }

    /**
     * 批量删除【请填写功能名称】
     * 
     * @param ids 需要删除的【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteDeviceByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除【请填写功能名称】信息
     * 
     * @param id 【请填写功能名称】主键
     * @return 结果
     */
    @Override
    public int deleteDeviceById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    /**
     * 同步产品列表
     */
    @Override
    public void syncProductList() {
        //设置请求参数
        ListProductsRequest request = new ListProductsRequest();
        request.setLimit(50);
        ListProductsResponse response = iotdAClient.listProducts(request);
        //判断是否成功
        if (response.getHttpStatusCode() != 200) {
            throw new BaseException("物联网接口 - 查询产品，同步失败");
        }
        //存入redis
        redisTemplate.opsForValue().set(CacheConstants.IOT_ALL_PRODUCT_LIST, JSON.toJSONString(response.getProducts()));

    }

    /**
     * 获取所有产品
     * @return
     */
    @Override
    public List<ProductVo> allProduct() {
        String json = redisTemplate.opsForValue().get(CacheConstants.IOT_ALL_PRODUCT_LIST);
        if (ObjectUtil.isEmpty(json)) {
            return Collections.emptyList();
        }
        return JSONUtil.toList(json, ProductVo.class);
    }

    /**
     * 注册设备
     * @param deviceDto
     */
    @Override
    public void registerDevice(DeviceDto deviceDto) {
        //设备名称是否重复
        long count = count(Wrappers.<Device>lambdaQuery().eq(Device::getDeviceName, deviceDto.getDeviceName()));
        if (count > 0){
            throw new BaseException("设备名称已存在，请重新输入");
        }
        //设备标识码是否重复
        count = count(Wrappers.<Device>lambdaQuery().eq(Device::getNodeId, deviceDto.getNodeId()));
        if (count > 0){
            throw new BaseException("设备标识码已存在，请重新输入");
        }
        if(deviceDto.getLocationType()==0){
            deviceDto.setPhysicalLocationType(-1);
        }
        //同一位置是否绑定了相同的产品
        count = count(Wrappers.<Device>lambdaQuery().eq(Device::getProductKey, deviceDto.getProductKey())
                .eq(Device::getBindingLocation, deviceDto.getBindingLocation())
                .eq(Device::getLocationType, deviceDto.getLocationType())
                .eq(Device::getPhysicalLocationType, deviceDto.getPhysicalLocationType())
        );
        if (count > 0){
            throw new BaseException("该老人/位置已绑定该产品，请重新选择");
        }
        //注册设备
        AddDeviceRequest request = new AddDeviceRequest();
        AddDevice body = new AddDevice();
        body.withProductId(deviceDto.getProductKey());
        body.withNodeId(deviceDto.getNodeId());
        body.withDeviceName(deviceDto.getDeviceName());
        //密钥配置
        AuthInfo authInfo = new AuthInfo();
        String secret = UUID.randomUUID().toString().replaceAll("-", "");
        authInfo.withSecret(secret);
        body.setAuthInfo(authInfo);
        request.setBody(body);
        AddDeviceResponse response;
        try {
            response = iotdAClient.addDevice(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw new BaseException("物联网接口 - 注册设备，调用失败");
        }
        //本地保存设备信息
        Device device = BeanUtil.toBean(deviceDto, Device.class);
        device.setSecret( secret);
        device.setIotId(response.getDeviceId());
        save(device);
    }

    @Override
    public DeviceDetailVo queryDeviceDetail(String iotId) {
        //查询本地数据
        Device device = getOne(Wrappers.<Device>lambdaQuery().eq(Device::getIotId, iotId));
        if (BeanUtil.isEmpty(device)){
            return null;
        }

        //查询物联网数据
        ShowDeviceRequest request = new ShowDeviceRequest();
        request.setDeviceId(iotId);
        ShowDeviceResponse response;
        try {
             response = iotdAClient.showDevice(request);
        } catch (Exception e) {
            throw new BaseException("物联网接口 - 查询设备详情，调用失败");
        }
        DeviceDetailVo deviceDetailVo = BeanUtil.toBean(device, DeviceDetailVo.class);
        deviceDetailVo.setDeviceStatus(response.getStatus());
        String activeTime = response.getActiveTime();
        if (ObjectUtil.isNotEmpty(activeTime)){
            //时间转换
            LocalDateTime localDateTime = LocalDateTimeUtil.parse(activeTime, DatePattern.UTC_MS_PATTERN);
            //校正时区
            LocalDateTime activeDateTime = DateTimeZoneConverter.utcToShanghai(localDateTime);
            deviceDetailVo.setActiveTime(activeDateTime);
        }


        return deviceDetailVo;
    }

    @Override
    public AjaxResult queryServiceProperties(String iotId) {
        ShowDeviceShadowRequest request = new ShowDeviceShadowRequest();
        request.setDeviceId(iotId);
        ShowDeviceShadowResponse response = iotdAClient.showDeviceShadow(request);
        if(response.getHttpStatusCode() != 200){
            throw new BaseException("物联网接口 - 查询设备影子，调用失败");
        }
        List<DeviceShadowData> shadow = response.getShadow();
        if (ObjectUtil.isEmpty(shadow)){
            return AjaxResult.success(Collections.emptyList());
        }
        DeviceShadowProperties reported = shadow.get(0).getReported();
        JSONObject obj = JSONUtil.parseObj(reported.getProperties());
        List<Map<String,Object>> list= new ArrayList<>();
        String eventTime = reported.getEventTime();
        LocalDateTime localDateTime = LocalDateTimeUtil.parse(eventTime, "yyyyMMdd'T'HHmmss'Z'");
        LocalDateTime eventDateTime = DateTimeZoneConverter.utcToShanghai(localDateTime);
        obj.forEach((k,v)->{
            Map<String,Object> map = new HashMap<>();
            map.put("functionId",k);
            map.put("value",v);
            map.put("eventTime",eventDateTime);
            list.add(map);
        });


        return AjaxResult.success(list);
    }

    @Override
    public void removeDeviceById(String iotId) {
        DeleteDeviceRequest request = new DeleteDeviceRequest();
        request.setDeviceId(iotId);
        try {
            iotdAClient.deleteDevice(request);
        } catch (Exception e) {
            throw new BaseException("物联网接口 - 删除设备，调用失败");
        }

        remove(Wrappers.<Device>lambdaQuery().eq(Device::getIotId,iotId));
    }

    @Override
    public AjaxResult queryProduct(String productKey) {
        //1.参数校验
        if (ObjectUtil.isEmpty(productKey)){
            throw new BaseException("产品标识码不能为空");
        }
        ShowProductRequest request=new ShowProductRequest();
        request.setProductId(productKey);
        ShowProductResponse response;
        try {
             response = iotdAClient.showProduct(request);
        } catch (Exception e) {
            throw new BaseException("物联网接口 - 查询产品，调用失败");
        }
        List<ServiceCapability> serviceCapabilities = response.getServiceCapabilities();
        if (CollUtil.isEmpty(serviceCapabilities)){
            return AjaxResult.success(Collections.emptyList());
        }
        return AjaxResult.success(serviceCapabilities);
    }
}
