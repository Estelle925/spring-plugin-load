package com.plugin.server;

import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.collect.Iterables.any;

@Slf4j
class PluginClassLoader extends URLClassLoader {

    public static final String[] DEFAULT_EXCLUDED_PACKAGES = new String[]{"java.", "javax.", "sun.", "oracle."};

    private final Set<String> excludedPackages;

    private final Set<String> overridePackages;

    public PluginClassLoader(URL url, ClassLoader parent) {
        super(new URL[]{url}, parent);
        this.excludedPackages = Sets.newHashSet(Arrays.asList(DEFAULT_EXCLUDED_PACKAGES.clone()));
        this.overridePackages = Sets.newHashSet();
    }

    public void addExcludedPackages(Set<String> excludedPackages) {
        this.excludedPackages.addAll(excludedPackages);
    }

    public void addOverridePackages(Set<String> overridePackages) {
        this.overridePackages.addAll(overridePackages);
    }

    @Override
    protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
        Class<?> result = null;
        synchronized (PluginClassLoader.class) {
            if (isEligibleForOverriding(name)) {
                if (log.isInfoEnabled()) {
                    log.info("Load class for overriding: {}", name);
                }
                result = loadClassForOverriding(name);
            }
            if (Objects.nonNull(result)) {
                // 链接类
                if (resolve) {
                    resolveClass(result);
                }
                return result;
            }
        }
        // 使用默认类加载方式
        return super.loadClass(name, resolve);
    }

    private Class<?> loadClassForOverriding(String name) throws ClassNotFoundException {
        // 查找已加载的类
        Class<?> result = findLoadedClass(name);
        if (Objects.isNull(result)) {
            // 加载类
            result = findClass(name);
        }
        return result;
    }

    private boolean isEligibleForOverriding(final String name) {
        checkNotNull(name, "name is null");
        return !isExcluded(name) && any(overridePackages, name::startsWith);
    }

    protected boolean isExcluded(String className) {
        checkNotNull(className, "className is null");
        for (String packageName : this.excludedPackages) {
            if (className.startsWith(packageName)) {
                return true;
            }
        }
        return false;
    }

}
