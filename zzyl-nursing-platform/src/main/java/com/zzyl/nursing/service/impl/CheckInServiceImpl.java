package com.zzyl.nursing.service.impl;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.CodeGenerator;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.domain.*;
import com.zzyl.nursing.dto.CheckInApplyDto;
import com.zzyl.nursing.dto.CheckInConfigDto;
import com.zzyl.nursing.dto.CheckInElderDto;
import com.zzyl.nursing.mapper.*;
import com.zzyl.nursing.vo.CheckInConfigVo;
import com.zzyl.nursing.vo.CheckInDetailVo;
import com.zzyl.nursing.vo.CheckInElderVo;
import com.zzyl.nursing.vo.ElderFamilyVo;
import kotlin.jvm.internal.Lambda;
import org.apache.commons.lang3.ObjectUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.service.ICheckInService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;

/**
 * 入住Service业务层处理
 *
 * @author ziyi
 * @date 2026-05-19
 */
@Service
public class CheckInServiceImpl extends ServiceImpl<CheckInMapper, CheckIn> implements ICheckInService {
    @Autowired
    private CheckInMapper checkInMapper;

    @Autowired
    private ElderMapper elderMapper;

    @Autowired
    private BedMapper bedMapper;

    @Autowired
    private CheckInConfigMapper checkInConfigMapper;

    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询入住
     *
     * @param id 入住主键
     * @return 入住
     */
    @Override
    public CheckIn selectCheckInById(Long id) {
        return getById(id);
    }

    /**
     * 查询入住列表
     *
     * @param checkIn 入住
     * @return 入住
     */
    @Override
    public List<CheckIn> selectCheckInList(CheckIn checkIn) {
        return checkInMapper.selectCheckInList(checkIn);
    }

    /**
     * 新增入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int insertCheckIn(CheckIn checkIn) {
        return save(checkIn) ? 1 : 0;
    }

    /**
     * 修改入住
     *
     * @param checkIn 入住
     * @return 结果
     */
    @Override
    public int updateCheckIn(CheckIn checkIn) {
        return updateById(checkIn) ? 1 : 0;
    }

    /**
     * 批量删除入住
     *
     * @param ids 需要删除的入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除入住信息
     *
     * @param id 入住主键
     * @return 结果
     */
    @Override
    public int deleteCheckInById(Long id) {
        return removeById(id) ? 1 : 0;
    }

    /**
     *
     * 申请入住
     *
     * @param dto
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void apply(CheckInApplyDto dto) {
        // 1.判断老人是否入住，已入住抛出异常
        CheckInElderDto checkInElderDto = dto.getCheckInElderDto();
        LambdaQueryWrapper<Elder> elderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        elderLambdaQueryWrapper.eq(Elder::getIdCardNo, checkInElderDto.getIdCardNo())
                .in(Elder::getStatus, 1, 4);
        Elder elder = elderMapper.selectOne(elderLambdaQueryWrapper);
        if (ObjectUtil.isNotEmpty(elder)) {
            throw new BaseException("该老人已入住，请勿重复入住");
        }
        // 2.更新床位状态为已入住
        Long bedId = dto.getCheckInConfigDto().getBedId();
        Bed bed = bedMapper.selectBedById(bedId);
        bed.setBedStatus(1);
        bedMapper.updateById(bed);
        // 3.新增或更新老人
        elder = insertOrUpdate(bed, dto.getCheckInElderDto());
        // 4.新增签约办理
        //生成编号
        String contractNumber = "HT"+CodeGenerator.generateContractNumber();

        insertContract(elder, contractNumber, dto);
        // 5.新增入住信息
        CheckIn checkIn = insertCheckInInfo(elder, bed.getBedNumber(), dto);
        // 6.新增入住配置
        insertCheckInConfig(checkIn,dto.getCheckInConfigDto());

    }

    @Override
    public CheckInDetailVo getChekInDetail(Long id) {
        //1.查询入住信息
        CheckIn checkIn = getById(id);
        //2.查询老人信息
        Elder elder = elderMapper.selectById(checkIn.getElderId());
        //3.查询合同
        LambdaQueryWrapper<Contract> contractLambdaQueryWrapper = new LambdaQueryWrapper<>();
        contractLambdaQueryWrapper.eq(Contract::getElderId,elder.getId());
        Contract contract = contractMapper.selectOne(contractLambdaQueryWrapper);
        //4.查询入住配置
        LambdaQueryWrapper<CheckInConfig> configLambdaQueryWrapper = new LambdaQueryWrapper<>();
        configLambdaQueryWrapper.eq(CheckInConfig::getCheckInId,id);
        CheckInConfig checkInConfig = checkInConfigMapper.selectOne(configLambdaQueryWrapper);
        //5.转换家属数据
        String remark = checkIn.getRemark();
        List<ElderFamilyVo> elderFamilyVos = JSON.parseArray(remark, ElderFamilyVo.class);
        //6.整合返回
        CheckInDetailVo checkInDetailVo = new CheckInDetailVo();
        CheckInConfigVo checkInConfigVo = new CheckInConfigVo();
        CheckInElderVo checkInElderVo = new CheckInElderVo();
        BeanUtils.copyProperties(elder,checkInElderVo);
        BeanUtils.copyProperties(checkInConfig,checkInConfigVo);
        checkInConfigVo.setStartDate(checkIn.getStartDate());
        checkInConfigVo.setEndDate(checkIn.getEndDate());
        checkInConfigVo.setBedNumber(elder.getBedNumber());
        checkInDetailVo.setCheckInConfigVo(checkInConfigVo);
        checkInDetailVo.setCheckInElderVo(checkInElderVo);
        checkInDetailVo.setContract(contract);
        checkInDetailVo.setElderFamilyVoList(elderFamilyVos);
        return checkInDetailVo;
    }

    /**
     *
     *新增入住配置
     * @param checkIn
     * @param checkInConfigDto
     */
    private void insertCheckInConfig(CheckIn checkIn, CheckInConfigDto checkInConfigDto) {
        CheckInConfig checkInConfig = new CheckInConfig();
        BeanUtils.copyProperties(checkInConfigDto,checkInConfig);
        checkInConfig.setCheckInId(checkIn.getId());
        checkInConfigMapper.insert(checkInConfig);

    }

