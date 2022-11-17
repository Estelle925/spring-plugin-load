package evisionos.plugin.server;

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
    public abstract String ip();

    /**
     * 端口
     */
    public abstract Integer port();

    /**
     * 协议
     */
    public abstract String protocol();

    /**
     * 主题 例mqtt topic
     */
    public abstract String parameter();


}