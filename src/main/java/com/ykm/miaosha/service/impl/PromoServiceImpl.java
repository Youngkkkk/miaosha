package com.ykm.miaosha.service.impl;

import com.ykm.miaosha.dataobject.PromoDO;
import com.ykm.miaosha.mapper.PromoDOMapper;
import com.ykm.miaosha.service.PromoService;
import com.ykm.miaosha.service.dto.PromoDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;

/**
 * @Author ykm
 * @Date 2019/10/22 15:22
 */
@Service
public class PromoServiceImpl implements PromoService {

    @Autowired
    private PromoDOMapper promoDOMapper;

    @Override
    public PromoDTO getPromoByItemId(Integer itemId) {
        PromoDO promoDO = promoDOMapper.selectByItemId(itemId);
        PromoDTO promoDTO = convertFromDataObject(promoDO);
        if (promoDO == null) {
            return null;
        }
        // 判断当前时间是否秒杀活动即将开始或正在进行
        Date now = new Date();
        if (promoDTO.getStartDate().after(now)) {
            // 还未开始
            promoDTO.setStatus(1);
        } else if (promoDTO.getEndDate().before(now)) {
            // 已结束
            promoDTO.setStatus(3);
        } else {
            // 正在进行中
            promoDTO.setStatus(2);
        }
        return promoDTO;
    }

    private PromoDTO convertFromDataObject(PromoDO promoDO) {
        if (promoDO == null) {
            return null;
        }
        PromoDTO promoDTO = new PromoDTO();
        BeanUtils.copyProperties(promoDO, promoDTO);
        promoDTO.setPromoItemPrice(new BigDecimal(promoDO.getPromoItemPrice()));
        return promoDTO;
    }
}
