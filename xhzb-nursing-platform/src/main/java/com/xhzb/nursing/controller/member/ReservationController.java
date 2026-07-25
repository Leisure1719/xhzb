package com.xhzb.nursing.controller.member;

import java.util.List;

import com.xhzb.common.utils.UserThreadLocal;
import jakarta.servlet.http.HttpServletResponse;
import org.aspectj.weaver.loadtime.Aj;
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
import com.xhzb.common.annotation.Log;
import com.xhzb.common.core.controller.BaseController;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.enums.BusinessType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import com.xhzb.nursing.domain.Reservation;
import com.xhzb.nursing.service.member.IReservationService;
import com.xhzb.common.utils.poi.ExcelUtil;
import com.xhzb.common.core.page.TableDataInfo;

/**
 * 预约信息Controller
 * 
 * @author ruoyi
 * @date 2026-07-25
 */
@RestController
@RequestMapping("/member/reservation")
@Tag(name = "预约信息相关接口")
public class ReservationController extends BaseController
{
    @Autowired
    private IReservationService reservationService;

    /**
     * 查询预约信息列表
     */
    @PreAuthorize("@ss.hasPermi('nursing:reservation:list')")
    @GetMapping("/page")
    @Operation(summary = "查询预约信息列表")
    public AjaxResult list(Reservation reservation)
    {
        startPage();
        List<Reservation> list = reservationService.selectReservationList(reservation);
        return success(getDataTable(list));
    }

    /**
     * 新增预约信息
     */
    @PreAuthorize("@ss.hasPermi('nursing:reservation:add')")
    @Log(title = "预约信息", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增预约信息")
    public AjaxResult add(@RequestBody Reservation reservation)
    {
        return toAjax(reservationService.insertReservation(reservation));
    }

    /**
     * 取消预约
     * @param id
     * @return
     */
    @PutMapping("/{id}/cancel")
    public AjaxResult cancel(@PathVariable Long id){
        reservationService.lambdaUpdate().eq(Reservation::getId, id)
                .set(Reservation::getStatus, 2)
                .update();
        return success();
    }

    @GetMapping("/cancelled-count")
    public AjaxResult getCancelledCount(){
        int count = reservationService.getCancelledCount();
        return success(count);
    }

    @GetMapping("/countByTime")
    public AjaxResult countByTime(){
        return success();
    }

}
