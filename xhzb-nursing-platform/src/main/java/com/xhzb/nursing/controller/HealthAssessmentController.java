package com.xhzb.nursing.controller;

import java.util.List;

import com.xhzb.nursing.domain.dto.ElderAssessmentDto;
import com.xhzb.nursing.service.IHealthAssessmentDataCollectionService;
import com.xhzb.nursing.service.IHealthAssessmentReportService;
import jakarta.servlet.http.HttpServletResponse;
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
import com.xhzb.nursing.domain.HealthAssessment;
import com.xhzb.nursing.service.IHealthAssessmentService;
import com.xhzb.common.utils.poi.ExcelUtil;
import com.xhzb.common.core.page.TableDataInfo;

/**
 * 健康评估记录Controller
 *
 * @author ruoyi
 * @date 2026-07-21
 */
@RestController
@RequestMapping("/nursing/healthAssessment")
@Tag(name = "健康评估记录相关接口")
public class HealthAssessmentController extends BaseController {
    @Autowired
    private IHealthAssessmentService healthAssessmentService;

    @Autowired
    private IHealthAssessmentReportService healthAssessmentReportService;

    @Autowired
    private IHealthAssessmentDataCollectionService healthAssessmentDataCollectionService;

    /**
     * 查询健康评估记录列表
     */
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:list')")
    @GetMapping("/list")
    @Operation(summary = "查询健康评估记录列表")
    public TableDataInfo list(HealthAssessment healthAssessment) {
        startPage();
        List<HealthAssessment> list = healthAssessmentService.selectHealthAssessmentList(healthAssessment);
        return getDataTable(list);
    }

    /**
     * 获取健康评估记录详细信息
     */
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:query')")
    @GetMapping(value = "/{id}")
    @Operation(summary = "获取健康评估记录详细信息")
    public AjaxResult getInfo(@Schema(name = "健康评估记录ID", requiredMode = Schema.RequiredMode.REQUIRED)
                              @PathVariable("id") Long id) {
        return success(healthAssessmentService.selectHealthAssessmentById(id));
    }

    /**
     * 新增健康评估记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:add')")
    @Log(title = "健康评估记录", businessType = BusinessType.INSERT)
    @PostMapping
    @Operation(summary = "新增健康评估记录")
    public AjaxResult add(@RequestBody ElderAssessmentDto dto) {
        return success(healthAssessmentService.insertHealthAssessment(dto));
    }

    /**
     * 修改健康评估记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:edit')")
    @Log(title = "健康评估记录", businessType = BusinessType.UPDATE)
    @PutMapping
    @Operation(summary = "修改健康评估记录")
    public AjaxResult edit(@RequestBody ElderAssessmentDto dto) {
        return success(healthAssessmentService.updateHealthAssessment(dto));
    }

    /**
     * 删除健康评估记录
     */
    @PreAuthorize("@ss.hasPermi('nursing:healthAssessment:remove')")
    @Log(title = "健康评估记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    @Operation(summary = "删除健康评估记录")
    public AjaxResult remove(@Schema(name = "健康评估记录ID", requiredMode = Schema.RequiredMode.REQUIRED) @PathVariable Long[] ids) {
        return toAjax(healthAssessmentService.deleteHealthAssessmentByIds(ids));
    }

    /**
     * AI健康评估
     *
     * @param dto
     * @return
     */
    @PostMapping("/assessmentData")
    public AjaxResult assessmentData(@RequestBody ElderAssessmentDto dto) {
        return success(healthAssessmentService.assessmentData(dto));
    }

    /**
     * 获取健康评估报告
     *
     * @param id
     * @return
     */
    @GetMapping("report/{id}")
    public AjaxResult report(@PathVariable Long id) {
        return success(healthAssessmentReportService.selectHealthAssessmentReportById(id));
    }

    /**
     * 取消健康评估
     *
     * @param id
     * @return
     */
    @PutMapping("/{id}")
    public AjaxResult cancel(@PathVariable Long id) {
        Long healthAssessmentId = healthAssessmentReportService.getById(id).getHealthAssessmentId();
        healthAssessmentService.lambdaUpdate().eq(HealthAssessment::getId, healthAssessmentId)
                .set(HealthAssessment::getEvaluationProgress, 2)
                .update();
        return success();
    }

    /**
     * 删除评估记录
     *
     * @param id
     * @return
     */
    @DeleteMapping("/{id}")
    public AjaxResult removeReport(@PathVariable Long id) {
        healthAssessmentService.removeReport(id);
        return success();
    }

    @GetMapping("/elder/{id}")
    public AjaxResult getElderInfo(@PathVariable Long id){
        return success(healthAssessmentService.getElderInfo(id));
    }
}
