package evisionos.plugin;

import com.google.common.collect.Lists;
import evisionos.plugin.server.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;

import java.util.List;
import java.util.Map;

/**
 * @author chenhaiming
 */
@Slf4j
public class PluginLoadBeanSelector implements ImportSelector {

    @Override
    public String[] selectImports(AnnotationMetadata importingClassMetadata) {
        Map<String, Object> attrs = importingClassMetadata.getAnnotationAttributes(EnablePluginLoadServer.class.getName());
        this.initEnableConfig(attrs);

        List<String> selectors = Lists.newArrayList();
        selectors.add(SpringUtils.class.getName());
        selectors.add(PluginLoader.class.getName());
        selectors.add(PluginManager.class.getName());
        selectors.add(PluginService.class.getName());
        selectors.add(PluginAutoConfigurationRegistrar.class.getName());

        return selectors.toArray(new String[]{});
    }

    private void initEnableConfig(Map<String, Object> attrs){
    }


}
