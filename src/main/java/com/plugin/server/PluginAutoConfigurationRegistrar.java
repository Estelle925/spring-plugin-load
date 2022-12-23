package com.plugin.server;

import com.google.common.collect.Lists;
import com.plugin.PluginConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;
import org.springframework.util.CollectionUtils;

import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Stream;

/**
 * @author chenhaiming
 */
@Slf4j
@Deprecated
public class PluginAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar, EnvironmentAware {

    String pluginJarAbsolutePath;

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata annotationMetadata, @NonNull BeanDefinitionRegistry beanDefinitionRegistry) {
        if (pluginJarAbsolutePath != null && !pluginJarAbsolutePath.isEmpty() && !pluginJarAbsolutePath.trim().isEmpty()) {

            //获取目录下jar包
            List<String> jarPaths = Lists.newArrayList();
            try {
                Stream<Path> paths = Files.walk(Paths.get(pluginJarAbsolutePath), 2);
                paths.map(Path::toString).filter(f -> f.endsWith(".jar")).forEach(jarPaths::add);
                paths.close();
            } catch (Exception e) {
                log.error("load jars packages from path fail exception = ", e);
            }

            //加载jar包
            if (!CollectionUtils.isEmpty(jarPaths)) {
                for (String path : jarPaths) {
                    Path jarPath = Paths.get(path);
                    try {
                        URL[] urls = new URL[]{jarPath.toUri().toURL()};
                        URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                        List<PluginConfig> pluginConfigList = new ArrayList<>();
                        ServiceLoader<PluginConfig> loadedDrivers = ServiceLoader.load(PluginConfig.class, urlClassLoader);
                        loadedDrivers.forEach(pluginConfigList::add);
                        if (pluginConfigList.size() != 1) {
                            log.error("plugin config has and only has one");
                            continue;
                        }

                        PluginConfig pluginConfig = pluginConfigList.get(0);
                        Set<Class<?>> classes = ClassUtil.getClasses(urlClassLoader, path);
                        for (Class<?> aClass : classes) {
                            if (SpringUtils.isSpringBeanClass(aClass)) {
                                SpringUtils.registerBean(aClass, beanDefinitionRegistry);
                            }
//                            if (SpringUtils.isSpringController(aClass)) {
//                                SpringUtils.registerController(aClass, beanDefinitionRegistry);
//                            }
                        }
                        log.info("Load plugin success: name={}, version={}, jarPath={}", pluginConfig.name(), pluginConfig.version(), jarPath);
                    } catch (Exception e) {
                        log.error(String.format("Load plugin exception, jarPath=%s", jarPath), e);
                    }
                }
            }
        }
    }

    @Override
    public void setEnvironment(Environment environment) {
        pluginJarAbsolutePath = environment.getProperty("plugin.loadPath");
    }
}
