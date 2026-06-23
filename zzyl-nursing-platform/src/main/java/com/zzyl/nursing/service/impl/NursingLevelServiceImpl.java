package com.zzyl.nursing.service.impl;

import java.util.Arrays;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.vo.NursingLevelVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingLevelMapper;
import com.zzyl.nursing.domain.NursingLevel;
import com.zzyl.nursing.service.INursingLevelService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 护理等级Service业务层处理
 * 
 * @author alexis
 * @date 2025-06-02
 */
@Service
public class NursingLevelServiceImpl extends ServiceImpl<NursingLevelMapper, NursingLevel> implements INursingLevelService
{
    @Autowired
    private NursingLevelMapper nursingLevelMapper;
    @Autowired
    private RedisTemplate<Object,Object> redisTemplate;

    /**
     * 查询护理等级
     * 
     * @param id 护理等级主键
     * @return 护理等级
     */
    @Override
    public NursingLevel selectNursingLevelById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询护理等级列表
     * 
     * @param nursingLevel 护理等级
     * @return 护理等级
     */
    @Override
    public List<NursingLevel> selectNursingLevelList(NursingLevel nursingLevel)
    {
        return nursingLevelMapper.selectNursingLevelList(nursingLevel);
    }

    /**
     * 新增护理等级
     * 
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int insertNursingLevel(NursingLevel nursingLevel)
    {

        boolean save = save(nursingLevel);
        //清除缓存
        cacheFlush();
        return save ? 1 : 0;
    }

    private void cacheFlush() {
        redisTemplate.delete(CacheConstants.NURSING_LEVEL_ALL_KEY);
    }

    /**
     * 修改护理等级
     * 
     * @param nursingLevel 护理等级
     * @return 结果
     */
    @Override
    public int updateNursingLevel(NursingLevel nursingLevel)
    {

        boolean b = updateById(nursingLevel);
        cacheFlush();
        return b ? 1 : 0;
    }

    /**
     * 批量删除护理等级
     * 
     * @param ids 需要删除的护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelByIds(Long[] ids)
    {
        boolean b = removeByIds(Arrays.asList(ids));
        cacheFlush();
        return b ? 1 : 0;
    }

    /**
     * 删除护理等级信息
     * 
     * @param id 护理等级主键
     * @return 结果
     */
    @Override
    public int deleteNursingLevelById(Long id)
    {
        boolean b = removeById(id);
        //清除缓存
        cacheFlush();
        return b ? 1 : 0;
    }

    /**
     * 查询护理等级Vo列表
     *
     * @param nursingLevel 条件
     * @return 结果
     */
    @Override
    public List<NursingLevelVo> selectNursingLevelVoList(NursingLevel nursingLevel) {
        return nursingLevelMapper.selectNursingLevelVoList(nursingLevel);
    }

    @Override
    public List<NursingLevel> selectAll() {
        //判断在redis缓存中是否有
        List<NursingLevel> nursingLevels=(List<NursingLevel>)redisTemplate.opsForValue().get(CacheConstants.NURSING_LEVEL_ALL_KEY);
        if (ObjectUtil.isNotEmpty(nursingLevels)){
            return nursingLevels;
        }
        LambdaQueryWrapper<NursingLevel> lambdaQueryWrapper =new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(NursingLevel::getStatus,1);
        //缓存中没有，加入到缓存
        nursingLevels= list(lambdaQueryWrapper);
        redisTemplate.opsForValue().set(CacheConstants.NURSING_LEVEL_ALL_KEY,nursingLevels);
        return nursingLevels;
    }
}
