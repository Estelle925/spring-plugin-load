package com.plugin.server;

public interface PluginHandler {

    Object execute(String params);

    String name();

}
