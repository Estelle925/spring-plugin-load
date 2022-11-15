package evisionos.plugin;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import evisionos.plugin.server.PluginAutoConfigurationRegistrar;
import evisionos.plugin.server.PluginLoader;
import evisionos.plugin.server.PluginManager;
import evisionos.plugin.server.PluginService;

import java.util.List;
import java.util.Map;

@Slf4j
public class PluginLoadBeanSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnablePluginLoadServer.class.getName());
        this.initEnableConfig(attrs);

        List<String> selectors = Lists.newArrayList();
        selectors.add(PluginLoader.class.getName());
        selectors.add(PluginManager.class.getName());
        selectors.add(PluginService.class.getName());
        selectors.add(PluginAutoConfigurationRegistrar.class.getName());

        return selectors.toArray(new String[]{});
    }

    private void initEnableConfig(Map<String, Object> attrs){
    }


}
