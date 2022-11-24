package evisionos.plugin.server;

import com.google.common.collect.Sets;
import evisionos.plugin.PluginConfig;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.CachedIntrospectionResults;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

@Slf4j
@Component
public class PluginLoader implements ApplicationContextAware {

    /**
     * 注入父applicationContext
     */
    @Setter
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
        if (log.isInfoEnabled()) {
            log.info("Start to load plugin: {}", jarPath);
        }
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
        pluginClassLoader.addOverridePackages(Sets.newHashSet());
        ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            // 把当前线程的ClassLoader切换成模块的
            Thread.currentThread().setContextClassLoader(pluginClassLoader);
            PluginApplicationContext pluginApplicationContext = new PluginApplicationContext();
            pluginApplicationContext.setParent(applicationContext);
            pluginApplicationContext.setClassLoader(pluginClassLoader);
            pluginApplicationContext.scan(Sets.newHashSet(pluginConfig.getClass().getPackage().getName()).toArray(new String[0]));
            pluginApplicationContext.refresh();

            Set<Class<?>> classes = ClassUtil.getClasses(pluginClassLoader, jarPath.toString());
            for (Class<?> aClass : classes) {
                if (SpringUtils.isSpringController(aClass)) {
                    SpringUtils.registerController(aClass.getName(),applicationContext,requestMappingHandlerMapping);
                }
            }

            log.info("Load plugin success: name={}, version={}, jarPath={}", pluginConfig.name(), pluginConfig.version(), jarPath);
            return new Plugin(jarPath, pluginConfig, pluginApplicationContext);
        } catch (Throwable e) {
            log.error(String.format("Load plugin exception, jarPath=%s", jarPath), e);
            CachedIntrospectionResults.clearClassLoader(pluginClassLoader);
            throw new PluginRuntimeException("create pluginApplicationContext exception", e);
        } finally {
            // 还原当前线程的ClassLoader
            Thread.currentThread().setContextClassLoader(currentClassLoader);
        }
    }

    public PluginConfigVO preLoad(Path jarPath){
        if (log.isInfoEnabled()) {
            log.info("Start to load plugin: {}", jarPath);
        }
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