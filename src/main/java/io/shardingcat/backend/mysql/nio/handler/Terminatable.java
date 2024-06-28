
package io.shardingcat.backend.mysql.nio.handler;

/**
 * @author shardingcat
 */
public interface Terminatable {
    void terminate(Runnable runnable);
}