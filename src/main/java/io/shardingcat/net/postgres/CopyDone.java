
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CopyDone (F & B) 
 * Byte1('c') Identifies the message as a COPY-complete indicator. 
 * Int32(4) Length of message contents in bytes, including self.
 * </pre>
 * 
 * @author shardingcat
 */
public class CopyDone extends PostgresPacket {

}