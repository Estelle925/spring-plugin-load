package org.thingsboard.server;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import static lombok.Lombok.checkNotNull;

@Slf4j
@Component
public class PluginManager implements DisposableBean {

    /**
     * 已注册的所有模块
     */
    private final ConcurrentHashMap<String, Plugin> allPlugins = new ConcurrentHashMap<>();

    public Plugin register(Plugin plugin) {
        checkNotNull(plugin, "module is null");
        String key = toKey(plugin.getPluginConfig().name(), plugin.getPluginConfig().version());
        if (log.isInfoEnabled()) {
            log.info("Put module: {}", key);
        }
        return allPlugins.put(key, plugin);
    }

    public Collection<Plugin> allPlugins() {
        return allPlugins.values();
    }

    public Plugin find(String moduleName, String moduleVersion) {
        checkNotNull(moduleName, "module name is null");
        checkNotNull(moduleVersion, "module version is null");
        return allPlugins.get(toKey(moduleName, moduleVersion));
    }

    public Plugin remove(String moduleName, String moduleVersion) {
        checkNotNull(moduleName, "module name is null");
        checkNotNull(moduleVersion, "module version is null");
        String key = toKey(moduleName, moduleVersion);
        if (log.isInfoEnabled()) {
            log.info("Remove module: {}", key);
        }
        return allPlugins.remove(key);
    }

    @Override
    public void destroy() {
        for (Plugin each : allPlugins.values()) {
            try {
                each.destroy();
            } catch (Exception e) {
                log.error(String.format("Failed to destroy module: %s", toKey(each.getPluginConfig().name(), each.getPluginConfig().version())), e);
            }
        }
        allPlugins.clear();
    }

    private String toKey(String moduleName, String moduleVersion) {
        return moduleName + "#" + moduleVersion;
    }

}