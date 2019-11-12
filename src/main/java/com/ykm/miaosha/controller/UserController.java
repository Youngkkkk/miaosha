package com.ykm.miaosha.controller;

import com.ykm.miaosha.controller.viewobject.UserVO;
import com.ykm.miaosha.error.BusinessException;
import com.ykm.miaosha.error.EmBusinessError;
import com.ykm.miaosha.response.CommonReturnType;
import com.ykm.miaosha.service.UserService;
import com.ykm.miaosha.service.dto.UserDTO;
import com.ykm.miaosha.util.Md5Util;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.Random;

/**
 * @Author ykm
 * @Date 2019/10/17 14:46
 */
@RestController
@RequestMapping("/user")
@CrossOrigin(allowCredentials = "true", allowedHeaders = "*")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private HttpServletRequest httpServletRequest;

    private Logger logger = LoggerFactory.getLogger(UserController.class);

    // 用户登录接口
    @RequestMapping("login")
    public CommonReturnType login(@RequestParam(name = "telphone") String telphone,
                                  @RequestParam(name = "password") String password) throws BusinessException {

        // 入参校验
        if (StringUtils.isEmpty(telphone) || StringUtils.isEmpty(password)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR);
        }

        // 用户校验
        UserDTO userDTO = userService.validateLogin(telphone, Md5Util.md5(password));
        // 将登录凭证加入到session中
        this.httpServletRequest.getSession().setAttribute("IS_LOGIN", true);
        this.httpServletRequest.getSession().setAttribute("LOGIN_USER", userDTO);
        return CommonReturnType.create(null);
    }

    // 用户注册接口
    @RequestMapping("register")
    public CommonReturnType register(@RequestParam(name = "telphone") String telphone,
                                     @RequestParam(name = "otpCode") String otpCode,
                                     @RequestParam(name = "name") String name,
                                     @RequestParam(name = "gender") Integer gender,
                                     @RequestParam(name = "age") Integer age,
                                     @RequestParam(name = "password") String password) throws BusinessException {

        // 验证手机号和otpcode相符合
        String inSessionOtpCode = String.valueOf(this.httpServletRequest.getSession().getAttribute(telphone));
        if (!StringUtils.equals(otpCode, inSessionOtpCode)) {
            throw new BusinessException(EmBusinessError.PARAMETER_VALIDATION_ERROR, "短信验证码不符合");
        }

        //用户的注册流程
        UserDTO userDTO = new UserDTO();
        userDTO.setName(name);
        userDTO.setGender(new Byte(String.valueOf(gender.intValue())));
        userDTO.setAge(age);
        userDTO.setTelphone(telphone);
        userDTO.setRegisterMode("byphone");
        userDTO.setEncrptPassword(Md5Util.md5(password));

        int result = 0;
        result = userService.register(userDTO);
        if (result <= 0) {
            throw new BusinessException(EmBusinessError.UNKNOWN_ERROR, "注册操作失败");
        }
        return CommonReturnType.create(null);

    }

    // 用户获取otp短信接口
    @PostMapping("getotp")
    public CommonReturnType getOtp(@RequestParam("telphone") String telphone) {
        // 生成opt验证码
        Random random = new Random();
        int randomInt = random.nextInt(99999);
        randomInt += 10000;
        String otpCode = String.valueOf(randomInt);
        // 将opt验证码同对应的用户手机号相关联，使用httpsession方式绑定手机号和opt验证码
        httpServletRequest.getSession().setAttribute(telphone, otpCode);
        // 将opt验证码通过短信渠道发送给用户，省略
        logger.info("telphone = " + telphone + " & otpCode = " + otpCode);
        return CommonReturnType.create(null);
    }

    @RequestMapping("/get")
    public CommonReturnType getUser(@RequestParam("id") Integer id) throws BusinessException {
        UserDTO userDTO = userService.getUserById(id);
        if (userDTO == null) {
            throw new BusinessException(EmBusinessError.USER_NOT_EXIST);
        }
        UserVO userVO = convertFromModel(userDTO);
        return CommonReturnType.create(userVO);
    }

    private UserVO convertFromModel(UserDTO userDTO) {
        if (userDTO == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(userDTO, userVO);
        return userVO;
    }
}
