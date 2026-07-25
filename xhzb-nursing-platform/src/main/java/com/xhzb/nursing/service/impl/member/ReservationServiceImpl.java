package com.xhzb.nursing.service.impl.member;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.xhzb.common.utils.UserThreadLocal;
import com.xhzb.nursing.domain.FamilyMember;
import com.xhzb.nursing.service.member.IFamilyMemberService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.xhzb.nursing.mapper.member.ReservationMapper;
import com.xhzb.nursing.domain.Reservation;
import com.xhzb.nursing.service.member.IReservationService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import java.util.Arrays;

/**
 * 预约信息Service业务层处理
 * 
 * @author ruoyi
 * @date 2026-07-25
 */
@Service
public class ReservationServiceImpl extends ServiceImpl<ReservationMapper, Reservation> implements IReservationService
{
    @Autowired
    private ReservationMapper reservationMapper;

    @Autowired
    private IFamilyMemberService familyMemberService;
    /**
     * 查询预约信息
     * 
     * @param id 预约信息主键
     * @return 预约信息
     */
    @Override
    public Reservation selectReservationById(Long id)
    {
        return getById(id);
    }

    /**
     * 查询预约信息列表
     * 
     * @param reservation 预约信息
     * @return 预约信息
     */
    @Override
    public List<Reservation> selectReservationList(Reservation reservation)
    {
        return reservationMapper.selectReservationList(reservation);
    }

    /**
     * 新增预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    @Override
    public int insertReservation(Reservation reservation)
    {
        return save(reservation)? 1 : 0;
    }

    /**
     * 修改预约信息
     * 
     * @param reservation 预约信息
     * @return 结果
     */
    @Override
    public int updateReservation(Reservation reservation)
    {
        return updateById(reservation)? 1 : 0;
    }

    /**
     * 批量删除预约信息
     * 
     * @param ids 需要删除的预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationByIds(Long[] ids)
    {
        return removeByIds(Arrays.asList(ids))? 1 : 0;
    }

    @Override
    public int getCancelledCount() {
        //获取ThreadLocal中的ID
        Long userId = UserThreadLocal.getUserId();
        //根据ID查询预约人手机号
        FamilyMember familyMember = familyMemberService.lambdaQuery().eq(FamilyMember::getId, userId)
                .one();
        String phone = familyMember.getPhone();
        //用于筛选当天的预约
        LocalDateTime startOfToday = LocalDate.now().atStartOfDay();
        LocalDateTime startOfTomorrow = LocalDate.now().plusDays(1).atStartOfDay();
        //根据手机号查询预约信息
        List<Reservation> list = lambdaQuery().eq(Reservation::getMobile, phone)
                .eq(Reservation::getStatus, 2)
                .ge(Reservation::getCreateTime, startOfToday)   // >= 今天 00:00:00
                .lt(Reservation::getCreateTime, startOfTomorrow) // < 明天 00:00:00
                .list();
        return list.size();
    }

    /**
     * 删除预约信息信息
     * 
     * @param id 预约信息主键
     * @return 结果
     */
    @Override
    public int deleteReservationById(Long id)
    {
        return removeById(id)? 1 : 0;
    }
}
