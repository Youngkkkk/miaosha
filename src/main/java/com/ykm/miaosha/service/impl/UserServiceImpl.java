package com.ykm.miaosha.service.impl;

import com.ykm.miaosha.dataobject.UserDO;
import com.ykm.miaosha.dataobject.UserPasswordDO;
import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.error.EmBusinessError;
import com.ykm.miaosha.mapper.UserDOMapper;
import com.ykm.miaosha.mapper.UserPasswordDOMapper;
import com.ykm.miaosha.service.UserService;
import com.ykm.miaosha.service.dto.UserDTO;
import com.ykm.miaosha.validator.ValidationResult;
import com.ykm.miaosha.validator.ValidatorImpl;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @Author ykm
 * @Date 2019/10/17 14:48
 */
@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDOMapper userDOMapper;
    @Autowired
    private UserPasswordDOMapper userPasswordDOMapper;
    @Autowired
    private ValidatorImpl validator;

    @Override
    public UserDTO getUserById(Integer id) {
        UserDO userDO = userDOMapper.selectByPrimaryKey(id);
        if (userDO == null) {
            return null;
        }
        // 通过用户id获取对应用户加密密码信息
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        return convertFromDO(userDO, userPasswordDO);

    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int register(UserDTO userDTO) throws BusinessException {
        if (userDTO == null) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }
        ValidationResult validate = validator.validate(userDTO);
        if (validate.isHasErrors()) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, validate.getErrMsg());
        }
        UserDO userDO = convertFromDTO(userDTO);
        try {
            userDOMapper.insertSelective(userDO);
        } catch (DuplicateKeyException e) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "手机号已重复注册");
        }

        userDTO.setId(userDO.getId());

        UserPasswordDO userPasswordDO = convertPasswordFromDTO(userDTO);
        return userPasswordDOMapper.insertSelective(userPasswordDO);

    }

    @Override
    public UserDTO validateLogin(String telphone, String password) throws BusinessException {
        UserDO userDO = userDOMapper.selectByTelphone(telphone);
        if (userDO == null) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        UserPasswordDO userPasswordDO = userPasswordDOMapper.selectByUserId(userDO.getId());
        UserDTO userDTO = convertFromDO(userDO, userPasswordDO);

        //比对用户信息内加密的密码是否和传输进来的密码相匹配
        if (!StringUtils.equals(password, userDTO.getEncrptPassword())) {
            throw new BusinessException(EmBusinessError.USER_LOGIN_FAIL);
        }
        return userDTO;
    }

    private UserPasswordDO convertPasswordFromDTO(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        UserPasswordDO userPasswordDO = new UserPasswordDO();
        userPasswordDO.setEncrptPassword(userDTO.getEncrptPassword());
        userPasswordDO.setUserId(userDTO.getId());
        return userPasswordDO;
    }

    private UserDO convertFromDTO(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        UserDO userDO = new UserDO();
        BeanUtils.copyProperties(userDTO, userDO);
        return userDO;
    }

    private UserDTO convertFromDO(UserDO userDO, UserPasswordDO userPasswordDO) {
        if (userDO == null) {
            return null;
        }
        UserDTO userDTO = new UserDTO();
        BeanUtils.copyProperties(userDO, userDTO);
        if (userPasswordDO != null) {
            userDTO.setEncrptPassword(userPasswordDO.getEncrptPassword());
        }
        return userDTO;
    }
}
