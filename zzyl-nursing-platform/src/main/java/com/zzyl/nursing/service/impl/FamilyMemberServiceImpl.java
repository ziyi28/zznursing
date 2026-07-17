package com.zzyl.nursing.service.impl;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cn.hutool.core.collection.ListUtil;
import cn.hutool.core.util.ObjectUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.zzyl.common.core.domain.model.LoginUser;
import com.zzyl.common.utils.DateUtils;
import com.zzyl.framework.web.service.TokenService;
import com.zzyl.nursing.dto.UserLoginRequestDto;
import com.zzyl.nursing.service.WechatService;
import com.zzyl.nursing.vo.LoginVo;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.zzyl.nursing.mapper.FamilyMemberMapper;
import com.zzyl.nursing.domain.FamilyMember;
import com.zzyl.nursing.service.IFamilyMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 老人家属Service业务层处理
 * 
 * @author ziyi
 * @date 2026-05-25
 */
@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements IFamilyMemberService
{
    @Autowired
    private WechatService wechatService;

    @Autowired
    private TokenService tokenService;

    static List<String> DEFAULT_NICKNAME_PREFIX = ListUtil.of(
            "生活更美好",
            "大桔大利",
            "日富一日",
            "好柿开花",
            "柿柿如意",
            "一椰暴富",
            "大柚所为",
            "杨梅吐气",
            "天生荔枝"
    );



    @Override
    public LoginVo login(UserLoginRequestDto userLoginRequestDto) {
        //1.获取openid
        String openid = wechatService.getOpenid(userLoginRequestDto.getCode());
        //2.根据openid查询
        FamilyMember familyMember = getOne(Wrappers.<FamilyMember>lambdaQuery().eq(FamilyMember::getOpenId, openid));
        //3.判断是否为空，为空则赋值
        if (ObjectUtil.isEmpty(familyMember)){
            familyMember=FamilyMember.builder().openId(openid).build();
        }
        //4.获取手机号
        String phone = wechatService.getPhone(userLoginRequestDto.getPhoneCode());
        //5.保存或更新
        saveOrUpdateByPhone(phone, familyMember);
        //6.生成jwt令牌 json web token
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", familyMember.getId());
        claims.put("nickName", familyMember.getName());
        String token = tokenService.createToken(claims);
        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setNickName(familyMember.getName());
        return loginVo;
    }

    private void saveOrUpdateByPhone(String phone, FamilyMember familyMember) {
        if (ObjectUtil.notEqual(phone, familyMember.getPhone())){
            familyMember.setPhone(phone);
        }
        if (ObjectUtil.isEmpty(familyMember.getId())){
            String nickName = DEFAULT_NICKNAME_PREFIX.get((int) (Math.random() * DEFAULT_NICKNAME_PREFIX.size()))
                    + StringUtils.substring(familyMember.getPhone(), 7);
            familyMember.setName(nickName);
            save(familyMember);
        }else {
            updateById(familyMember);
        }
    }

}
