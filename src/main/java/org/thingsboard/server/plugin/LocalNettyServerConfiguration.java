package org.thingsboard.server.plugin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/**
 * @author chenhaiming
 */
@Configuration
public class LocalNettyServerConfiguration {

    @Scope(value = "singleton")
    @Bean(initMethod = "start", destroyMethod = "close")
    public LocalNettyServer localNettyServer() {
        return new LocalNettyServer();
    }

}
