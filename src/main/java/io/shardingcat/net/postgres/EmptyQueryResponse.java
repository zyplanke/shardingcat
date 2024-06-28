
package io.shardingcat.net.postgres;

/**
 * <pre>
 * EmptyQueryResponse (B) 
 * Byte1('I') Identifies the message as a response to an empty query 
 *            string. (This substitutes for CommandComplete.) 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class EmptyQueryResponse extends PostgresPacket {

}