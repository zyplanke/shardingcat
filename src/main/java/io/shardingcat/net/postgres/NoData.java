
package io.shardingcat.net.postgres;

/**
 * <pre>
 * NoData (B) 
 * Byte1('n') Identifies the message as a no-data indicator.
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class NoData extends PostgresPacket {

}