package org.thingsboard.server;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

@Configuration
@Import(PluginAutoConfigurationRegistrar.class)
public class PluginAutoRegistrarConfiguration {
}
