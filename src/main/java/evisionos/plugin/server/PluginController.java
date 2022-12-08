package evisionos.plugin.server;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.List;

/**
 * @author chenhaiming
 */
@RestController
@RequestMapping("/api")
public class PluginController {

    @Resource
    private PluginService pluginService;

    @GetMapping("/plugin/install")
    public void installPlugin(String path,String name,String version) {
        pluginService.loadAndRegister(Paths.get(path),name,version);
    }


    @GetMapping("/plugin/unInstall")
    public void unInstallPlugin(String pluginName,String pluginVersion) {
        pluginService.removeAndDestroy(pluginName, pluginVersion);
    }

    @GetMapping("/plugin/getAllPlugins")
    public List<PluginConfigVO> getAllPlugins(){
        return pluginService.queryAllPlugin();
    }

}
