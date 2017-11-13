package com.xxx.rpc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * Description
 * <p>
 * </p>
 * DATE 2017/9/12.
 *
 * @author zhangchunju.
 */
@SpringBootApplication(exclude = DataSourceAutoConfiguration.class)
@ComponentScan(basePackages = {"com.xxx.rpc.*"})
public class App {
    public static void main(String[] args) {
        SpringApplication.run(App.class, args);
    }
}
