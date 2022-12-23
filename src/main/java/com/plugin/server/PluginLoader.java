package com.plugin.server;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.plugin.PluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

@Slf4j
@Component
public class PluginLoader {

    /**
     * 注入父applicationContext
     */
    @Resource
    private ApplicationContext applicationContext;

    @Resource
    private RequestMappingHandlerMapping requestMappingHandlerMapping;

    private PluginConfig pluginConfig;

    /**
     * 加载模块
     *
     * @param jarPath jar包路径
     * @return plugin
     */
    public Plugin load(Path jarPath) {
        //判断jar是否存在
        if (!Files.exists(jarPath)) {
            throw new PluginRuntimeException("jar file is noe exist");
        }
        log.info("Start to load plugin: {}", jarPath);
        PluginClassLoader pluginClassLoader;
        try {
            pluginClassLoader = new PluginClassLoader(jarPath.toUri().toURL(), applicationContext.getClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginRuntimeException("create pluginClassLoader exception", e);
        }

        List<PluginConfig> pluginConfigList = new ArrayList<>();
        ServiceLoader<PluginConfig> loadedDrivers = ServiceLoader.load(PluginConfig.class, pluginClassLoader);
        loadedDrivers.forEach(pluginConfigList::add);
        if (pluginConfigList.size() != 1) {
            throw new PluginRuntimeException("plugin config has and only has one");
        }
        pluginConfig = pluginConfigList.get(0);
        checkNotNull(pluginConfig.name(), "pluginName is null");
        checkNotNull(pluginConfig.version(), "pluginVersion is null");

        pluginClassLoader.addOverridePackages(Sets.newHashSet());
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 把当前线程的ClassLoader切换成模块的
            //注入bean
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            PluginApplicationContext pluginApplicationContext = new PluginApplicationContext();
            pluginApplicationContext.setParent(applicationContext);
            pluginApplicationContext.setClassLoader(pluginClassLoader);
            pluginApplicationContext.scan(Sets.newHashSet(pluginConfig.getClass().getPackage().getName()).toArray(new String[0]));
            pluginApplicationContext.refresh();

            //controller注入
            List<Object> mvcControllers = Lists.newArrayList();
            Set<Class<?>> classes = ClassUtil.getClasses(pluginClassLoader, jarPath.toString());
            for (Class<?> aClass : classes) {
                if (SpringUtils.isSpringController(aClass)) {
                    Object targetBean = pluginApplicationContext.getBean(aClass);
                    mvcControllers.add(targetBean);
                    SpringUtils.registerController(targetBean, requestMappingHandlerMapping);
                }
            }

            log.info("Load plugin success: name={}, version={}, jarPath={}", pluginConfig.name(), pluginConfig.version(), jarPath);
            return new Plugin(jarPath, pluginConfig,mvcControllers, pluginApplicationContext);
        } catch (Throwable e) {
            log.error(String.format("Load plugin exception, jarPath=%s", jarPath), e);
            CachedIntrospectionResults.clearClassLoader(pluginClassLoader);
            throw new PluginRuntimeException("create pluginApplicationContext exception", e);
        } finally {
            // 还原当前线程的ClassLoader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    public PluginConfigVO preLoad(Path jarPath) {
        log.info("pre Start to load plugin: {}", jarPath);
        PluginClassLoader pluginClassLoader;
        try {
            pluginClassLoader = new PluginClassLoader(jarPath.toUri().toURL(), applicationContext.getClassLoader());
        } catch (MalformedURLException e) {
            throw new PluginRuntimeException("create pluginClassLoader exception", e);
        }

        List<PluginConfig> pluginConfigList = new ArrayList<>();
        ServiceLoader<PluginConfig> loadedDrivers = ServiceLoader.load(PluginConfig.class, pluginClassLoader);
        loadedDrivers.forEach(pluginConfigList::add);
        if (pluginConfigList.size() != 1) {
            throw new PluginRuntimeException("plugin config has and only has one");
        }
        pluginConfig = pluginConfigList.get(0);
        return PluginConfigVO.configToConfigVo(pluginConfig);
    }

}
