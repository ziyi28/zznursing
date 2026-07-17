package com.zzyl.nursing.controller;

import java.util.List;
import javax.servlet.http.HttpServletResponse;

import com.zzyl.common.core.domain.R;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.zzyl.common.annotation.Log;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.enums.BusinessType;
import com.zzyl.nursing.domain.Reservation;
import com.zzyl.nursing.service.IReservationService;
import com.zzyl.common.utils.poi.ExcelUtil;
import com.zzyl.common.core.page.TableDataInfo;

/**
 * 预约信息Controller
 * 
 * @author ziyi
 * @date 2026-05-26
 */
@Api("预约信息管理")
@RestController
@RequestMapping("/nursing/reservation")
public class ReservationController extends BaseController
{
    @Autowired
    private IReservationService reservationService;

    /**
     * 查询预约信息列表
     */
    @ApiOperation("查询预约信息列表")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:list')")
    @GetMapping("/list")
    public TableDataInfo<List<Reservation>> list(@ApiParam("查询条件对象") Reservation reservation)
    {
        startPage();
        List<Reservation> list = reservationService.selectReservationList(reservation);
        return getDataTable(list);
    }

    /**
     * 导出预约信息列表
     */
    @ApiOperation("导出预约信息列表")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:export')")
    @Log(title = "预约信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(@ApiParam("导出的查询条件") HttpServletResponse response, Reservation reservation)
    {
        List<Reservation> list = reservationService.selectReservationList(reservation);
        ExcelUtil<Reservation> util = new ExcelUtil<Reservation>(Reservation.class);
        util.exportExcel(response, list, "预约信息数据");
    }

    /**
     * 获取预约信息详细信息
     */
    @ApiOperation("获取预约信息详细信息")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:query')")
    @GetMapping(value = "/{id}")
    public R<Reservation> getInfo(@PathVariable("id") @ApiParam("预约信息ID") Long id)
    {
        return R.ok(reservationService.selectReservationById(id));
    }

    /**
     * 新增预约信息
     */
    @ApiOperation("新增预约信息")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:add')")
    @Log(title = "预约信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody @ApiParam("新增的预约信息对象") Reservation reservation)
    {
        return toAjax(reservationService.insertReservation(reservation));
    }

    /**
     * 修改预约信息
     */
    @ApiOperation("修改预约信息")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:edit')")
    @Log(title = "预约信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody @ApiParam("修改的预约信息对象") Reservation reservation)
    {
        return toAjax(reservationService.updateReservation(reservation));
    }

    /**
     * 删除预约信息
     */
    @ApiOperation("删除预约信息")
    @PreAuthorize("@ss.hasPermi('nursing:reservation:remove')")
    @Log(title = "预约信息", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable @ApiParam("要删除的预约信息ID") Long[] ids)
    {
        return toAjax(reservationService.deleteReservationByIds(ids));
    }
}
