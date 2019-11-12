package com.ykm.miaosha.service;

import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.service.dto.UserDTO;

/**
 * @Author ykm
 * @Date 2019/10/17 14:48
 */
public interface UserService {
    // 通过用户ID获取用户
    UserDTO getUserById(Integer id);

    int register(UserDTO userDTO) throws BusinessException;

    UserDTO validateLogin(String telphone, String password) throws BusinessException;
}
