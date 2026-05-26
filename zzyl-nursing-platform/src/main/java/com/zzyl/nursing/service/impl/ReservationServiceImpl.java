package com.zzyl.nursing.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.exception.base.BaseException;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.nursing.domain.Elder;
import com.zzyl.nursing.service.IElderService;
import com.zzyl.nursing.vo.CountByTimeVo;
import net.bytebuddy.asm.Advice;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.ReservationMapper;
import com.zzyl.nursing.domain.Reservation;
import com.zzyl.nursing.service.IReservationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 预约信息Service业务层处理
 * 
 * @author ziyi
 * @date 2026-05-26
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements IReservationService
{
    @Autowired
    private ReservationMapper reservationMapper;
    @Autowired
    private IElderService elderService;

    /**
     * 查询预约信息
     * 
     * @param id 预约信息主键
     * @return 预约信息
     */
    @Override
    public Reservation selectReservationById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询预约信息列表
     * 
     * @param reservation 预约信息
     * @return 预约信息
     */
    @Override
    public List<Reservation> selectReservationList(Reservation reservation)
    {
        return reservationMapper.selectReservationList(reservation);
    }

    /**
     * 新增预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    @Override
    public int insertReservation(Reservation reservation)
    {
        String visitor = reservation.getVisitor();
        Elder one = elderService.getOne(Wrappers.<Elder>lambdaQuery().eq(Elder::getName, visitor));
        if (ObjectUtil.isEmpty( one)){
            throw new BaseException("请输入正确的老人名称");
        }

        try {
            return save(reservation) ? 1 : 0;
        } catch (Exception e) {
            throw new BaseException("不能重复预约哦");
        }

    }

    /**
     * 修改预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    @Override
    public int updateReservation(Reservation reservation)
    {
        return updateById(reservation) ? 1 : 0;
    }

    /**
     * 批量删除预约信息
     * 
     * @param ids 需要删除的预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 删除预约信息信息
     * 
     * @param id 预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationById(Long id)
    {
        return removeById(id) ? 1 : 0;
    }

    @Override
    public Integer getCancelByUserId(Long userId) {

        LocalDateTime startTime = LocalDate.now().atStartOfDay();
        LocalDateTime endTime = startTime.plusDays(1);
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getUpdateBy, userId)
                .eq(Reservation::getStatus, 2)
                .between(Reservation::getUpdateTime, startTime, endTime);
        long count = count(queryWrapper);

        return (int) count;
    }

    @Override
    public List<CountByTimeVo> getCountByTime(Long time) {
        LocalDateTime localDateTime = LocalDateTimeUtil.of(time);
        LocalDateTime startOfDay = localDateTime.toLocalDate().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        List<CountByTimeVo> countByTimeVos = reservationMapper.getCountByTime(startOfDay, endOfDay);
        return countByTimeVos;
    }

    @Override
    public TableDataInfo<Reservation> pageReservationDetail(Page page, Long userId) {
        LambdaQueryWrapper<Reservation> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Reservation::getCreateBy, userId);
        Page<Reservation> reservationPage = page(page, queryWrapper);
        TableDataInfo<Reservation> tableDataInfo = new TableDataInfo<>(reservationPage.getRecords(), reservationPage.getTotal());

        return tableDataInfo;
    }

    @Override
    public void updateReservationStatus(Long id) {
        Reservation reservation = getById(id);
        if (reservation != null) {
            reservation.setStatus(2);
            updateById(reservation);
        }
    }

    @Override
    public void updateReservationStatus() {
        List<Reservation> reservations = reservationMapper.selectList(Wrappers.<Reservation>lambdaQuery()
                .lt(Reservation::getTime, LocalDateTime.now().minusMinutes(30))
                .eq(Reservation::getStatus, 0));
        reservations.forEach(reservation -> reservation.setStatus(3));
        updateBatchById(reservations);
    }
}
