package evisionos.plugin.server;

import lombok.Data;

/**
 * @author chenhaiming
 */
@Data
public class PluginConfigVO {
    private String name;

    private String version;

    private String desc;

    /**
     * 使用ip,本地可不填写
     */
    private String ip;

    /**
     * 端口
     */
    private Integer port;

    /**
     * 协议
     */
    private String protocol;

    /**
     * 主题 例mqtt topic
     */
    private String topic;

    public static PluginConfigVO configToConfigVo(PluginConfig config) {
        PluginConfigVO configVO = new PluginConfigVO();
        configVO.setName(config.name());
        configVO.setVersion(config.version());
        configVO.setIp(config.ip());
        configVO.setPort(config.port());
        configVO.setProtocol(config.protocol());
        configVO.setTopic(config.topic());
        configVO.setDesc(config.desc());
        return configVO;
    }
}