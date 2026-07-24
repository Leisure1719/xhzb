package com.xhzb.nursing.service;

import java.util.List;
import com.xhzb.nursing.domain.Elder;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 老人表Service接口
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
public interface IElderService extends IService<Elder>
{
    /**
     * 查询老人表
     * 
     * @param id 老人表主键
     * @return 老人表
     */
    public Elder selectElderById(Long id);

    /**
     * 查询老人表列表
     * 
     * @param elder 老人表
     * @return 老人表集合
     */
    public List<Elder> selectElderList(Elder elder);

    /**
     * 新增老人表
     * 
     * @param elder 老人表
     * @return 结果
     */
    public int insertElder(Elder elder);

    /**
     * 修改老人表
     * 
     * @param elder 老人表
     * @return 结果
     */
    public int updateElder(Elder elder);

    /**
     * 批量删除老人表
     * 
     * @param ids 需要删除的老人表主键集合
     * @return 结果
     */
    public int deleteElderByIds(Long[] ids);

    /**
     * 删除老人表信息
     * 
     * @param id 老人表主键
     * @return 结果
     */
    public int deleteElderById(Long id);
}
