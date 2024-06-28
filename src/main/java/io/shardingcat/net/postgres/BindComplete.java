
package io.shardingcat.net.postgres;

/**
 * <pre>
 * BindComplete (B) 
 * Byte1('2') Identifies the message as a Bind-complete indicator. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class BindComplete extends PostgresPacket {

}