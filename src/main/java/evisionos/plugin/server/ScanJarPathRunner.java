package evisionos.plugin.server;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;

import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
class ScanJarPathRunner implements CommandLineRunner {

    @Setter
    private String pluginJarAbsolutePath;
    @Setter
    private boolean pathIsNewCreated;
    @Setter
    private PluginService pluginService;

    @Override
    public void run(String... args) {
        if (pathIsNewCreated) {
            return;
        }
        log.info("Start to run scan jar path: {}", pluginJarAbsolutePath);
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(pluginJarAbsolutePath), "*.jar")) {
            stream.forEach(pluginService::loadAndRegister);
        } catch (Exception e) {
            log.error("Load exist jar exception", e);
            throw new PluginRuntimeException("load exist jar exception");
        }
    }

}