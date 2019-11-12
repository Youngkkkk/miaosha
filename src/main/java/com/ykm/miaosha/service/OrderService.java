package com.ykm.miaosha.service;

import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.service.dto.OrderDTO;

/**
 * @Author ykm
 * @Date 2019/10/22 10:13
 */
public interface OrderService {
    OrderDTO createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException;
}
