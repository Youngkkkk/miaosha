package com.ykm.miaosha.service.impl;

import com.ykm.miaosha.dataobject.OrderDO;
import com.ykm.miaosha.dataobject.SequenceDO;
import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.error.EmBusinessError;
import com.ykm.miaosha.mapper.OrderDOMapper;
import com.ykm.miaosha.mapper.SequenceDOMapper;
import com.ykm.miaosha.service.ItemService;
import com.ykm.miaosha.service.OrderService;
import com.ykm.miaosha.service.UserService;
import com.ykm.miaosha.service.dto.ItemDTO;
import com.ykm.miaosha.service.dto.OrderDTO;
import com.ykm.miaosha.service.dto.UserDTO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;

/**
 * @Author ykm
 * @Date 2019/10/22 10:14
 */
@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private ItemService itemService;
    @Autowired
    private UserService userService;
    @Autowired
    private OrderDOMapper orderDOMapper;
    @Autowired
    private SequenceDOMapper sequenceDOMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public OrderDTO createOrder(Integer userId, Integer itemId, Integer promoId, Integer amount) throws BusinessException {
        // 1、校验下单状态
        ItemDTO itemDTO = itemService.getItemById(itemId);
        if (itemDTO == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "商品不存在");
        }
        UserDTO userDTO = userService.getUserById(userId);
        if (userDTO == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "用户不存在");
        }
        if (amount <= 0 || amount > 99) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "数量信息不正确");
        }
        // 校验活动信息
        if (promoId != null) {
            // 校验对应活动是否存在这个适用商品
            if (promoId.intValue() != itemDTO.getPromoDTO().getId()) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确");
                // 校验活动是否正在进行中
            } else if (itemDTO.getPromoDTO().getStatus().intValue() != 2) {
                throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "活动信息不正确，活动还未开始");
            }
        }
        // 2、落单减库存，支付减库存
        boolean result = itemService.decreaseStock(itemId, amount);
        if (!result) {
            throw new BusinessException(EmBusinessError.STOCK_NOT_ENOUGH);
        }
        // 3、订单入库
        OrderDTO orderDTO = new OrderDTO();
        orderDTO.setUserId(userId);
        orderDTO.setItemId(itemId);
        orderDTO.setAmount(amount);
        if (promoId != null) {
            orderDTO.setItemPrice(itemDTO.getPromoDTO().getPromoItemPrice());
        } else {
            orderDTO.setItemPrice(itemDTO.getPrice());
        }
        orderDTO.setPromoId(promoId);
        orderDTO.setOrderPrice(orderDTO.getItemPrice().multiply(new BigDecimal(amount)));
        // 生成交易流水号，订单号  TODO
        orderDTO.setId(generateOrderNo());
        OrderDO orderDO = convertFromOrderDTO(orderDTO);
        orderDOMapper.insertSelective(orderDO);
        // 加上商品的销量
        itemService.increaseSales(itemId, amount);
        // 4、返回前端
        return orderDTO;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    String generateOrderNo() {
        // 订单号有16位
        StringBuilder stringBuilder = new StringBuilder();
        // 前8位为时间信息
        LocalDateTime now = LocalDateTime.now();
        String nowDate = now.format(DateTimeFormatter.ISO_DATE).replace("-", "");
        stringBuilder.append(nowDate);
        // 中间6位为自增序列
        // 获取当前sequence
        int sequence = 0;
        SequenceDO sequenceDO = sequenceDOMapper.getSequenceByName("order_info");
        sequence = sequenceDO.getCurrentValue();
        sequenceDO.setCurrentValue(sequenceDO.getCurrentValue() + sequenceDO.getStep());
        sequenceDOMapper.updateByPrimaryKeySelective(sequenceDO);
        String sequenceStr = String.valueOf(sequence);
        for (int i = 0; i < sequenceStr.length(); i++) {
            stringBuilder.append(0);
        }
        stringBuilder.append(sequenceStr);
        // 最后2位为分库分表位，暂时写死
        stringBuilder.append("00");
        return stringBuilder.toString();
    }

    private OrderDO convertFromOrderDTO(OrderDTO orderDTO) {
        if (orderDTO == null) {
            return null;
        }
        OrderDO orderDO = new OrderDO();
        BeanUtils.copyProperties(orderDTO, orderDO);
        orderDO.setItemPrice(orderDTO.getItemPrice().doubleValue());
        orderDO.setOrderPrice(orderDTO.getOrderPrice().doubleValue());
        return orderDO;
    }

}
