package com.zzyl.nursing.service.impl;

import java.util.Arrays;
import java.util.List;

import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzyl.common.constant.CacheConstants;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.common.utils.StringUtils;
import com.zzyl.nursing.vo.NursingProjectVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.NursingProjectMapper;
import com.zzyl.nursing.domain.NursingProject;
import com.zzyl.nursing.service.INursingProjectService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 护理项目Service业务层处理
 * 
 * @author alexis
 * @date 2025-06-02
 */
@Service
public class NursingProjectServiceImpl extends ServiceImpl<NursingProjectMapper, NursingProject> implements INursingProjectService
{
    @Autowired
    private NursingProjectMapper nursingProjectMapper;
    @Autowired
    private RedisTemplate<Object, Object> redisTemplate;

    /**
     * 查询护理项目
     * 
     * @param id 护理项目主键
     * @return 护理项目
     */
    @Override
    public NursingProject selectNursingProjectById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询护理项目列表
     * 
     * @param nursingProject 护理项目
     * @return 护理项目
     */
    @Override
    public List<NursingProject> selectNursingProjectList(NursingProject nursingProject)
    {
        return nursingProjectMapper.selectNursingProjectList(nursingProject);
    }

    /**
     * 新增护理项目
     * 
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int insertNursingProject(NursingProject nursingProject)
    {
        boolean save = save(nursingProject);
        //清除缓存
        cacheFlush();
        return save ? 1 : 0;
    }

    /**
     * 修改护理项目
     * 
     * @param nursingProject 护理项目
     * @return 结果
     */
    @Override
    public int updateNursingProject(NursingProject nursingProject)
    {
        boolean b = updateById(nursingProject);
        //清除缓存
        cacheFlush();
        return b ? 1 : 0;
    }

    /**
     * 批量删除护理项目
     * 
     * @param ids 需要删除的护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectByIds(Long[] ids)
    {
        boolean b = removeByIds(Arrays.asList(ids));
        //清除缓存
        cacheFlush();
        return b ? 1 : 0;
    }

    /**
     * 删除护理项目信息
     * 
     * @param id 护理项目主键
     * @return 结果
     */
    @Override
    public int deleteNursingProjectById(Long id)
    {
        boolean b = removeById(id);
        //清除缓存
        cacheFlush();
        return b ? 1 : 0;
    }

    private void cacheFlush() {
        redisTemplate.delete(CacheConstants.NURSING_PROJECT_ALL_KEY);
    }

    /**
     * 查询所有护理项目
     *
     * @return 护理项目列表
     */
    @Override
    public List<NursingProjectVo> getAll() {
        //先判断缓存中有没有
        List<NursingProjectVo> all=(List<NursingProjectVo>)redisTemplate.opsForValue().get(CacheConstants.NURSING_PROJECT_ALL_KEY);
        if (ObjectUtil.isNotEmpty(all)){
            return all;
        }
        all= nursingProjectMapper.getAll();
        redisTemplate.opsForValue().set(CacheConstants.NURSING_PROJECT_ALL_KEY,all);
        return all;
    }

    @Override
    public TableDataInfo<NursingProject> getProject4MemberByNameAndStatus(IPage<NursingProject> page, String name, Integer status) {
        TableDataInfo<NursingProject> tableDataInfo = new TableDataInfo<>();
        LambdaQueryWrapper<NursingProject> queryWrapper = Wrappers.lambdaQuery();
        queryWrapper.like(StringUtils.isNotEmpty(name),NursingProject::getName,name)
                .eq(ObjectUtil.isNotEmpty(status),NursingProject::getStatus,status);
        page(page,queryWrapper);
        tableDataInfo.setRows(page.getRecords());
        tableDataInfo.setTotal(page.getTotal());
        tableDataInfo.setCode(200);
        tableDataInfo.setMsg("查询成功");
        return tableDataInfo;
    }
}
