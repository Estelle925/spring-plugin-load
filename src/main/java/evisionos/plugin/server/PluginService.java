package evisionos.plugin.server;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PluginService {

    @Resource
    private PluginLoader pluginLoader;
    @Resource
    private PluginManager pluginManager;

    public void loadAndRegister(Path jarPath) {
        Plugin plugin = pluginLoader.load(jarPath);
        Plugin oldPlugin = pluginManager.register(plugin);
        log.warn("插件安装成功 plugin={}", oldPlugin);
    }

    public void removeAndDestroy(String pluginName, String pluginVersion) {
        Plugin plugin = pluginManager.remove(pluginName, pluginVersion);
        destroyPlugin(plugin);
    }

    private void destroyPlugin(Plugin plugin) {
        if (Objects.nonNull(plugin)) {
            try {
                plugin.destroy();
            } catch (Exception e) {
                log.error(String.format("Failed to destroy plugin: name=%s, version=%s", plugin.getPluginConfig().name(), plugin.getPluginConfig().version()), e);
            }
        }
    }

    public Object handle(String pluginName, String pluginVersion, String handlerName, Map<String, Object> handlerArgs) {
        Plugin plugin = pluginManager.find(pluginName, pluginVersion);
        if (Objects.isNull(plugin)) {
            throw new PluginRuntimeException("plugin not exist");
        }
        return plugin.doHandler(handlerName, GsonUtils.toJson(handlerArgs));
    }

    public String queryAllPlugin() {
        List<pluginVO> result = pluginManager.allPlugins().stream().map(plugin -> {
            PluginConfig pluginConfig = plugin.getPluginConfig();
            return new pluginVO(pluginConfig.name(), pluginConfig.version(), pluginConfig.desc(), plugin.getJarPath().toString());
        }).collect(Collectors.toList());
        return GsonUtils.toJson(result);
    }

    @Data
    @AllArgsConstructor
    private static class pluginVO {
        private String name;
        private String version;
        private String desc;
        private String jarPath;
    }

}