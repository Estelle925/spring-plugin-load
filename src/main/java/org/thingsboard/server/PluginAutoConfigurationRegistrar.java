package org.thingsboard.server;

import cn.hutool.core.collection.CollectionUtil;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

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

@Slf4j
class PluginAutoConfigurationRegistrar implements ImportBeanDefinitionRegistrar {


    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        //获取目录下jar包
        List<String> jarPaths = Lists.newArrayList();
        String moduleJarAbsolutePath = "/Users/chenhaiming/data/tb/driver";
        try {
            Stream<Path> paths = Files.walk(Paths.get(moduleJarAbsolutePath), 2);
            paths.map(Path::toString).filter(f -> f.endsWith(".jar")).forEach(jarPaths::add);
            paths.close();
        } catch (Exception e) {
            log.error("load jars packages from path fail exception = ", e);
        }

        //加载jar包
        if (CollectionUtil.isNotEmpty(jarPaths)) {
            for (String path : jarPaths) {
                Path jarPath = Path.of(path);
                try {
                    URL[] urls = new URL[]{jarPath.toUri().toURL()};
                    URLClassLoader urlClassLoader = new URLClassLoader(urls, Thread.currentThread().getContextClassLoader());
                    List<PluginConfig> pluginConfigList = new ArrayList<>();
                    ServiceLoader<PluginConfig> loadedDrivers = ServiceLoader.load(PluginConfig.class, urlClassLoader);
                    loadedDrivers.forEach(pluginConfigList::add);
                    if (pluginConfigList.size() != 1) {
                        log.error("plugin config has and only has one");
                        break;
                    }

                    PluginConfig pluginConfig = pluginConfigList.get(0);
                    Set<Class<?>> classes = ClassUtil.getClasses(urlClassLoader, path);
                    for (Class<?> aClass : classes) {
                        if (SpringUtils.isSpringBeanClass(aClass)) {
                            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(aClass);
                            BeanDefinition beanDefinition = builder.getBeanDefinition();
                            beanDefinitionRegistry.registerBeanDefinition(aClass.getName(), beanDefinition);
                        }
                    }
                    log.info("Load plugin success: name={}, version={}, jarPath={}", pluginConfig.name(), pluginConfig.version(), jarPath);
                } catch (Exception e) {
                    log.error(String.format("Load plugin exception, jarPath=%s", jarPath), e);
                    break;
                }
            }
        }
    }

}