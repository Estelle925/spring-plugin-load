package com.plugin.server;

import com.plugin.api.PluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Component
public class PluginService {

    @Resource
    private PluginLoader pluginLoader;
    @Resource
    private PluginManager pluginManager;

    /**
     * 预加载插件信息
     * @param jarPath jar包路径
     * @return PluginConfigVO
     */
    public PluginConfigVO preLoad(Path jarPath) {
        return pluginLoader.preLoad(jarPath);
    }

    /**
     * 指定插件名和版本加载注册插件
     * @param jarPath jar包路径
     * @param pluginName 插件名
     * @param pluginVersion 插件版本
     * @return 插件加载注册成功
     */
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
            Collection<Plugin> allPlugin =  pluginManager.allPlugins();
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

    /**
     * 不指定插件名字和版本，加载注册插件
     * @param jarPath jar包路径
     * @return 插件加载注册成功
     */
    public boolean loadAndRegister(Path jarPath) {
        try {
            if (!Files.exists(jarPath)) {
                throw new PluginRuntimeException("jar file is noe exist");
            }
            Collection<Plugin> allPlugin =  pluginManager.allPlugins();
            for (Plugin plugin : allPlugin) {
                if (plugin.getJarPath().toString().equals(jarPath.toString())) {
                    throw new PluginRuntimeException("plugin load and register fail plugin jar file is loaded");
                }
            }
            Plugin plugin = pluginLoader.load(jarPath);
            Plugin successPlugin = pluginManager.register(plugin, null, null);
        } catch (Exception e) {
            throw new PluginRuntimeException("plugin load and register fail", e);
        }
        return true;
    }

    /**
     * 卸载删除插件
     * @param pluginName 插件名
     * @param pluginVersion 插件版本
     * @return 卸载删除成功
     */
    public boolean removeAndDestroy(String pluginName, String pluginVersion) {
        try {
            if (pluginManager.find(pluginName, pluginVersion) == null) {
                throw new PluginRuntimeException("plugin load and register fail plugin is not exist");
            }
            Plugin plugin = pluginManager.remove(pluginName, pluginVersion);
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
        } catch (Exception e) {
            throw new PluginRuntimeException("plugin remove and destroy fail", e);
        }
        return true;
    }

    /**
     * 获取所有插件
     * @return List<PluginConfigVO>
     */
    public List<PluginConfigVO> queryAllPlugin() {
        return pluginManager.allPlugins().stream().map(plugin -> {
            PluginConfig pluginConfig = plugin.getPluginConfig();
            return PluginConfigVO.configToConfigVo(pluginConfig);
        }).collect(Collectors.toList());
    }

}
