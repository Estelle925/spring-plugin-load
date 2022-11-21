package evisionos.plugin.server;

import cn.hutool.core.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.EnvironmentAware;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author chenhaiming
 */
@Slf4j
@Component
public class ScanJarPathRunner implements CommandLineRunner, EnvironmentAware {


    private String pluginJarAbsolutePath;
    @Resource
    private PluginService pluginService;

    @Override
    public void run(String... args) {
        if (StrUtil.isNotBlank(pluginJarAbsolutePath)) {
            log.info("Start to run scan jar path: {}", pluginJarAbsolutePath);
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(pluginJarAbsolutePath), "*.jar")) {
                stream.forEach(pluginService::loadAndRegister);
            } catch (Exception e) {
                log.error("Load exist jar exception", e);
                throw new PluginRuntimeException("load exist jar exception");
            }
        }
    }


    @Override
    public void setEnvironment(Environment environment) {
        pluginJarAbsolutePath = environment.getProperty("evisionos.plugin.loadPath");
    }
}