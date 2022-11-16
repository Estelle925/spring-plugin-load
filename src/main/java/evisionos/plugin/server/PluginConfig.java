package evisionos.plugin.server;

import com.google.common.collect.Sets;

import java.util.Set;

/**
 * @author chenhaiming
 */
public abstract class PluginConfig {

    public abstract String name();

    public abstract String version();

    public abstract String desc();

    /**
     * 使用ip,本地可不填写
     */
    public abstract  String ip();

    /**
     * 端口
     */
    public abstract  Integer port();

    /**
     * 协议
     */
    public abstract  String protocol();

    /**
     * 主题 例mqtt topic
     */
    public abstract  String parameter();

    public Set<String> scanPackages() {
        return Sets.newHashSet(this.getClass().getPackage().getName());
    }

    public Set<String> overridePackages() {
        return Sets.newHashSet();
    }

}