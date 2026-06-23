package com.zzyl.nursing.controller.member;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zzyl.common.core.controller.BaseController;
import com.zzyl.common.core.domain.R;
import com.zzyl.common.core.page.TableDataInfo;
import com.zzyl.nursing.domain.NursingProject;
import com.zzyl.nursing.service.INursingProjectService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import kotlin.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * @Author: ziyi
 * @Date: 2026/5/25 22:10
 * @Version: v1.0.0
 * @Description: TODO
 **/
@RestController
@Api(tags = "会员项目接口")
@RequestMapping("/member/orders/project")
public class MemberProjectController extends BaseController {
    @Autowired
    INursingProjectService nursingProjectService;
    @GetMapping("/page")
    @ApiOperation("分页查询会员项目")
    public TableDataInfo<NursingProject> page(String name, Integer status, Integer pageNum, Integer pageSize) {
        IPage<NursingProject> page = new Page<>(pageNum,pageSize);

        return nursingProjectService.getProject4MemberByNameAndStatus(page,name,status);
    }
    @GetMapping("/{id}")
    @ApiOperation("查询会员项目")
    public R<NursingProject> getById(@PathVariable Integer id) {
        NursingProject nursingProject = nursingProjectService.selectNursingProjectById(id.longValue());
        return R.ok(nursingProject);
    }
}
