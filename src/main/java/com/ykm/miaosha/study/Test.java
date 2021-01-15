package com.ykm.miaosha.study;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;

/**
 * @author ykm
 * @date 2020/9/25 3:27 下午
 */
public class Test {

    interface Subject {
        void doSomething();
    }

    static class RealSubject implements Subject {

        @Override
        public void doSomething() {
            System.out.println("RealSubject do something");
        }
    }

    static class JdkDynamicProxy implements InvocationHandler {

        private Object target;

        public JdkDynamicProxy(Object target) {
            this.target = target;
        }

        /**
         * 获取被代理接口实例对象
         * @param <T>
         * @return
         */
        public <T> T getProxy() {
            return (T) Proxy.newProxyInstance(target.getClass().getClassLoader(), target.getClass().getInterfaces(), this);
        }

        @Override
        public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
            System.out.println("Do something before");
            Object result = method.invoke(target, args);
            System.out.println("Do something after");
            return result;
        }
    }

    public static void main(String[] args) {
        RealSubject realSubject = new JdkDynamicProxy(new RealSubject()).getProxy();
        realSubject.doSomething();
    }

}
