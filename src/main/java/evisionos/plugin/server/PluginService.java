package evisionos.plugin.server;

import evisionos.plugin.PluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
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

    public boolean loadAndRegister(Path jarPath, String pluginName, String pluginVersion) {
        try {
            if (!Files.exists(jarPath)) {
                throw new PluginRuntimeException("jar file is noe exist");
            }
            if (StringUtils.isNotBlank(pluginName) && StringUtils.isNotBlank(pluginVersion)) {
                Plugin existPlugin = pluginManager.find(pluginName, pluginVersion);
                if (existPlugin != null) {
                    throw new PluginRuntimeException("plugin load and register fail plugin is exist");
                }
            }
            List<Plugin> allPlugin = (List<Plugin>) pluginManager.allPlugins();
            for (Plugin plugin : allPlugin) {
                if (plugin.getJarPath().toString().equals(jarPath.toString())) {
                    throw new PluginRuntimeException("plugin load and register fail plugin jar file is loaded");
                }
            }
            Plugin plugin = pluginLoader.load(jarPath);
            Plugin successPlugin = pluginManager.register(plugin, pluginName, pluginVersion);
        } catch (Exception e) {
            throw new PluginRuntimeException("plugin load and register fail", e);
        }
        return true;
    }

    public boolean removeAndDestroy(String pluginName, String pluginVersion) {
        try {
            if (pluginManager.find(pluginName, pluginVersion) == null) {
                throw new PluginRuntimeException("plugin load and register fail plugin is not exist");
            }
            Plugin plugin = pluginManager.remove(pluginName, pluginVersion);
            destroyPlugin(plugin);
        } catch (Exception e) {
            throw new PluginRuntimeException("plugin remove and destroy fail", e);
        }
        return true;
    }

    private void destroyPlugin(Plugin plugin) {
        if (Objects.nonNull(plugin)) {
            try {
                for (Object targetBean : plugin.getMvcController()) {
                    SpringUtils.unRegisterController(plugin.getPluginApplicationContext(), targetBean.getClass());
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
