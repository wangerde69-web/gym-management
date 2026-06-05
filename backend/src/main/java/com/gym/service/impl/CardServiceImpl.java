package com.gym.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gym.entity.Card;
import com.gym.entity.CardTypeEntity;
import com.gym.mapper.CardMapper;
import com.gym.service.CardService;
import com.gym.service.CardTypeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;

/* 会员卡服务实现类，实现购卡、支付确认、审批、退款等核心业务逻辑及会员卡自动过期定时任务 */
@Service
public class CardServiceImpl extends ServiceImpl<CardMapper, Card> implements CardService {

    @Autowired
    private CardTypeService cardTypeService;

    @Autowired
    private ResetIdUtil resetIdUtil;

    /* 会员卡自动过期定时任务：每日零点将已过期的生效中卡片标记为已过期 */
    @Scheduled(cron = "0 0 0 * * ?")
    public void autoExpireCards() {
        baseMapper.updateExpiredCards();
    }

    // 购卡逻辑：根据卡种配置创建会员卡，设置有效期与初始状态
    @Override
    public Card buyCard(Integer memberId, String cardTypeKey) {
        CardTypeEntity cardType = cardTypeService.lambdaQuery()
                .eq(CardTypeEntity::getCardKey, cardTypeKey)
                .eq(CardTypeEntity::getStatus, 1)
                .one();

        if (cardType == null) {
            throw new RuntimeException("卡种不存在或已下架");
        }

        Card card = new Card();
        card.setMemberId(memberId);
        card.setCardTypeKey(cardTypeKey);
        card.setStatus(0); // 0=待审核（等待管理员审批）
        card.setPrice(cardType.getPrice());

        this.save(card);
        return card;
    }

    // 会员确认已支付：将状态从待审核(0)改为已确认支付待审(5)，等待管理员最终审批
    @Override
    public void confirmPayment(Integer cardId, Integer memberId) {
        Card card = this.getById(cardId);
        if (card == null || !card.getMemberId().equals(memberId) || card.getStatus() != 0) {
            throw new RuntimeException("无权操作或状态异常");
        }
        card.setStatus(5);
        this.updateById(card);
    }

    // 会员取消支付，将卡片标记为未支付状态
    @Override
    public void rejectPayment(Integer cardId, Integer memberId) {
        Card card = this.getById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new RuntimeException("无权操作");
        }
        if (card.getStatus() != 0) {
            throw new RuntimeException("状态异常");
        }
        card.setStatus(4); // 4=未支付，保留记录作为证据
        this.updateById(card);
    }

    // 管理员确认会员卡未支付状态
    @Override
    public void confirmUnpaid(Integer cardId) {
        Card card = this.getById(cardId);
        if (card == null) throw new RuntimeException("卡不存在");
        card.setStatus(4); // 4=未支付，用户可删
        this.updateById(card);
    }

    // 管理员审批通过：激活会员卡
    @Override
    public void approveCard(Integer cardId) {
        Card card = this.getById(cardId);
        if (card == null) throw new RuntimeException("卡不存在");
        card.setStatus(1); // 1=生效中
        this.updateById(card);
    }

    // 会员申请退款：将卡片状态改为已退款
    @Override
    public void refundCard(Integer cardId, Integer memberId) {
        Card card = this.getById(cardId);
        if (card == null || !card.getMemberId().equals(memberId)) {
            throw new RuntimeException("无权操作此卡");
        }
        if (card.getStatus() == 3) {
            throw new RuntimeException("此卡已退款");
        }
        card.setStatus(3); // 3=已退款
        this.updateById(card);
    }

    // 会员确认退款完成：校验权限和状态后删除卡片记录
    @Override
    public void confirmRefund(Integer cardId, Integer memberId) {
        Card card = this.getById(cardId);
        if (card == null || !card.getMemberId().equals(memberId) || card.getStatus() != 3) {
            throw new RuntimeException("无权操作或状态异常");
        }
        this.removeById(cardId);
    }

    @Override
    public void deleteAndResetId(Integer id) {
        resetIdUtil.deleteAndReset(id, "card");
    }

    // 联表查询所有会员卡记录（含会员姓名）
    @Override
    public List<Card> selectListWithDetail() {
        return baseMapper.selectListWithDetail();
    }
}
