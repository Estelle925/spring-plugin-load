package evisionos.plugin.server;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * @author chenhaiming
 */

public class SpringUtils {

    public static boolean isSpringBeanClass(Class<?> cla) {
        if (cla == null) {
            return false;
        }
        //是否是接口
        if (cla.isInterface()) {
            return false;
        }
        //是否是抽象类
        if (Modifier.isAbstract(cla.getModifiers())) {
            return false;
        }
        if (cla.getAnnotation(Component.class) != null) {
            return true;
        }
        if (cla.getAnnotation(Repository.class) != null) {
            return true;
        }
        if (cla.getAnnotation(Configuration.class) != null) {
            return true;
        }
        if (cla.getAnnotation(Bean.class) != null) {
            return true;
        }
        if (cla.getAnnotation(Autowired.class) != null) {
            return true;
        }
        return cla.getAnnotation(Service.class) != null;
    }

    public static boolean isSpringController(Class<?> cla) {
        if (cla == null) {
            return false;
        }
        //是否是接口
        if (cla.isInterface()) {
            return false;
        }
        if (cla.getAnnotation(RestController.class) != null) {
            return true;
        }
        return cla.getAnnotation(Controller.class) != null;
    }

    public static void registerBean(Class<?> aClass, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(aClass);
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition(aClass.getName(), beanDefinition);
    }

    public static void registerController(Class<?> aClass, BeanDefinitionRegistry beanDefinitionRegistry) {
        BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(aClass);
        BeanDefinition beanDefinition = builder.getBeanDefinition();
        beanDefinitionRegistry.registerBeanDefinition(aClass.getSimpleName(), new RootBeanDefinition(aClass));

    }

    public static void registerController(String controllerBeanName,RequestMappingHandlerMapping requestMappingHandlerMapping) throws Exception {
        //注册Controller
        Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
        //将private改为可使用
        method.setAccessible(true);
        method.invoke(requestMappingHandlerMapping, controllerBeanName);
    }


}
