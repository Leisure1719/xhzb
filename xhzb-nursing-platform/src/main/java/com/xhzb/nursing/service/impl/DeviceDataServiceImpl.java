package com.xhzb.nursing.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONUtil;
import com.xhzb.common.utils.DateUtils;
import com.xhzb.nursing.constant.RedisKeyConstant;
import com.xhzb.nursing.domain.Device;
import com.xhzb.nursing.domain.dto.IotMsgNotifyData;
import com.xhzb.nursing.domain.dto.IotMsgService;
import com.xhzb.nursing.service.IDeviceService;
import com.xhzb.nursing.util.DateTimeZoneConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.format.DateTimeFormatters;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.DeviceDataMapper;
import com.xhzb.nursing.domain.DeviceData;
import com.xhzb.nursing.service.IDeviceDataService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;
import java.util.Map;

/**
 * 设备数据Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-28
 */
@Service
public class DeviceDataServiceImpl extends ServiceImpl<DeviceDataMapper, DeviceData> implements IDeviceDataService
{
    @Autowired
    private DeviceDataMapper deviceDataMapper;

    @Autowired
    private IDeviceService deviceService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 查询设备数据
     * 
     * @param id 设备数据主键
     * @return 设备数据
     */
    @Override
    public DeviceData selectDeviceDataById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询设备数据列表
     * 
     * @param deviceData 设备数据
     * @return 设备数据
     */
    @Override
    public List<DeviceData> selectDeviceDataList(DeviceData deviceData)
    {
        return deviceDataMapper.selectDeviceDataList(deviceData);
    }

    @Override
    public void batchInsertDeviceData(IotMsgNotifyData iotMsgNotifyData) {
        //将Device表中已经有的数据封装
        String deviceId = iotMsgNotifyData.getHeader().getDeviceId();
        Device device = deviceService.lambdaQuery().eq(Device::getIotId, deviceId).one();
        if(ObjectUtil.isEmpty(device)){
            return;
        }
        //将iot平台返回的event_time和物模型数据封装
        IotMsgService services = iotMsgNotifyData.getBody().getServices().get(0);
        //时间格式化
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        LocalDateTime parse = LocalDateTime.parse(services.getEventTime(), dtf);
        LocalDateTime time = DateTimeZoneConverter.utcToShanghai(parse);
        //获取物模型数据
        ArrayList<DeviceData> list = new ArrayList<>();
        Map<String, Object> properties = services.getProperties();
        properties.forEach((k,v) ->{
            DeviceData bean = BeanUtil.toBean(device, DeviceData.class);
            // 需要修改ID和access_location
            bean.setId(null);
            bean.setAccessLocation(device.getBindingLocation());
            bean.setFunctionId(k);
            bean.setDataValue(v+"");
            bean.setAlarmTime(time);
            list.add(bean);
        });
        saveBatch(list);
        //将数据同步到Redis中
        redisTemplate.opsForHash().put(RedisKeyConstant.IOT_DEVICE_DATA_KEY,deviceId, JSONUtil.toJsonStr(list));
    }

    /**
     * 新增设备数据
     * 
     * @param deviceData 设备数据
     * @return 结果
     */
    @Override
    public int insertDeviceData(DeviceData deviceData)
    {
        return save(deviceData)? 1 : 0;
    }

    /**
     * 修改设备数据
     * 
     * @param deviceData 设备数据
     * @return 结果
     */
    @Override
    public int updateDeviceData(DeviceData deviceData)
    {
        return updateById(deviceData)? 1 : 0;
    }

    /**
     * 批量删除设备数据
     * 
     * @param ids 需要删除的设备数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    /**
     * 删除设备数据信息
     * 
     * @param id 设备数据主键
     * @return 结果
     */
    @Override
    public int deleteDeviceDataById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
