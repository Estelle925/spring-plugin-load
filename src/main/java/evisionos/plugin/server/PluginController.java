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
    public void installPlugin() {
//        configurableApplicationContext.refresh();
        pluginService.loadAndRegister(Paths.get("/Users/chenhaiming/workCode/EVISION/evision_iot_driver/evision-driver/tcp-driver/target/tcp-driver-3.4.1.jar"));
    }


    @GetMapping("unInstall")
    public void unInstallPlugin() {
        pluginService.removeAndDestroy("tcp-plugin", "1.0.0");
    }


}
