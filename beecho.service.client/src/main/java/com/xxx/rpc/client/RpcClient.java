package com.xxx.rpc.client;

import com.xxx.rpc.client.handler.RpcClientHandler;
import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import com.xxx.rpc.common.codec.RpcDecoder;
import com.xxx.rpc.common.codec.RpcEncoder;
import com.xxx.rpc.registry.ServiceDiscovery;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Description
 * <p>
 *     RPC 客户端
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */
@Component
public class RpcClient {

    @Autowired
    private ServiceDiscovery serviceDiscovery;

    // 存放请求编号与响应之间的映射关系
    private ConcurrentMap<String, RpcResponse> responseMap = new ConcurrentHashMap<String, RpcResponse>();

    public <T> T create(final Class<?> interfaceClass) {
        // 创建动态代理对象
        return (T) Proxy.newProxyInstance(
                interfaceClass.getClassLoader(),
                new Class<?>[]{interfaceClass},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        // 创建 RPC 请求对象
                        RpcRequest request = new RpcRequest();
                        request.setRequestId(UUID.randomUUID().toString());
                        request.setClassName(method.getDeclaringClass().getName());
                        request.setMethodName(method.getName());
                        request.setParameterTypes(method.getParameterTypes());
                        request.setParameters(args);

//                        // 获取 RPC 服务地址
//                        String serviceName = interfaceClass.getName();

//                        String serviceAddress = serviceDiscovery.discover(serviceName);
//                        if (serviceAddress.equals("")) {
//                            throw new RuntimeException("server address is empty");
//                        }
//                        // 从 RPC 服务地址中解析主机名与端口号
//                        String[] array = serviceAddress.split(":");
//                        String host = array[0];
//                        int port = Integer.parseInt(array[1]);

                        // 发送 RPC 请求
                        String host = InetAddress.getLocalHost().getHostAddress();
                        Integer port = 9000;
                        RpcResponse response = send(request, host, port);
                        if (response == null) {
                            return null;
                        }

                        // 获取响应结果
                        return response.getResult();
                    }
                }
        );
    }

    private RpcResponse send(RpcRequest request, String host, int port) {
        RpcResponse response = null;
        EventLoopGroup group = new NioEventLoopGroup(1); // 单线程模式
        try {
            // 创建 RPC 连接
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.group(group);
            bootstrap.channel(NioSocketChannel.class);

            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new RpcEncoder(RpcRequest.class)); // 编码 RPC 请求
                    ch.pipeline().addLast(new RpcDecoder(RpcResponse.class)); // 解码 RPC 响应
                    ch.pipeline().addLast(new RpcClientHandler(responseMap)); // 处理 RPC 响应
                }
            });

            // for test

//            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
//                @Override
//                public void initChannel(SocketChannel ch) throws Exception {
//                    ch.pipeline().addLast(new DemoClientInboundHandler());
//                }
//            });

            ChannelFuture future = bootstrap.connect(host,port).sync();
            // 写入 RPC 请求对象
            future.channel().writeAndFlush(request).sync();
            future.channel().closeFuture().sync();

            // 获取 RPC 响应对象
            response = responseMap.get(request.getRequestId());
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            // 关闭连接
            group.shutdownGracefully();
            // 移除请求编号与响应对象之间的映射关系
            responseMap.remove(request.getRequestId());
        }
        return response;
    }
}
