package com.ykm.miaosha.error;

import com.ykm.miaosha.response.CommonReturnType;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.NoHandlerFoundException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author ykm
 * @Date 2019/10/17 21:00
 */
@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public CommonReturnType doError(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, Exception ex) {
        Map<String, Object> responseData = new HashMap<>();
        if (ex instanceof BusinessException) {
            BusinessException businessException = (BusinessException) ex;
            responseData.put("errCode", businessException.getErrCode());
            responseData.put("errMsg", businessException.getErrMsg());
        } else if (ex instanceof MissingServletRequestParameterException) {
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", "参数 " + ((MissingServletRequestParameterException) ex).getParameterName() + " 不能为空");
        } else if (ex instanceof NullPointerException) {
            NullPointerException exception = (NullPointerException) ex;
            StackTraceElement stackTraceElement = exception.getStackTrace()[0];
            String msg = stackTraceElement.getFileName() + "在" + stackTraceElement.getLineNumber() + "行出现空指针异常";
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", msg);
        } else if (ex instanceof ServletRequestBindingException) {
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", "url绑定路由问题");
        } else if (ex instanceof NoHandlerFoundException) {
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", "没有找到对应的访问路径");
        } else if (ex instanceof MissingServletRequestParameterException) {
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", "参数 " + ((MissingServletRequestParameterException) ex).getParameterName() + " 不能为空");
        } else {
            responseData.put("errCode", EmBusinessError.UNKNOWN_ERROR.getErrCode());
            responseData.put("errMsg", EmBusinessError.UNKNOWN_ERROR.getErrMsg());
        }
        return CommonReturnType.create(responseData, "fail");
    }
}
