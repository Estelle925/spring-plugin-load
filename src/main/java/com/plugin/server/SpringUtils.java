package com.plugin.server;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.util.ClassUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author chenhaiming
 */

@Slf4j
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

    public static void unRegisterController(ApplicationContext applicationContext, Class<?> object) {
        final RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) applicationContext.getBean("requestMappingHandlerMapping");
        Object controller = applicationContext.getBean(object);
        final Class<?> targetClass = controller.getClass();
        ReflectionUtils.doWithMethods(targetClass, method -> {
            Method specificMethod = ClassUtils.getMostSpecificMethod(method, targetClass);
            try {
                Method createMappingMethod = RequestMappingHandlerMapping.class.getDeclaredMethod("getMappingForMethod", Method.class, Class.class);
                createMappingMethod.setAccessible(true);
                RequestMappingInfo requestMappingInfo = (RequestMappingInfo) createMappingMethod.invoke(requestMappingHandlerMapping, specificMethod, targetClass);
                if (requestMappingInfo != null) {
                    requestMappingHandlerMapping.unregisterMapping(requestMappingInfo);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }, ReflectionUtils.USER_DECLARED_METHODS);
    }

    public static void registerController(Object controller, RequestMappingHandlerMapping requestMappingHandlerMapping) throws Exception {
        Method method = requestMappingHandlerMapping.getClass().getSuperclass().getSuperclass().getDeclaredMethod("detectHandlerMethods", Object.class);
        //将private改为可使用
        method.setAccessible(true);
        method.invoke(requestMappingHandlerMapping, controller);
    }

    public static boolean isRefDubboClass(Class<?> clazz) {
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            DubboReference dubboReference = field.getAnnotation(DubboReference.class);
            if (dubboReference != null) {
                return true;
            }
        }
        return false;
    }

    public static List<Class<?>> getDubboConsumer(Class<?> clazz, Set<Class<?>> classes) {
        List<Class<?>> list = Lists.newArrayList();
        Field[] fields = clazz.getDeclaredFields();
        for (Field field : fields) {
            //取类中引用的 provider类
            if (field.isAnnotationPresent(DubboReference.class)) {
                for (Class<?> aClass : classes) {
                    //provider类 创建 consumer
                    if (field.getName().equalsIgnoreCase(aClass.getSimpleName())) {
                        list.add(aClass);
                    }
                }
            }
        }
        return list;
    }

    public static void registerDubboConsumer(Map<Class<?>, List<Class<?>>> proxyClassMap, PluginApplicationContext applicationContext) throws IllegalAccessException, InstantiationException {
        for (Map.Entry<Class<?>, List<Class<?>>> entry : proxyClassMap.entrySet()) {
            List<Class<?>> proxyClass = entry.getValue();
            Class<?> mainClass = entry.getKey();
            String beanName = getBeanName(mainClass);
            Object bean = applicationContext.getBean(beanName);
            Class<?> beanClass = bean.getClass();
            for (Class<?> consumerClass : proxyClass) {
                Object proxyBean = DubboDynamicLoadUtil.createDubboBean(consumerClass);
                Field[] fields = beanClass.getDeclaredFields();
                for (Field field : fields) {
                    if (field.getName().equalsIgnoreCase(consumerClass.getSimpleName())) {
                        field.setAccessible(true);
                        field.set(bean, proxyBean);
                        log.info("给bean注入dubbo 依赖接口服务成功 bean = {},proxyBean={}", bean, proxyBean);
                    }
                }
                applicationContext.getBean(beanName);
            }
        }
    }

    public static String getBeanName(Class<?> aClass) {
        String beanName = null;
        if (aClass.getAnnotation(RestController.class) != null) {
            RestController restController = aClass.getAnnotation(RestController.class);
            if (StringUtils.isNotBlank(restController.value())) {
                beanName = restController.value();
            }
        }
        if (aClass.getAnnotation(Controller.class) != null) {
            Controller controller = aClass.getAnnotation(Controller.class);
            if (StringUtils.isNotBlank(controller.value())) {
                beanName = controller.value();
            }
        }
        if (aClass.getAnnotation(Service.class) != null) {
            Service service = aClass.getAnnotation(Service.class);
            if (StringUtils.isNotBlank(service.value())) {
                beanName = service.value();
            }
        }
        if (aClass.getAnnotation(Component.class) != null) {
            Component component = aClass.getAnnotation(Component.class);
            if (StringUtils.isNotBlank(component.value())) {
                beanName = component.value();
            }
        }
        if (aClass.getAnnotation(Bean.class) != null) {
            Bean bean = aClass.getAnnotation(Bean.class);
            if (bean.value().length > 0) {
                beanName = bean.value()[0];
            }
        }
        if (StringUtils.isBlank(beanName)) {
            beanName = CommonUtils.firstCharToLowercase(aClass.getSimpleName());
        }
        return beanName;
    }
}
