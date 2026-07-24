package com.xhzb.nursing.service;

import java.util.List;
import com.xhzb.nursing.domain.CheckIn;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhzb.nursing.domain.dto.CheckInApplyDto;
import com.xhzb.nursing.domain.vo.CheckInDetailVo;

/**
 * 入住表Service接口
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
public interface ICheckInService extends IService<CheckIn>
{
    /**
     * 查询入住表
     * 
     * @param id 入住表主键
     * @return 入住表
     */
    public CheckIn selectCheckInById(Long id);

    /**
     * 查询入住表列表
     * 
     * @param checkIn 入住表
     * @return 入住表集合
     */
    public List<CheckIn> selectCheckInList(CheckIn checkIn);

    /**
     * 新增入住表
     * 
     * @param checkIn 入住表
     * @return 结果
     */
    public int insertCheckIn(CheckIn checkIn);

    /**
     * 修改入住表
     * 
     * @param checkIn 入住表
     * @return 结果
     */
    public int updateCheckIn(CheckIn checkIn);

    /**
     * 批量删除入住表
     * 
     * @param ids 需要删除的入住表主键集合
     * @return 结果
     */
    public int deleteCheckInByIds(Long[] ids);

    /**
     * 删除入住表信息
     * 
     * @param id 入住表主键
     * @return 结果
     */
    public int deleteCheckInById(Long id);

    void apply(CheckInApplyDto dto);

    CheckInDetailVo getDetailById(long id);
}