    /**
     *
     *新增入住信息
     * @param elder
     * @param bedNumber
     * @param dto
     */
    private CheckIn insertCheckInInfo(Elder elder, String bedNumber, CheckInApplyDto dto) {
        CheckIn checkIn = new CheckIn();
        CheckInConfigDto checkInConfigDto = dto.getCheckInConfigDto();
        BeanUtils.copyProperties(checkInConfigDto,checkIn);
        checkIn.setElderId(elder.getId());
        checkIn.setIdCardNo(elder.getIdCardNo());
        checkIn.setElderName(elder.getName());
        checkIn.setBedNumber(bedNumber);
        checkIn.setStatus(0);
        checkIn.setRemark(JSON.toJSONString(dto.getElderFamilyDtoList()));
        save(checkIn);
        return checkIn;

    }

    /**
     *
     *新增签约办理
     * @param elder
     * @param contractNumber
     * @param dto
     */
    private Contract insertContract(Elder elder, String contractNumber, CheckInApplyDto dto) {
        Contract contract = new Contract();
        contract.setContractNumber(contractNumber);
        BeanUtils.copyProperties(dto.getCheckInContractDto(),contract);
        contract.setElderId(elder.getId());
        contract.setElderName(elder.getName());
        LocalDateTime startDate = dto.getCheckInConfigDto().getStartDate();
        LocalDateTime endDate = dto.getCheckInConfigDto().getEndDate();
        contract.setStartDate(startDate);
        contract.setEndDate(endDate);
        if (startDate.isAfter(LocalDateTime.now())) {
            contract.setStatus(1);
        }else {
            contract.setStatus(0);
        }
        contractMapper.insert(contract);
        return contract;
    }

    /**
     *新增或更新老人
     *
     * @param bed
     * @param checkInElderDto
     */
    private Elder insertOrUpdate(Bed bed, CheckInElderDto checkInElderDto) {
        Elder elder = new Elder();
        BeanUtils.copyProperties(checkInElderDto,elder);
        elder.setBedId(bed.getId());
        elder.setBedNumber(bed.getBedNumber());
        elder.setStatus(1);
        LambdaQueryWrapper<Elder> elderLambdaQueryWrapper = new LambdaQueryWrapper<>();
        elderLambdaQueryWrapper.eq(Elder::getIdCardNo,checkInElderDto.getIdCardNo());
        Elder elderByIdCardNu = elderMapper.selectOne(elderLambdaQueryWrapper);
        if (ObjectUtils.isEmpty(elderByIdCardNu)){
            elderMapper.insert(elder);
        }
        else {
            elder.setId(elderByIdCardNu.getId());
            elderMapper.updateById(elder);
        }
        return elder;

    }
}
