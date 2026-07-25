package com.xhzb.nursing.service.impl.member;

import java.util.*;

import com.xhzb.framework.web.service.TokenService;
import com.xhzb.nursing.domain.dto.UserLoginRequestDto;
import com.xhzb.nursing.domain.vo.LoginVo;
import com.xhzb.nursing.service.member.WechatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.member.FamilyMemberMapper;
import com.xhzb.nursing.domain.FamilyMember;
import com.xhzb.nursing.service.member.IFamilyMemberService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 老人家属Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-25
 */
@Service
public class FamilyMemberServiceImpl extends ServiceImpl<FamilyMemberMapper, FamilyMember> implements IFamilyMemberService {
    static List<String> DEFAULT_NICKNAME_PREFIX = Arrays.asList(
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

    @Autowired
    private FamilyMemberMapper familyMemberMapper;

    @Autowired
    private WechatService wechatService;
    @Autowired
    private TokenService tokenService;

    /**
     * 查询老人家属
     * 
     * @param id 老人家属主键
     * @return 老人家属
     */
    @Override
    public FamilyMember selectFamilyMemberById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询老人家属列表
     * 
     * @param familyMember 老人家属
     * @return 老人家属
     */
    @Override
    public List<FamilyMember> selectFamilyMemberList(FamilyMember familyMember)
    {
        return familyMemberMapper.selectFamilyMemberList(familyMember);
    }

    /**
     * 新增老人家属
     * 
     * @param familyMember 老人家属
     * @return 结果
     */
    @Override
    public int insertFamilyMember(FamilyMember familyMember)
    {
        return save(familyMember)? 1 : 0;
    }

    /**
     * 修改老人家属
     * 
     * @param familyMember 老人家属
     * @return 结果
     */
    @Override
    public int updateFamilyMember(FamilyMember familyMember)
    {
        return updateById(familyMember)? 1 : 0;
    }

    /**
     * 批量删除老人家属
     * 
     * @param ids 需要删除的老人家属主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public LoginVo login(UserLoginRequestDto userLoginRequestDto) {
        //调用微信接口获取openid和phone
        String openid = wechatService.getOpenid(userLoginRequestDto.getCode());
        String phone = wechatService.getPhone(userLoginRequestDto.getPhoneCode());

        //判断用户是否已经注册
        FamilyMember familyMember = lambdaQuery().eq(FamilyMember::getOpenId, openid).one();
        if(Objects.isNull(familyMember)){
            //随机字符串 + 手机号后4位生成昵称
            Collections.shuffle(DEFAULT_NICKNAME_PREFIX);
            String name = DEFAULT_NICKNAME_PREFIX.get(0) + phone.substring(phone.length() - 4);
            //新增
             familyMember = FamilyMember.builder()
                    .name(name)
                    .phone(phone)
                    .openId(openid)
                    .build();
             save(familyMember);
        }else if(!Objects.equals(familyMember.getPhone(),phone)){
            //更新手机号
            lambdaUpdate().eq(FamilyMember::getOpenId,openid)
                    .set(FamilyMember::getPhone,phone)
                    .update();
        }

        //生成token返回
        Map claims = new HashMap();
        claims.put("userId",familyMember.getId());
        claims.put("name",familyMember.getName());

        String token = tokenService.createToken(claims);

        LoginVo loginVo = new LoginVo();
        loginVo.setToken(token);
        loginVo.setNickName(familyMember.getName());
        return loginVo;
    }

    /**
     * 删除老人家属信息
     * 
     * @param id 老人家属主键
     * @return 结果
     */
    @Override
    public int deleteFamilyMemberById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
