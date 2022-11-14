package org.thingsboard.server;

public interface PluginHandler {

    Object execute(String params);

    String name();

}