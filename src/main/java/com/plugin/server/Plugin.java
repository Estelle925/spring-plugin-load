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

    private final Map<String, PluginHandler> handlers;
    private Path jarPath;
    private PluginConfig pluginConfig;
    private PluginApplicationContext pluginApplicationContext;

    private List<Object> mvcController;
    public Plugin(Path jarPath, PluginConfig pluginConfig,List<Object> mvcController, PluginApplicationContext pluginApplicationContext) {
        this.jarPath = jarPath;
        this.pluginConfig = pluginConfig;
        this.pluginApplicationContext = pluginApplicationContext;
        this.handlers = scanHandlers();
        this.mvcController = mvcController;
    }

    private Map<String, PluginHandler> scanHandlers() {
        Map<String, PluginHandler> handlers = Maps.newHashMap();
        // find Handler in plugin
        for (PluginHandler handler : pluginApplicationContext.getBeansOfType(PluginHandler.class).values()) {
            String handlerName = handler.name();
            if (!StringUtils.hasText(handlerName)) {
                throw new PluginRuntimeException("scanHandlers handlerName is null");
            }
            checkState(!handlers.containsKey(handlerName), "Duplicated handler %s found by: %s",
                    PluginHandler.class.getSimpleName(), handlerName);
            handlers.put(handlerName, handler);
        }
        if (log.isInfoEnabled()) {
            log.info("Scan handlers finish: {}", String.join(",", handlers.keySet()));
        }
        return ImmutableMap.copyOf(handlers);
    }

    public PluginHandler getHandler(String handlerName) {
        checkNotNull(handlerName, "handlerName is null");
        PluginHandler handler = handlers.get(handlerName);
        checkNotNull(handler, String.format("find handler is null, handlerName=%s", handlerName));
        return handler;
    }

    public Object doHandler(String handlerName, String handlerArgs) {
        checkNotNull(handlerName, "handlerName is null");
        checkNotNull(handlerArgs, "handlerArgs is null");
        return doHandlerWithinPluginClassLoader(getHandler(handlerName), handlerArgs);
    }

    private Object doHandlerWithinPluginClassLoader(PluginHandler handler, String handlerArgs) {
        checkNotNull(handler, "handler is null");
        checkNotNull(handlerArgs, "handlerArgs is null");
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader pluginClassLoader = handler.getClass().getClassLoader();
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            return handler.execute(handlerArgs);
        } catch (Exception e) {
            log.error(String.format("Invoke plugin exception, handler=%s", handler.name()), e);
            throw new PluginRuntimeException(String.format("doHandlerWithinPluginClassLoader has error, handler=%s", handler.getClass().getName()), e);
        } finally {
            Thread.currentThread().setContextClassLoader(classLoader);
        }
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
        // Introspector??????BeanInfo???????????????????????????????????????????????????Introspector??????????????????
        Introspector.flushCaches();
        // ????????????????????????????????????????????????????????????????????????
        ResourceBundle.clearCache(classLoader);
        // clear the introspection cache for the given ClassLoader
        CachedIntrospectionResults.clearClassLoader(classLoader);
        // close
        if (classLoader instanceof URLClassLoader) {
            ((URLClassLoader) classLoader).close();
        }
    }

}
