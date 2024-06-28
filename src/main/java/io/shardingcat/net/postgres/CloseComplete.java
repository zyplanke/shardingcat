
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CloseComplete (B) 
 * Byte1('3') Identifies the message as a Close-complete indicator. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class CloseComplete extends PostgresPacket {

}