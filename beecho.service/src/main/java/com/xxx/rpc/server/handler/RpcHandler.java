package com.xxx.rpc.server.handler;

import com.xxx.rpc.common.bean.RpcRequest;
import com.xxx.rpc.common.bean.RpcResponse;
import com.xxx.rpc.server.RpcServer;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cglib.reflect.FastClass;
import org.springframework.cglib.reflect.FastMethod;

import java.util.Map;

/**
 * Description
 * ...
 * DATE 2017/11/9.
 *
 * @author Mr. Chun.
 */
public class RpcHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcHandler.class);

    private final Map<String, Object> handlerMap;

    public RpcHandler(Map<String, Object> handlerMap) {
        this.handlerMap = handlerMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest request) throws Exception {
        LOGGER.debug("Receive request " + request.getRequestId());
        RpcResponse response = new RpcResponse();
        response.setRequestId(request.getRequestId());
        try {
            Object result = handle(request);
            response.setResult(result);
        } catch (Throwable t) {
            response.setError(t.toString());
            LOGGER.error("RPC Server handle request error", t);
        }
        channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                LOGGER.debug("Send response for request " + request.getRequestId());
            }
        });


//        RpcServer.submit(new Runnable() {
//            @Override
//            public void run() {
//                LOGGER.debug("Receive request " + request.getRequestId());
//                RpcResponse response = new RpcResponse();
//                response.setRequestId(request.getRequestId());
//                try {
//                    Object result = handle(request);
//                    response.setResult(result);
//                } catch (Throwable t) {
//                    response.setError(t.toString());
//                    LOGGER.error("RPC Server handle request error", t);
//                }
//                channelHandlerContext.writeAndFlush(response).addListener(new ChannelFutureListener() {
//                    @Override
//                    public void operationComplete(ChannelFuture channelFuture) throws Exception {
//                        LOGGER.debug("Send response for request " + request.getRequestId());
//                    }
//                });
//            }
//        });
    }

    private Object handle(RpcRequest request) throws Throwable {
        String className = request.getClassName();
        Object serviceBean = handlerMap.get(className);

        Class<?> serviceClass = serviceBean.getClass();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        LOGGER.debug(serviceClass.getName());
        LOGGER.debug(methodName);
        for (int i = 0; i < parameterTypes.length; ++i) {
            LOGGER.debug(parameterTypes[i].getName());
        }
        for (int i = 0; i < parameters.length; ++i) {
            LOGGER.debug(parameters[i].toString());
        }

        // JDK reflect
        /*Method method = serviceClass.getMethod(methodName, parameterTypes);
        method.setAccessible(true);
        return method.invoke(serviceBean, parameters);*/

        // Cglib reflect
        FastClass serviceFastClass = FastClass.create(serviceClass);
        FastMethod serviceFastMethod = serviceFastClass.getMethod(methodName, parameterTypes);
        return serviceFastMethod.invoke(serviceBean, parameters);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        LOGGER.error("server caught exception", cause);
        ctx.close();
    }
}
