package com.xhzb.nursing.service.impl;

import java.time.LocalDateTime;
import java.util.List;

import cn.hutool.core.collection.CollUtil;
import com.xhzb.common.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.ContractMapper;
import com.xhzb.nursing.domain.Contract;
import com.xhzb.nursing.service.IContractService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;

/**
 * 合同表Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-22
 */
@Service
public class ContractServiceImpl extends ServiceImpl<ContractMapper, Contract> implements IContractService
{
    @Autowired
    private ContractMapper contractMapper;

    /**
     * 查询合同表
     * 
     * @param id 合同表主键
     * @return 合同表
     */
    @Override
    public Contract selectContractById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询合同表列表
     * 
     * @param contract 合同表
     * @return 合同表
     */
    @Override
    public List<Contract> selectContractList(Contract contract)
    {
        return contractMapper.selectContractList(contract);
    }

    /**
     * 新增合同表
     * 
     * @param contract 合同表
     * @return 结果
     */
    @Override
    public int insertContract(Contract contract)
    {
        return save(contract)? 1 : 0;
    }

    /**
     * 修改合同表
     * 
     * @param contract 合同表
     * @return 结果
     */
    @Override
    public int updateContract(Contract contract)
    {
        return updateById(contract)? 1 : 0;
    }

    /**
     * 批量删除合同表
     * 
     * @param ids 需要删除的合同表主键
     * @return 结果
     */
    @Override
    public int deleteContractByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public void updateContract() {
        //找出合同状态为未生效的合同
        List<Contract> list = lambdaQuery().eq(Contract::getStatus, 0).list();
        if(CollUtil.isEmpty(list)){
            return;
        }
        //判断 合同入住时间 是否早于 当前时间
        List<Contract> list1 = list.stream().filter(con -> con.getStartDate().isBefore(LocalDateTime.now())).toList();
        //将合同状态修改为已生效
        list1.forEach(con -> con.setStatus(1));
    }

    /**
     * 删除合同表信息
     * 
     * @param id 合同表主键
     * @return 结果
     */
    @Override
    public int deleteContractById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
