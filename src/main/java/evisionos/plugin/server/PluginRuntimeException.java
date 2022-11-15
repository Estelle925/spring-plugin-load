package evisionos.plugin.server;

class PluginRuntimeException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PluginRuntimeException(String message) {
        super(message);
    }

    public PluginRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

}