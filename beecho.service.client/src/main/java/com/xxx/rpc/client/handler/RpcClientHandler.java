package com.xxx.rpc.client.handler;

import com.xxx.rpc.common.bean.RpcResponse;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import java.util.concurrent.ConcurrentMap;

/**
 * Description
 * <p>
 *     用于处理 RPC 响应
 * </p>
 * DATE 2017/9/12.
 *
 * @author zhangchunju.
 */
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {

    // 存放请求编号和响应对象之间的映射
    private ConcurrentMap<String, RpcResponse> responseMap;

    public RpcClientHandler(ConcurrentMap<String, RpcResponse> responseMap) {
        this.responseMap = responseMap;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse rpcResponse) {
        System.out.println("rpcResponse = [" + rpcResponse + "]");
        // 建立请求编号和响应对象之间的映射关系
        responseMap.put(rpcResponse.getRequestId(), rpcResponse);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
