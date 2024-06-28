
package io.shardingcat.net.postgres;

/**
 * <pre>
 * ParseComplete (B) 
 * Byte1('1') Identifies the message as a Parse-complete indicator. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class ParseComplete extends PostgresPacket {

}