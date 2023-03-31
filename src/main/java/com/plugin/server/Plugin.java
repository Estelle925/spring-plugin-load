package com.plugin.server;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.plugin.PluginConfig;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

@Data
@Slf4j
@AllArgsConstructor
public class Plugin {

    private Path jarPath;
    private PluginConfig pluginConfig;
    private PluginApplicationContext pluginApplicationContext;

    private List<Object> mvcController;
    public Plugin(Path jarPath, PluginConfig pluginConfig,List<Object> mvcController, PluginApplicationContext pluginApplicationContext) {
        this.jarPath = jarPath;
        this.pluginConfig = pluginConfig;
        this.pluginApplicationContext = pluginApplicationContext;
        this.mvcController = mvcController;
    }

    public void destroy() throws Exception {
        if (log.isInfoEnabled()) {
            log.info("Destroy plugin: name={}, version={}", pluginConfig.name(), pluginConfig.version());
        }
        // close spring context
        closeApplicationContext(pluginApplicationContext);
        // clean class loader
        clearClassLoader(pluginApplicationContext.getClassLoader());
        // delete jar file
//        Files.deleteIfExists(jarPath);
    }

    private void closeApplicationContext(ConfigurableApplicationContext applicationContext) {
        checkNotNull(applicationContext, "applicationContext is null");
        try {
            applicationContext.close();
        } catch (Exception e) {
            log.error("Failed to close application context", e);
        }
    }

    private void clearClassLoader(ClassLoader classLoader) throws IOException {
        checkNotNull(classLoader, "classLoader is null");
        // Introspector缓存BeanInfo类来获得更好的性能。卸载时刷新所有Introspector的内部缓存。
        Introspector.flushCaches();
        // 从已经使用给定类加载器加载的缓存中移除所有资源包
        ResourceBundle.clearCache(classLoader);
        // clear the introspection cache for the given ClassLoader
        CachedIntrospectionResults.clearClassLoader(classLoader);
        // close
        if (classLoader instanceof URLClassLoader) {
            ((URLClassLoader) classLoader).close();
        }
    }

}
