package com.ykm.miaosha.service;

import com.ykm.miaosha.service.dto.PromoDTO;

/**
 * @Author ykm
 * @Date 2019/10/22 15:20
 */
public interface PromoService {
    // 根据商品id获取即将进行的或正在进行的秒杀活动
    PromoDTO getPromoByItemId(Integer itemId);
}
