package com.xxx.rpc;

import com.xxx.rpc.client.RpcClient;
import com.xxx.rpc.hello.HelloService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

import javax.annotation.PostConstruct;
import java.net.UnknownHostException;

/**
 * Description
 * <p>
 *     http://www.cnblogs.com/luxiaoxun/p/3959450.html
 * </p>
 * DATE 2017/9/12.
 *
 * @author zhangchunju.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.xxx.rpc.*"})
public class App {

    @Autowired
    private RpcClient rpcClient;

    // 构造方法之后调用
    @PostConstruct
    public void run() throws UnknownHostException {
        System.out.println("Client start ...");
        HelloService helloService = rpcClient.create(HelloService.class);
        System.out.println(helloService.say("world"));
        System.out.println("Client finish");
    }

    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
