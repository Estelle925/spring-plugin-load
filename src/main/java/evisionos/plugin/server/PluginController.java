package evisionos.plugin.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.file.Paths;

/**
 * @author chenhaiming
 */
@RestController
@RequestMapping("/api")
public class PluginController {

    @Resource
    private PluginService pluginService;



    @GetMapping("/plugin/install")
    public void installPlugin(String path) {
        pluginService.loadAndRegister(Paths.get(path));
    }


    @GetMapping("/plugin/unInstall")
    public void unInstallPlugin(String pluginName,String pluginVersion) {
        pluginService.removeAndDestroy(pluginName, pluginVersion);
    }

    @GetMapping("/plugin/getAllPlugins")
    public String getAllPlugins(){
        return pluginService.queryAllPlugin();
    }

}
