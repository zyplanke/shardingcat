
package io.shardingcat.config.util;

/**
 * @author shardingcat
 */
public class ObjectAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public ObjectAccessException(String message) {
        super(message);
    }

    public ObjectAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}