package evisionos.plugin.server;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author chenhaiming
 */
interface PluginConfig {

    String name();

    String version();

    String desc();

    /**
     * 使用ip,本地可不填写
     */
    String ip();

    /**
     * 端口
     */
    Integer port();

    /**
     * 协议
     */
    String protocol();

    /**
     * 主题 例mqtt topic
     */
    String parameter();

}