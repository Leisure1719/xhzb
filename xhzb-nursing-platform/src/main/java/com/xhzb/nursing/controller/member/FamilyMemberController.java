package com.xhzb.nursing.controller.member;

import java.util.List;

import com.xhzb.nursing.domain.dto.UserLoginRequestDto;
import com.xhzb.nursing.domain.vo.LoginVo;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.weaver.loadtime.Aj;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import com.xhzb.common.annotation.Log;
import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.xhzb.nursing.domain.FamilyMember;
import com.xhzb.nursing.service.member.IFamilyMemberService;
import com.xhzb.common.utils.poi.ExcelUtil;
import com.xhzb.common.core.page.TableDataInfo;

/**
 * 老人家属Controller
 * 
 * @author ruoyi
 * @date 2026-07-25
 */
@RestController
@RequestMapping("/member/user")
@Tag(name = "老人家属相关接口")
public class FamilyMemberController extends BaseController
{
    @Autowired
    private IFamilyMemberService familyMemberService;


    @PostMapping("/login")
    public AjaxResult login(@RequestBody UserLoginRequestDto userLoginRequestDto){
        LoginVo loginVo = familyMemberService.login(userLoginRequestDto);
        return success(loginVo);
    }


}
