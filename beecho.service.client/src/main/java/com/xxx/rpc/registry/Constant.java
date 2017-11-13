package com.xxx.rpc.registry;

/**
 * Description
 * <p>
 *     相关常量
 * </p>
 * DATE 2017/9/11.
 *
 * @author zhangchunju.
 */
public interface Constant {

    int ZK_SESSION_TIMEOUT = 5000;
    String ZK_REGISTRY_PATH = "/registry";
    String ZK_DATA_PATH = ZK_REGISTRY_PATH + "/data";
}
