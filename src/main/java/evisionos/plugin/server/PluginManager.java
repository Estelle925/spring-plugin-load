package evisionos.plugin.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Component
public class PluginManager implements DisposableBean {

    /**
     * 已注册的所有模块
     */
    private final ConcurrentHashMap<String, Plugin> allPlugins = new ConcurrentHashMap<>();

    public Plugin register(Plugin plugin) {
        checkNotNull(plugin, "plugin is null");
        String key = toKey(plugin.getPluginConfig().name(), plugin.getPluginConfig().version());
        log.info("Put plugin: {}", key);
        return allPlugins.put(key, plugin);
    }

    public Collection<Plugin> allPlugins() {
        return allPlugins.values();
    }

    public Plugin find(String pluginName, String pluginVersion) {
        checkNotNull(pluginName, "plugin name is null");
        checkNotNull(pluginVersion, "plugin version is null");
        return allPlugins.get(toKey(pluginName, pluginVersion));
    }

    public Plugin remove(String pluginName, String pluginVersion) {
        checkNotNull(pluginName, "plugin name is null");
        checkNotNull(pluginVersion, "plugin version is null");
        String key = toKey(pluginName, pluginVersion);
        if (log.isInfoEnabled()) {
            log.info("Remove plugin: {}", key);
        }
        return allPlugins.remove(key);
    }

    @Override
    public void destroy() {
        for (Plugin each : allPlugins.values()) {
            try {
                each.destroy();
            } catch (Exception e) {
                log.error(String.format("Failed to destroy plugin: %s", toKey(each.getPluginConfig().name(), each.getPluginConfig().version())), e);
            }
        }
        allPlugins.clear();
    }

    private String toKey(String pluginName, String pluginVersion) {
        return pluginName + "#" + pluginVersion;
    }

}