package com.ykm.miaosha.controller;

import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.error.EmBusinessError;
import com.ykm.miaosha.response.CommonReturnType;
import com.ykm.miaosha.service.OrderService;
import com.ykm.miaosha.service.dto.OrderDTO;
import com.ykm.miaosha.service.dto.UserDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @Author ykm
 * @Date 2019/10/22 14:24
 */
@RestController
@RequestMapping("/order")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @RequestMapping(value = "/createorder", method = {RequestMethod.POST})
    public CommonReturnType createOrder(@RequestParam("itemId") Integer itemId,
                                        @RequestParam("amount") Integer amount,
                                        @RequestParam(value = "promoId", required = false) Integer promoId,
                                        HttpServletRequest request) throws BusinessException {
        Boolean isLogin = (Boolean) request.getSession().getAttribute("IS_LOGIN");
        if (isLogin == null || !isLogin) {
            throw new BusinessException(EmBusinessError.USER_NOT_LOGIN);
        }
        // 获取用户登录信息
        UserDTO userDTO = (UserDTO) request.getSession().getAttribute("LOGIN_USER");
        OrderDTO orderDTO = orderService.createOrder(userDTO.getId(), itemId, promoId, amount);
        return CommonReturnType.create(null);
    }
}
