package evisionos.plugin;

import org.springframework.context.annotation.Import;

import java.lang.annotation.*;

/**
 * @author chenhaiming
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Import(PluginLoadBeanSelector.class)
public @interface EnablePluginLoadServer {
}
