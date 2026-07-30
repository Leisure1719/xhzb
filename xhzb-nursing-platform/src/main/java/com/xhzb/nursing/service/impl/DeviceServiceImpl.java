package com.xhzb.nursing.service.impl;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.huaweicloud.sdk.iotda.v5.IoTDAClient;
import com.huaweicloud.sdk.iotda.v5.model.*;
import com.networknt.schema.format.DateTimeFormat;
import com.xhzb.common.core.domain.AjaxResult;
import com.xhzb.common.exception.base.BaseException;
import com.xhzb.common.utils.StringUtils;
import com.xhzb.nursing.constant.RedisKeyConstant;
import com.xhzb.nursing.domain.dto.DeviceDto;
import com.xhzb.nursing.domain.vo.DeviceDetailVo;
import com.xhzb.nursing.domain.vo.ProdoctVo;
import com.xhzb.nursing.util.DateTimeZoneConverter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.DeviceMapper;
import com.xhzb.nursing.domain.Device;
import com.xhzb.nursing.service.IDeviceService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;

/**
 * 监测设备表Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-27
 */
@Service
public class DeviceServiceImpl extends ServiceImpl<DeviceMapper, Device> implements IDeviceService
{
    @Autowired
    private DeviceMapper deviceMapper;

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private IoTDAClient iotdaClient;
    /**
     * 查询监测设备表
     * 
     * @param id 监测设备表主键
     * @return 监测设备表
     */
    @Override
    public Device selectDeviceById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询监测设备表列表
     * 
     * @param device 监测设备表
     * @return 监测设备表
     */
    @Override
    public List<Device> selectDeviceList(Device device)
    {
        return deviceMapper.selectDeviceList(device);
    }

    /**
     * 新增监测设备表
     * 
     * @param device 监测设备表
     * @return 结果
     */
    @Override
    public int insertDevice(Device device)
    {
        return save(device)? 1 : 0;
    }

    /**
     * 修改监测设备表
     * 
     * @param device 监测设备表
     * @return 结果
     */
    @Override
    public int updateDevice(Device device)
    {
        return updateById(device)? 1 : 0;
    }

