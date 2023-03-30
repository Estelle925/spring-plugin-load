package com.plugin.server;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import org.apache.dubbo.config.ReferenceConfig;
import org.apache.dubbo.config.utils.ReferenceConfigCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author chenhaiming
 */
public class DubboDynamicLoadUtil {

    private static final Logger LOGGER = LoggerFactory.getLogger(DubboDynamicLoadUtil.class);

    /**
     * 创建Dubbo服务接口的Bean
     *
     * @param interfaceClass Dubbo服务接口的Class
     * @return Dubbo服务接口的Bean
     */
    public static <T> T createDubboBean(Class<T> interfaceClass) {
        ReferenceConfig<T> referenceConfig = new ReferenceConfig<>();
        referenceConfig.setInterface(interfaceClass);
        referenceConfig.setLazy(true);
        ReferenceConfigCache cache = ReferenceConfigCache.getCache();
        return cache.get(referenceConfig);
    }

    /**
     * 注册Nacos Dubbo Consumer
     *
     * @param dubboBean    Dubbo服务接口的Bean
     * @param serviceName  Nacos服务名
     * @param groupName    Nacos服务分组
     * @param nacosAddress Nacos地址
     * @throws NacosException
     */
    public static <T> void registerNacosDubboConsumer(T dubboBean, String serviceName, String groupName, String nacosAddress) throws NacosException {
        NamingService namingService = NamingFactory.createNamingService(nacosAddress);
        Instance instance = new Instance();
        instance.setIp("127.0.0.1"); // TODO: 修改为实际的IP地址
        instance.setPort(0); // 自动分配端口
        instance.setWeight(1.0);
        instance.setHealthy(true);
        Map<String, String> metaData = instance.getMetadata();
        metaData.put("protocol", "dubbo");
        metaData.put("interface", dubboBean.getClass().getName());
        metaData.put("side", "consumer");
        namingService.registerInstance(serviceName, groupName, instance);
        namingService.subscribe(serviceName, groupName, event ->
                LOGGER.info("Nacos Dubbo Consumer is {}.", event.getClass()));
    }

    /**
     * 销毁Nacos Dubbo Consumer
     *
     * @param dubboBean    Dubbo服务接口的Bean
     * @param serviceName  Nacos服务名
     * @param groupName    Nacos服务分组
     * @param nacosAddress Nacos地址
     */
    public static <T> void destroyNacosDubboConsumer(T dubboBean, String serviceName, String groupName, String nacosAddress) throws NacosException {
        NamingService namingService = NamingFactory.createNamingService(nacosAddress);
        Map<String, String> metaData = namingService.selectOneHealthyInstance(serviceName, groupName).getMetadata();
        metaData.put("side", "consumer");
        namingService.deregisterInstance(serviceName, groupName, namingService.selectOneHealthyInstance(serviceName, groupName));
    }
}

