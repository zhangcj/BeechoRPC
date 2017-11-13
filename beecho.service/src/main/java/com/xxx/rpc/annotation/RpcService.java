package com.xxx.rpc.annotation;

import org.springframework.stereotype.Service;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Description
 * <p>
 *     此注解上带有Spring的@Service注解，
 *     所以此注解具备@Service注解的特性，
 *     所以凡是被@RpcService注解标示的类都会被Spring框架扫描到
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Service
public @interface RpcService {
    Class<?> value();
}