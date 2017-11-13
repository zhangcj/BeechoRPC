package com.xxx.rpc.server.handler;

import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Description
 * <p>
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    // 存放服务名称与服务实例之间的映射
    private final Map<String,Object> handlerMap;

    public RpcServerHandler(Map<String,Object> handlerMap){
        this.handlerMap = handlerMap;
    }

    /**
     * 用于从Channel中读取数据
     * @param ctx
     * @param rpcRequest
     * @throws Exception
     */
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest rpcRequest) {
        // 创建 RPC 响应对象
        RpcResponse response = new RpcResponse();
        response.setRequestId(rpcRequest.getRequestId());

        try {
            // 处理 RPC 请求成功
            Object result = handle(rpcRequest);
            response.setResult(result);
        } catch (Exception e) {
            // 处理 RPC 请求失败
            response.setError(e.getMessage());
        }
        System.out.println("rpcRequest = [" + rpcRequest + "], response = [" + response + "]");

        // 写入 RPC 响应对象，同时关闭与客户端连接
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause){
        cause.printStackTrace();
        ctx.close();
    }


    private Object handle(RpcRequest request) throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
        // 获取服务实例
        String serviceName = request.getClassName();
        Object serviceBean = handlerMap.get(serviceName);
        if(serviceBean == null){
            throw new RuntimeException(String.format("Can not find service bean by key: %s",serviceName));
        }

        // 获取反射调用所需的变量
        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        // 执行反射调用
        Method method = serviceClass.getMethod(methodName,parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean,parameters);
    }
}
