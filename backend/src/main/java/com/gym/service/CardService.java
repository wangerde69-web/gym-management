package com.gym.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gym.entity.Card;
import java.util.List;

/* 会员卡服务接口，定义购卡、支付确认/拒绝、审批、退款等完整业务流 */
public interface CardService extends IService<Card> {
    Card buyCard(Integer memberId, String cardTypeKey);        // 购卡
    void confirmPayment(Integer cardId, Integer memberId);     // 确认支付
    void rejectPayment(Integer cardId, Integer memberId);      // 拒绝支付（标记未支付）
    void confirmUnpaid(Integer cardId);                        // 管理员确认未支付
    void refundCard(Integer cardId, Integer memberId);         // 申请退款
    void confirmRefund(Integer cardId, Integer memberId);      // 确认退款
    void approveCard(Integer cardId);                          // 管理员审批通过
    void deleteAndResetId(Integer id);                         // 删除并重置自增 ID
    List<Card> selectListWithDetail();                         // 联表查询（含会员姓名）
    List<Card> selectMyListWithDetail(Integer memberId);        // 联表查询指定会员的卡（含卡种名称、过期时间）
}
