package com.xhzb.nursing.service;

import java.util.List;
import com.xhzb.nursing.domain.CheckInConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 入住配置表Service接口
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
public interface ICheckInConfigService extends IService<CheckInConfig>
{
    /**
     * 查询入住配置表
     * 
     * @param id 入住配置表主键
     * @return 入住配置表
     */
    public CheckInConfig selectCheckInConfigById(Long id);

    /**
     * 查询入住配置表列表
     * 
     * @param checkInConfig 入住配置表
     * @return 入住配置表集合
     */
    public List<CheckInConfig> selectCheckInConfigList(CheckInConfig checkInConfig);

    /**
     * 新增入住配置表
     * 
     * @param checkInConfig 入住配置表
     * @return 结果
     */
    public int insertCheckInConfig(CheckInConfig checkInConfig);

    /**
     * 修改入住配置表
     * 
     * @param checkInConfig 入住配置表
     * @return 结果
     */
    public int updateCheckInConfig(CheckInConfig checkInConfig);

    /**
     * 批量删除入住配置表
     * 
     * @param ids 需要删除的入住配置表主键集合
     * @return 结果
     */
    public int deleteCheckInConfigByIds(Long[] ids);

    /**
     * 删除入住配置表信息
     * 
     * @param id 入住配置表主键
     * @return 结果
     */
    public int deleteCheckInConfigById(Long id);
}
