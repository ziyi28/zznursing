package com.zzyl.nursing.service;

import java.util.List;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.Reservation;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.vo.CountByTimeVo;

/**
 * 预约信息Service接口
 * 
 * @author ziyi
 * @date 2026-05-26
 */
public interface IReservationService extends IService<Reservation>
{
    /**
     * 查询预约信息
     * 
     * @param id 预约信息主键
     * @return 预约信息
     */
    public Reservation selectReservationById(Long id);

    /**
     * 查询预约信息列表
     * 
     * @param reservation 预约信息
     * @return 预约信息集合
     */
    public List<Reservation> selectReservationList(Reservation reservation);

    /**
     * 新增预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    public int insertReservation(Reservation reservation);

    /**
     * 修改预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    public int updateReservation(Reservation reservation);

    /**
     * 批量删除预约信息
     * 
     * @param ids 需要删除的预约信息主键集合
     * @return 结果
     */
    public int deleteReservationByIds(Long[] ids);

    /**
     * 删除预约信息信息
     * 
     * @param id 预约信息主键
     * @return 结果
     */
    public int deleteReservationById(Long id);
    /**
     * 查询用户取消预约数量
     * @param userId
     * @return
     */
    Integer getCancelByUserId(Long userId);

    /**
     * 查询用户剩余预约数量
     * @return
     */
    List<CountByTimeVo> getCountByTime(Long time);

    /**
     * 分页查询用户预约详情
     * @param userId
     * @return
     */
    TableDataInfo<Reservation> pageReservationDetail(Page page, Long userId);
    /**
     * 取消预约
     * @param id
     */
    void updateReservationStatus(Long id);
    /**
     * 修改预约信息状态
     *
     */
    void updateReservationStatus();
}
