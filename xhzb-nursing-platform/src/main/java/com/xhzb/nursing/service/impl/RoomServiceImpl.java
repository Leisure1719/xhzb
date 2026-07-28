package com.xhzb.nursing.service.impl;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.xhzb.common.constant.CacheConstants;
import com.xhzb.common.utils.StringUtils;
import com.xhzb.nursing.constant.RedisKeyConstant;
import com.xhzb.nursing.domain.DeviceData;
import com.xhzb.nursing.domain.Room;
import com.xhzb.nursing.mapper.DeviceDataMapper;
import com.xhzb.nursing.mapper.RoomMapper;
import com.xhzb.nursing.service.IRoomService;
import com.xhzb.nursing.domain.vo.RoomVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * 房间Service业务层处理
 *
 * @author ruoyi
 * @date 2025-03-28
 */
@Service
public class RoomServiceImpl extends ServiceImpl<RoomMapper, Room> implements IRoomService {
    @Autowired
    private RoomMapper roomMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;
    @Autowired
    private DeviceDataMapper deviceDataMapper;

    /**
     * 查询房间
     *
     * @param id 房间主键
     * @return 房间
     */
    @Override
    public Room selectRoomById(Long id) {
        return getById(id);
    }

    /**
     * 查询房间列表
     *
     * @param room 房间
     * @return 房间
     */
    @Override
    public List<Room> selectRoomList(Room room) {
        return roomMapper.selectRoomList(room);
    }

    /**
     * 新增房间
     *
     * @param room 房间
     * @return 结果
     */
    @Override
    public int insertRoom(Room room) {
        return save(room) ? 1 : 0;
    }

    /**
     * 修改房间
     *
     * @param room 房间
     * @return 结果
     */
    @Override
    public int updateRoom(Room room) {
        return updateById(room) ? 1 : 0;
    }

    /**
     * 批量删除房间
     *
     * @param ids 需要删除的房间主键
     * @return 结果
     */
    @Override
    public int deleteRoomByIds(Long[] ids) {
        return removeByIds(Arrays.asList(ids)) ? 1 : 0;
    }

    /**
     * 根据楼层 id 获取房间视图对象列表
     *
     * @param floorId
     * @return
     */
    @Override
    public List<RoomVo> getRoomsByFloorId(Long floorId) {
        return roomMapper.selectByFloorId(floorId);
    }


    @Override
    public RoomVo getRoomOne(Long id) {
        RoomVo roomVo = roomMapper.getRoomOne(id);
        return roomVo;
    }

    @Override
    public List<RoomVo> getRoomsWithDeviceByFloorId(long floorId) {
        //从表里封装数据
        List<RoomVo> roomVos = roomMapper.selectRoomsWithDeviceByFloorId(floorId);
        roomVos.forEach(roomVo -> {
            //从redis中获取房间设备数据并封装
            roomVo.getDeviceVos().forEach(deviceInfo -> {
                String deviceData = (String)redisTemplate.opsForHash().get(RedisKeyConstant.IOT_DEVICE_DATA_KEY, deviceInfo.getIotId());
                if(StringUtils.isEmpty(deviceData)){
                    return;//跳出本次循环，并不是结束方法
                }
                List<DeviceData> list = JSONUtil.toList(deviceData, DeviceData.class);
                deviceInfo.setDeviceDataVos(list);
            });
            //从redis中获取床位设备数据并封装
            roomVo.getBedVoList().forEach(bedVo -> {
                bedVo.getDeviceVos().forEach(deviceInfo -> {
                    String deviceData = (String)redisTemplate.opsForHash().get(RedisKeyConstant.IOT_DEVICE_DATA_KEY, deviceInfo.getIotId());
                    if(StringUtils.isEmpty(deviceData)){
                        return;//跳出本次循环，并不是结束方法
                    }
                    List<DeviceData> list = JSONUtil.toList(deviceData, DeviceData.class);
                    deviceInfo.setDeviceDataVos(list);
                });
            });
        });

        return roomVos;
    }

    /**
     * 获取所有房间（负责老人）
     *
     * @param floorId
     * @return
     */
    @Override
    public List<RoomVo> getRoomsWithNurByFloorId(Long floorId) {
        return roomMapper.selectByFloorIdWithNur(floorId);
    }

}
