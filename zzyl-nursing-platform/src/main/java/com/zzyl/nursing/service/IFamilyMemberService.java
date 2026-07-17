package com.zzyl.nursing.service;

import java.util.List;
import com.zzyl.nursing.domain.FamilyMember;
import com.baomidou.mybatisplus.extension.service.IService;
import com.zzyl.nursing.dto.UserLoginRequestDto;
import com.zzyl.nursing.vo.LoginVo;

/**
 * 老人家属Service接口
 * 
 * @author ziyi
 * @date 2026-05-25
 */
public interface IFamilyMemberService extends IService<FamilyMember>
{

    /**
     * 登录
     * @param userLoginRequestDto 小程序登录参数
     * @return 登录信息
     */
    LoginVo login(UserLoginRequestDto userLoginRequestDto);
}
