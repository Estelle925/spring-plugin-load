package evisionos.plugin.server;

import evisionos.plugin.PluginConfig;
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

    public PluginConfigVO preLoad(Path jarPath) {
        return pluginLoader.preLoad(jarPath);
    }

    public boolean loadAndRegister(Path jarPath) {
        try {
            Plugin plugin = pluginLoader.load(jarPath);
            Plugin successPlugin = pluginManager.register(plugin);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    public boolean removeAndDestroy(String pluginName, String pluginVersion) {
        try {
            Plugin plugin = pluginManager.remove(pluginName, pluginVersion);
            destroyPlugin(plugin);
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    private void destroyPlugin(Plugin plugin) {
        if (Objects.nonNull(plugin)) {
            try {
                for (Object targetBean : plugin.getMvcController()) {
                    SpringUtils.unRegisterController(plugin.getPluginApplicationContext(),targetBean.getClass());
                }
                plugin.destroy();
            } catch (Throwable e) {
                log.error(String.format("Failed to destroy plugin: name=%s, version=%s", plugin.getPluginConfig().name(), plugin.getPluginConfig().version()), e);
                throw new PluginRuntimeException("Failed to destroy plugin", e);
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
        List<PluginConfigVO> result = pluginManager.allPlugins().stream().map(plugin -> {
            PluginConfig pluginConfig = plugin.getPluginConfig();
            return PluginConfigVO.configToConfigVo(pluginConfig);
        }).collect(Collectors.toList());
        return GsonUtils.toJson(result);
    }

}