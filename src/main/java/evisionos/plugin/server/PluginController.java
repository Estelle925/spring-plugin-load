package evisionos.plugin.server;

import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.file.Paths;

@RestController
public class PluginController {

    @Resource
    private PluginService pluginService;

    @Resource
    private ConfigurableApplicationContext configurableApplicationContext;


    @GetMapping("install")
    public void installPlugin(String path) {
//        configurableApplicationContext.refresh();
        pluginService.loadAndRegister(Paths.get(path));
    }


    @GetMapping("unInstall")
    public void unInstallPlugin(String pluginName,String pluginVersion) {
        pluginService.removeAndDestroy(pluginName, pluginVersion);
    }


}
