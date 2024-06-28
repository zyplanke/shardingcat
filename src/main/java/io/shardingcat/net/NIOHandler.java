
package io.shardingcat.net;

/**
 * @author shardingcat
 */
public interface NIOHandler {

    void handle(byte[] data);

}