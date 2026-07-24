package com.xhzb.nursing.service;

import java.util.List;
import com.xhzb.nursing.domain.Contract;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * 合同表Service接口
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
public interface IContractService extends IService<Contract>
{
    /**
     * 查询合同表
     * 
     * @param id 合同表主键
     * @return 合同表
     */
    public Contract selectContractById(Long id);

    /**
     * 查询合同表列表
     * 
     * @param contract 合同表
     * @return 合同表集合
     */
    public List<Contract> selectContractList(Contract contract);

    /**
     * 新增合同表
     * 
     * @param contract 合同表
     * @return 结果
     */
    public int insertContract(Contract contract);

    /**
     * 修改合同表
     * 
     * @param contract 合同表
     * @return 结果
     */
    public int updateContract(Contract contract);

    /**
     * 批量删除合同表
     * 
     * @param ids 需要删除的合同表主键集合
     * @return 结果
     */
    public int deleteContractByIds(Long[] ids);

    /**
     * 删除合同表信息
     * 
     * @param id 合同表主键
     * @return 结果
     */
    public int deleteContractById(Long id);

    public void updateContract();
}
