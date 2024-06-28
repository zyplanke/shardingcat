
package io.shardingcat.net.postgres;

/**
 * <pre>
 * CopyFail (F) 
 * Byte1('f') Identifies the message as a COPY-failure indicator. 
 * Int32 Length of message contents in bytes, including self.
 * String An error message to report as the cause of failure.
 * </pre>
 * 
 * @author shardingcat
 */
public class CopyFail extends PostgresPacket {

}