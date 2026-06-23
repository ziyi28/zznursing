package com.zzyl.nursing.controller.member;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.AjaxResult;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.common.utils.thread.UserThreadLocal;
import com.zzyl.nursing.domain.Reservation;
import com.zzyl.nursing.service.IFamilyMemberService;
import com.zzyl.nursing.service.IReservationService;
import com.zzyl.nursing.vo.CountByTimeVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 预约信息Controller
 * 
 * @author ruoyi
 */
@RestController
@RequestMapping("/member/reservation")
@Api(tags =  "预约信息相关接口")
public class MemberReservationController extends BaseController

{
    @Autowired
    private IReservationService reservationService;
    @GetMapping("/cancelled-count")
    @ApiOperation("查询取消预约数量")
    public R<Integer> getCancelledReservationCount() {
        Long userId = UserThreadLocal.getUserId();
        Integer count =reservationService.getCancelByUserId(userId);

        return R.ok(count);
    }
    @GetMapping("/countByTime")
    public R<List<CountByTimeVo>> getCountByTime(Long time) {
        Long userId = UserThreadLocal.getUserId();
        List<CountByTimeVo> vo =reservationService.getCountByTime(time);
        return R.ok(vo);
    }
    @PostMapping
    public AjaxResult addReservation(@RequestBody Reservation reservation) {
        Long userId = UserThreadLocal.getUserId();
        reservation.setCreateBy(String.valueOf(userId));
        reservation.setUpdateBy(String.valueOf(userId));
        reservation.setStatus(0);
        reservationService.insertReservation( reservation);

        return AjaxResult.success();
    }
    @GetMapping("/page")
    public R<TableDataInfo<Reservation>> getReservationPage(@RequestParam(value = "pageNum", defaultValue = "1") Integer pageNum,
                                               @RequestParam(value = "pageSize", defaultValue = "10") Integer pageSize) {
        Long userId = UserThreadLocal.getUserId();
        Page<Reservation> page = new Page<>(pageNum,pageSize);
        TableDataInfo<Reservation> reservationTableDataInfo =reservationService.pageReservationDetail(page,userId);

        return R.ok(reservationTableDataInfo);
    }
    @PutMapping("{id}/cancel")
    public AjaxResult cancelReservation(@PathVariable Long id) {
        reservationService.updateReservationStatus(id);

        return AjaxResult.success();
    }

}