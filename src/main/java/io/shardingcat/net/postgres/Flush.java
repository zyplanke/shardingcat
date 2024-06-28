
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Flush (F) 
 * Byte1('H') Identifies the message as a Flush command. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class Flush extends PostgresPacket {

}