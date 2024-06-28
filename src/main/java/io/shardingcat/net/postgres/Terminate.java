
package io.shardingcat.net.postgres;

/**
 * <pre>
 * Terminate (F) 
 * Byte1('X') Identifies the message as a termination.
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class Terminate extends PostgresPacket {

}