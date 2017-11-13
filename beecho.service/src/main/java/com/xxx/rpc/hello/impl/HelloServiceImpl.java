package com.xxx.rpc.hello.impl;

import com.xxx.rpc.annotation.RpcService;
import com.xxx.rpc.hello.HelloService;

/**
 * Description
 * <p>
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */

@RpcService(HelloService.class)
public class HelloServiceImpl implements HelloService {
    public String say(String name) {
        return "hello " + name;
    }
}
