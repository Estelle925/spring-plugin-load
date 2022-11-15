package evisionos.plugin.server;


import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.Enumeration;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * 扫描包下的所有类
 *
 */
@Slf4j
public class ClassUtil {

    /**
     * 以文件的形式来获取包下的所有Class
     */
    public static Set<Class<?>> getClasses(ClassLoader classLoader, String packagePath) {
        Set<Class<?>> classes = Sets.newHashSet();
        try {
            File jar = new File(packagePath);
            JarFile jarFile = new JarFile(jar);
            for (Enumeration<JarEntry> ea = jarFile.entries(); ea.hasMoreElements(); ) {
                JarEntry jarEntry = ea.nextElement();
                String name = jarEntry.getName();
                if (name.endsWith(".class")) {
                    String loadName = name.replace("/", ".").substring(0, name.length() - 6);
                    //加载class
                    Class<?> c = classLoader.loadClass(loadName);
                    classes.add(c);
                }
            }
            jarFile.close();
        } catch (Exception e) {
            log.error("ClassUtil load class from jar error ", e);
        }

        return classes;
    }
}
