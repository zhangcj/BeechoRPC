package com.xxx.rpc.server;

import com.xxx.rpc.annotation.RpcService;
import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import com.xxx.rpc.common.codec.RpcDecoder;
import com.xxx.rpc.common.codec.RpcEncoder;
import com.xxx.rpc.registry.ServiceRegistry;
import com.xxx.rpc.server.handler.DemoServerInboundHandler;
import com.xxx.rpc.server.handler.RpcHandler;
import com.xxx.rpc.server.handler.RpcServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Description
 * <p>
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */
@Component
public class RpcServer implements ApplicationContextAware, InitializingBean {

    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);

    // 服务端口
    @Value("${rpc.port}")
    private int port;

    @Autowired
    private ServiceRegistry serviceRegistry;

    // 存放服务名称与服务实例之间的映射关系
    private Map<String, Object> handlerMap = new HashMap<String, Object>();

    private static ThreadPoolExecutor threadPoolExecutor;

    // 扫描注解获取服务实例
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        // 扫描带有指定annotation注解的服务类
        Map<String, Object> serviceBeanMap = applicationContext.getBeansWithAnnotation(RpcService.class);
        if (serviceBeanMap != null) {
            for (Object serviceBean : serviceBeanMap.values()) {
                String serviceName = serviceBean.getClass().getAnnotation(RpcService.class).value().getName();
                handlerMap.put(serviceName, serviceBean);
            }
        }
    }

    public void afterPropertiesSet() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        EventLoopGroup childGroup = new NioEventLoopGroup();
        try {
            // 启动 RPC 服务
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(group, childGroup);
            bootstrap.channel(NioServerSocketChannel.class);

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline()
                            .addLast(new LengthFieldBasedFrameDecoder(65536,0,4,0,0)) // 增加粘包处理Handler
                            .addLast(new RpcDecoder(RpcRequest.class)) // 解码 RPC 请求
                            .addLast(new RpcEncoder(RpcResponse.class)) // 编码 RPC 请求
                            .addLast(new RpcHandler(handlerMap)); // 处理 RPC 请求
                }
            })
            .option(ChannelOption.SO_BACKLOG,128)
            .childOption(ChannelOption.SO_KEEPALIVE,true);

            // for DemoServerInboundHandler

//            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new DemoServerInboundHandler());
//                }
//            });

            ChannelFuture future = bootstrap.bind(port).sync();
            LOGGER.info("Server started on port {}",port);

            // 注册 RPC 服务地址

//            String serviceAddress = InetAddress.getLocalHost().getHostAddress() + ":" + port;
//            for (String name : handlerMap.keySet()) {
//                serviceRegistry.register(name, serviceAddress);
//            }

            // 释放资源
            future.channel().closeFuture().sync();
        } finally {
            // 关闭 RPC 服务
            childGroup.shutdownGracefully();
            group.shutdownGracefully();
        }
    }

    public static void submit(Runnable task){
        if(threadPoolExecutor == null){
            synchronized (RpcServer.class){
                if(threadPoolExecutor == null){
                    threadPoolExecutor = new ThreadPoolExecutor(16,16,600L, TimeUnit.SECONDS,new ArrayBlockingQueue<Runnable>(65536));
                }
            }
        }
        threadPoolExecutor.submit(task);
    }
}
