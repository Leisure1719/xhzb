package com.xhzb.nursing.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.xhzb.nursing.domain.Device;

/**
 * 监测设备表Mapper接口
 * 
 * @author ruoyi
 * @date 2026-07-27
 */
@Mapper
public interface DeviceMapper extends BaseMapper<Device>
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
     * 删除监测设备表
     * 
     * @param id 监测设备表主键
     * @return 结果
     */
    public int deleteDeviceById(Long id);

    /**
     * 批量删除监测设备表
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteDeviceByIds(Long[] ids);
}
