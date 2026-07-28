package com.xhzb.nursing.controller;

import java.util.List;

import com.huaweicloud.sdk.iotda.v5.model.ShowDeviceRequest;
import com.huaweicloud.sdk.iotda.v5.model.ShowDeviceResponse;
import com.xhzb.nursing.domain.dto.DeviceDto;
import com.xhzb.nursing.domain.vo.DeviceDetailVo;
import com.xhzb.nursing.domain.vo.ProdoctVo;
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
import com.xhzb.nursing.domain.Device;
import com.xhzb.nursing.service.IDeviceService;
import com.xhzb.common.utils.poi.ExcelUtil;
import com.xhzb.common.core.page.TableDataInfo;


/**
 * 监测设备表Controller
 * 
 * @author ruoyi
 * @date 2026-07-27
 */
@RestController
@RequestMapping("/nursing/device")
@Tag(name = "监测设备表相关接口")
public class DeviceController extends BaseController
{
    @Autowired
    private IDeviceService deviceService;

    /**
     * 查询监测设备表列表
     */
    @PreAuthorize("@ss.hasPermi('nursing:device:list')")
    @GetMapping("/list")
    @Operation(summary = "查询监测设备表列表")
    public TableDataInfo list(Device device)
    {
        startPage();
        List<Device> list = deviceService.selectDeviceList(device);
        return getDataTable(list);
    }

    @Operation(summary = "从物联网平台同步产品列表")
    @PostMapping("/syncProductList")
    public AjaxResult syncProductList(){
        deviceService.getsyncProductList();
        return AjaxResult.success();
    }

    @GetMapping("/allProduct")
    public AjaxResult getAllProduct(){
        List<ProdoctVo> list = deviceService.getAllProduct();
        return success(list);
    }

    @PostMapping("/register")
    public AjaxResult register(@RequestBody DeviceDto dto) {
        deviceService.register(dto);
        return success();
    }

    @GetMapping("/{iotId}")
    public AjaxResult getDeviceByIotId(@PathVariable String iotId) {
        DeviceDetailVo deviceDetailVo = deviceService.getDeviceByIotId(iotId);
        return success(deviceDetailVo);
    }

    @GetMapping("/queryServiceProperties/{iotId}")
    public AjaxResult queryServiceProperties(@PathVariable String iotId){
        List list = deviceService.queryServiceProperties(iotId);
        return success(list);
    }


}