    /**
     * 批量删除监测设备表
     * 
     * @param ids 需要删除的监测设备表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public List<ServiceCapability> queryProduct(String productKey) {
        //参数校验
        if(StrUtil.isBlank(productKey)){
            throw new BaseException("请输入正确的参数");
        }
        //调用华为云物联网接口
        ShowProductRequest showProductRequest = new ShowProductRequest();
        showProductRequest.setProductId(productKey);
        ShowProductResponse response;

        try {
            response = iotdaClient.showProduct(showProductRequest);
        } catch (Exception e) {
            throw new BaseException("查询产品详情失败");
        }
        //判断是否存在服务数据
        List<ServiceCapability> serviceCapabilities = response.getServiceCapabilities();
        if(CollUtil.isEmpty(serviceCapabilities)){
            return Collections.emptyList();
        }
        return serviceCapabilities;
    }

    @Override
    public List queryServiceProperties(String iotId) {
        ShowDeviceShadowRequest request = new ShowDeviceShadowRequest();
        request.withDeviceId(iotId);
        ShowDeviceShadowResponse response;
        response = iotdaClient.showDeviceShadow(request);
        if(response.getHttpStatusCode() != 200){
            throw new BaseException("查询设备上报数据失败");
        }
        List<DeviceShadowData> shadow = response.getShadow();
        if(CollUtil.isEmpty(shadow)){
            return List.of();
        }
        ArrayList list = new ArrayList();
        DeviceShadowProperties reported = shadow.get(0).getReported();
        JSONObject entries = JSONUtil.parseObj(reported.getProperties());
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");
        LocalDateTime time = LocalDateTime.parse(reported.getEventTime(), dtf);
        //时区转换
        LocalDateTime eventTime = DateTimeZoneConverter.utcToShanghai(time);
        entries.forEach((k,v) ->{
            HashMap map = new HashMap();
            map.put("functionId",k);
            map.put("value",v);
            map.put("eventTime",eventTime);
            list.add(map);
        });
        return list;
    }

    @Override
    public DeviceDetailVo getDeviceByIotId(String iotId) {
        //先从数据库里面获取已有的数据
        Device device = lambdaQuery().eq(Device::getIotId, iotId).one();
        //从物联网平台获取数据deviceStatus和activeTime
        ShowDeviceRequest request = new ShowDeviceRequest();
        request.withDeviceId(iotId);
        ShowDeviceResponse response;
        try {
            response = iotdaClient.showDevice(request);
        }catch (Exception e){
            throw new BaseException("获取设备信息失败");
        }
        if (response.getHttpStatusCode() != 200){
            throw new BaseException("获取设备信息失败");
        }
        DeviceDetailVo bean = BeanUtil.toBean(device, DeviceDetailVo.class);
        bean.setDeviceStatus(response.getStatus());
        //返回的activityTime格式是"2019-03-03T08:10:11.122Z"
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        if(response.getActiveTime() != null){
            LocalDateTime time = LocalDateTime.parse(response.getActiveTime(), dtf);
            bean.setActiveTime(time);
        }
        return bean;
    }

    @Override
    public void register(DeviceDto dto) {
        //判断设备名称是否已经存在
        Long count = lambdaQuery().eq(Device::getDeviceName, dto.getDeviceName()).count();
        if(count > 0 ){
            throw new BaseException("设备名称已存在");
        }
        //判断设备标识是否已经存在
        count = lambdaQuery().eq(Device::getNodeId,dto.getNodeId()).count();
        if(count > 0){
            throw new BaseException("设备标识已存在");
        }
        //判断同一位置是否绑定了相同产品
        //如果是随身设备，前端不会传数据过来,需要我们自己设置为-1
        dto.setPhysicalLocationType(dto.getPhysicalLocationType() == null ? -1 : dto.getPhysicalLocationType());
        count = lambdaQuery().eq(Device::getBindingLocation,dto.getBindingLocation())
                .eq(Device::getLocationType,dto.getLocationType())
                .eq(Device::getPhysicalLocationType,dto.getPhysicalLocationType())
                .eq(Device::getProductKey,dto.getProductKey())
                .count();
        if(count > 0){
            throw new BaseException("该老人/位置已绑定该产品，请重新选择");
        }

        //开始注册
        AddDeviceRequest request = new AddDeviceRequest();
        AddDevice body = new AddDevice();

        body.withDeviceName(dto.getDeviceName());
        body.withNodeId(dto.getNodeId());
        body.withProductId(dto.getProductKey());

        //秘钥设置
        AuthInfo authInfobody = new AuthInfo();
        authInfobody.withSecret(UUID.randomUUID().toString().replace("-",""));
        body.withAuthInfo(authInfobody);

        AddDeviceResponse response;
        request.withBody(body);
        try {
            response = iotdaClient.addDevice(request);
        }catch (Exception e){
            throw new BaseException("注册失败");
        }

        //本地保存设备信息
        Device bean = BeanUtil.toBean(dto, Device.class);
        //密钥
        bean.setSecret(authInfobody.getSecret());
        //设备ID
        bean.setIotId(response.getDeviceId());
        save(bean);

    }

    @Override
    public List<ProdoctVo> getAllProduct() {
        //从Redis中取出数据
        String jsonStr =  redisTemplate.opsForValue().get(RedisKeyConstant.IOT_ALL_PRODUCT_LIST)+"";
        //把jsonStr转成Product
        if(StringUtils.isEmpty(jsonStr)){
            return List.of();
        }
        List<ProdoctVo> bean = JSONUtil.toList(jsonStr, ProdoctVo.class);
        return bean;
    }

    @Override
    public void getsyncProductList() {
        ListProductsRequest request = new ListProductsRequest();
        ListProductsResponse response;
        try {
            response = iotdaClient.listProducts(request);
        }catch (Exception e){
            throw new BaseException("获取设备列表失败");
        }
        if(response.getHttpStatusCode() != 200){
            throw new BaseException("获取设备列表失败");
        }
        //将数据同步到Redis中
        redisTemplate.opsForValue().set(RedisKeyConstant.IOT_ALL_PRODUCT_LIST, JSONUtil.toJsonStr(response.getProducts()));
    }

    /**
     * 删除监测设备表信息
     * 
     * @param id 监测设备表主键
     * @return 结果
     */
    @Override
    public int deleteDeviceById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
