package com.xhzb.nursing.service;

import java.util.List;

import com.huaweicloud.sdk.iotda.v5.model.ServiceCapability;
import com.xhzb.nursing.domain.Device;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhzb.nursing.domain.dto.DeviceDto;
import com.xhzb.nursing.domain.vo.DeviceDetailVo;
import com.xhzb.nursing.domain.vo.ProdoctVo;

/**
 * 监测设备表Service接口
 * 
 * @author ruoyi
 * @date 2026-07-27
 */
public interface IDeviceService extends IService<Device>
{
    /**
     * 查询监测设备表
     * 
     * @param id 监测设备表主键
     * @return 监测设备表
     */
    public Device selectDeviceById(Long id);

    /**
     * 查询监测设备表列表
     * 
     * @param device 监测设备表
     * @return 监测设备表集合
     */
    public List<Device> selectDeviceList(Device device);

    /**
     * 新增监测设备表
     * 
     * @param device 监测设备表
     * @return 结果
     */
    public int insertDevice(Device device);

    /**
     * 修改监测设备表
     * 
     * @param device 监测设备表
     * @return 结果
     */
    public int updateDevice(Device device);

    /**
     * 批量删除监测设备表
     * 
     * @param ids 需要删除的监测设备表主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);

    /**
     * 删除监测设备表信息
     * 
     * @param id 监测设备表主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    void getsyncProductList();

    List<ProdoctVo> getAllProduct();

    void register(DeviceDto dto);

    DeviceDetailVo getDeviceByIotId(String iotId);

    List queryServiceProperties(String iotId);

    List<ServiceCapability> queryProduct(String productKey);
}
