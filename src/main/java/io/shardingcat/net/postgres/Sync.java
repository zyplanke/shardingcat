
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Sync (F) 
 * Byte1('S') Identifies the message as a Sync command. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class Sync extends PostgresPacket {

}