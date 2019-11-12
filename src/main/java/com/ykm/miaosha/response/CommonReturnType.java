package com.ykm.miaosha.response;

/**
 * @Author ykm
 * @Date 2019/10/17 16:04
 */
public class CommonReturnType {
    // 返回处理结果 success 或 fail
    private String status;
    // 若status为success，则data返回前端所需json数据
    // 若status为fail，则data内返回通用错误码格式
    private Object data;

    // 定义一个通用的创建方法
    public static CommonReturnType create(Object result) {
        return CommonReturnType.create(result, "success");
    }

    public static CommonReturnType create(Object result, String status) {
        CommonReturnType type = new CommonReturnType();
        type.setStatus(status);
        type.setData(result);
        return type;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
